package com.battery.mantra.ui.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.battery.mantra.data.models.LoginRequest
import com.battery.mantra.data.models.LoginResponse
import com.battery.mantra.data.repository.AuthRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Success(val loginResponse: LoginResponse) : AuthState()
    data class Error(val message: String) : AuthState()
}

enum class LoginMode {
    ENGINEER_OTP,
    ADMIN_PASSWORD
}

data class LoginUiState(
    val identifier: String = "",
    val password: String = "",
    val passwordVisible: Boolean = false,
    val phoneNumber: String = "",
    val otp: String = "",
    val isOtpSent: Boolean = false,
    val resendCountdown: Int = 0,
    val loginMode: LoginMode = LoginMode.ENGINEER_OTP,
    val isOtpSending: Boolean = false
)

class AuthViewModel(private val authRepository: AuthRepository) : ViewModel() {
    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()
    
    private val _loginUiState = MutableStateFlow(LoginUiState())
    val loginUiState: StateFlow<LoginUiState> = _loginUiState.asStateFlow()

    private var countdownJob: Job? = null

    fun setLoginMode(mode: LoginMode) {
        _loginUiState.value = _loginUiState.value.copy(loginMode = mode)
    }

    fun updateIdentifier(identifier: String) {
        _loginUiState.value = _loginUiState.value.copy(identifier = identifier)
    }

    fun updatePassword(password: String) {
        _loginUiState.value = _loginUiState.value.copy(password = password)
    }

    fun togglePasswordVisibility() {
        _loginUiState.value = _loginUiState.value.copy(passwordVisible = !_loginUiState.value.passwordVisible)
    }

    fun updatePhoneNumber(phoneNumber: String) {
        val digits = phoneNumber.filter { it.isDigit() }.take(10)
        _loginUiState.value = _loginUiState.value.copy(phoneNumber = digits)
    }

    fun updateOtp(otp: String) {
        val digits = otp.filter { it.isDigit() }.take(6)
        _loginUiState.value = _loginUiState.value.copy(otp = digits)
    }

    fun resetOtpState() {
        countdownJob?.cancel()
        _loginUiState.value = _loginUiState.value.copy(
            isOtpSent = false,
            otp = "",
            resendCountdown = 0,
            isOtpSending = false
        )
    }

    private fun startCountdown(seconds: Int = 30) {
        countdownJob?.cancel()
        _loginUiState.value = _loginUiState.value.copy(resendCountdown = seconds)
        countdownJob = viewModelScope.launch {
            for (i in seconds downTo 1) {
                _loginUiState.value = _loginUiState.value.copy(resendCountdown = i)
                delay(1000)
            }
            _loginUiState.value = _loginUiState.value.copy(resendCountdown = 0)
        }
    }

    fun sendEngineerOtp(onSuccess: (String) -> Unit = {}) {
        val phone = _loginUiState.value.phoneNumber.trim()
        if (phone.length != 10) {
            _authState.value = AuthState.Error("Please enter a valid 10-digit phone number")
            return
        }
        _loginUiState.value = _loginUiState.value.copy(isOtpSending = true)
        viewModelScope.launch {
            val result = authRepository.sendEngineerOtp(phone)
            _loginUiState.value = _loginUiState.value.copy(isOtpSending = false)
            if (result.isSuccess) {
                _loginUiState.value = _loginUiState.value.copy(isOtpSent = true)
                startCountdown(30)
                onSuccess("OTP sent successfully to +91 $phone")
            } else {
                _authState.value = AuthState.Error(result.exceptionOrNull()?.message ?: "Failed to send OTP")
            }
        }
    }

    fun resendEngineerOtp(onSuccess: (String) -> Unit = {}) {
        if (_loginUiState.value.resendCountdown > 0) return
        sendEngineerOtp(onSuccess)
    }

    fun verifyEngineerOtp() {
        val phone = _loginUiState.value.phoneNumber.trim()
        val otp = _loginUiState.value.otp.trim()
        if (phone.length != 10) {
            _authState.value = AuthState.Error("Please enter a valid 10-digit phone number")
            return
        }
        if (otp.length < 4) {
            _authState.value = AuthState.Error("Please enter the 4-digit OTP")
            return
        }
        _authState.value = AuthState.Loading
        viewModelScope.launch {
            val result = authRepository.verifyEngineerOtp(phone, otp)
            if (result.isSuccess) {
                _authState.value = AuthState.Success(result.getOrNull()!!)
            } else {
                _authState.value = AuthState.Error(result.exceptionOrNull()?.message ?: "Invalid OTP")
            }
        }
    }

    fun login(username: String, password: String) {
        _authState.value = AuthState.Loading
        viewModelScope.launch {
            val result = authRepository.login(LoginRequest(username, password))
            if (result.isSuccess) {
                _authState.value = AuthState.Success(result.getOrNull()!!)
            } else {
                _authState.value = AuthState.Error(result.exceptionOrNull()?.message ?: "Unknown Error")
            }
        }
    }

    fun resetState() {
        _authState.value = AuthState.Idle
    }

    companion object {
        fun provideFactory(authRepository: AuthRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return AuthViewModel(authRepository) as T
                }
            }
    }
}
