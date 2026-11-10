package com.battery.mantra.ui.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.battery.mantra.data.local.TokenManager
import com.battery.mantra.data.models.LoginRequest
import com.battery.mantra.data.models.LoginResponse
import com.battery.mantra.data.repository.AuthRepository
import com.battery.mantra.data.repository.InvalidGatePinException
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
    PARTNER_OTP
}

sealed class AdminGateState {
    object Hidden : AdminGateState()
    data class LockedOut(val remainingSeconds: Long) : AdminGateState()
    data class PinChallenge(
        val attemptsLeft: Int,
        val isVerifying: Boolean = false,
        val error: String? = null
    ) : AdminGateState()
    data class BiometricChallenge(val tier: String, val gateToken: String) : AdminGateState()
    data class Unlocked(val tier: String, val gateToken: String) : AdminGateState()
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

    private val _adminGateState = MutableStateFlow<AdminGateState>(AdminGateState.Hidden)
    val adminGateState: StateFlow<AdminGateState> = _adminGateState.asStateFlow()

    private var countdownJob: Job? = null
    private var lockoutTickerJob: Job? = null

    fun setLoginMode(mode: LoginMode) {
        if (_loginUiState.value.loginMode == mode) return
        countdownJob?.cancel()
        _authState.value = AuthState.Idle
        _loginUiState.value = _loginUiState.value.copy(
            loginMode = mode,
            isOtpSent = false,
            otp = "",
            resendCountdown = 0,
            isOtpSending = false
        )
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

    fun sendOtp(onSuccess: (String) -> Unit = {}) {
        val phone = _loginUiState.value.phoneNumber.trim()
        if (phone.length != 10) {
            _authState.value = AuthState.Error("Please enter a valid 10-digit phone number")
            return
        }
        val mode = _loginUiState.value.loginMode
        _loginUiState.value = _loginUiState.value.copy(isOtpSending = true)
        viewModelScope.launch {
            val result = when (mode) {
                LoginMode.ENGINEER_OTP -> authRepository.sendEngineerOtp(phone)
                LoginMode.PARTNER_OTP -> authRepository.sendPartnerOtp(phone)
            }
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

    fun resendOtp(onSuccess: (String) -> Unit = {}) {
        if (_loginUiState.value.resendCountdown > 0) return
        sendOtp(onSuccess)
    }

    fun verifyOtp() {
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
        val mode = _loginUiState.value.loginMode
        _authState.value = AuthState.Loading
        viewModelScope.launch {
            val result = when (mode) {
                LoginMode.ENGINEER_OTP -> authRepository.verifyEngineerOtp(phone, otp)
                LoginMode.PARTNER_OTP -> authRepository.verifyPartnerOtp(phone, otp)
            }
            if (result.isSuccess) {
                _authState.value = AuthState.Success(result.getOrNull()!!)
            } else {
                _authState.value = AuthState.Error(result.exceptionOrNull()?.message ?: "Invalid OTP")
            }
        }
    }

    // --- Hidden Admin Gate State Machine ---
    fun onAdminTriggerActivated() {
        viewModelScope.launch {
            val remainingMs = authRepository.getAdminGateLockoutRemainingMs()
            if (remainingMs > 0L) {
                startLockoutTicker((remainingMs + 999L) / 1000L)
            } else {
                val failed = authRepository.getAdminGateFailedAttempts()
                val left = (TokenManager.MAX_ADMIN_GATE_ATTEMPTS - failed).coerceAtLeast(1)
                _adminGateState.value = AdminGateState.PinChallenge(attemptsLeft = left)
            }
        }
    }

    private fun startLockoutTicker(initialSeconds: Long) {
        lockoutTickerJob?.cancel()
        _adminGateState.value = AdminGateState.LockedOut(initialSeconds.coerceAtLeast(1L))
        lockoutTickerJob = viewModelScope.launch {
            var remaining = initialSeconds
            while (remaining > 0L && _adminGateState.value is AdminGateState.LockedOut) {
                _adminGateState.value = AdminGateState.LockedOut(remaining)
                delay(1000)
                remaining--
            }
            if (_adminGateState.value is AdminGateState.LockedOut) {
                authRepository.resetAdminGateAttempts()
                _adminGateState.value = AdminGateState.PinChallenge(attemptsLeft = TokenManager.MAX_ADMIN_GATE_ATTEMPTS)
            }
        }
    }

    fun submitGatePin(pin: String, canUseBiometricOnDevice: Boolean) {
        val trimmed = pin.trim()
        if (trimmed.isEmpty()) {
            val currentLeft = (_adminGateState.value as? AdminGateState.PinChallenge)?.attemptsLeft
                ?: TokenManager.MAX_ADMIN_GATE_ATTEMPTS
            _adminGateState.value = AdminGateState.PinChallenge(
                attemptsLeft = currentLeft,
                error = "Enter access PIN"
            )
            return
        }

        val currentLeft = (_adminGateState.value as? AdminGateState.PinChallenge)?.attemptsLeft
            ?: TokenManager.MAX_ADMIN_GATE_ATTEMPTS
        _adminGateState.value = AdminGateState.PinChallenge(attemptsLeft = currentLeft, isVerifying = true)

        viewModelScope.launch {
            val result = authRepository.verifyAdminGate(trimmed)
            result.onSuccess { gateResponse ->
                authRepository.resetAdminGateAttempts()
                val useBiometric = canUseBiometricOnDevice && authRepository.isAdminBiometricEnabled()
                _adminGateState.value = if (useBiometric) {
                    AdminGateState.BiometricChallenge(
                        tier = gateResponse.tier,
                        gateToken = gateResponse.gateToken
                    )
                } else {
                    AdminGateState.Unlocked(
                        tier = gateResponse.tier,
                        gateToken = gateResponse.gateToken
                    )
                }
            }.onFailure { err ->
                if (err is InvalidGatePinException) {
                    val (attemptsLeft, lockedUntil) = authRepository.recordFailedAdminGateAttempt()
                    if (lockedUntil > 0L || attemptsLeft <= 0) {
                        val lockSecs = ((lockedUntil - System.currentTimeMillis()) + 999L) / 1000L
                        startLockoutTicker(lockSecs.coerceAtLeast(1L))
                    } else {
                        _adminGateState.value = AdminGateState.PinChallenge(
                            attemptsLeft = attemptsLeft,
                            error = "${err.message} ($attemptsLeft attempt${if (attemptsLeft == 1) "" else "s"} left)"
                        )
                    }
                } else {
                    _adminGateState.value = AdminGateState.PinChallenge(
                        attemptsLeft = currentLeft,
                        error = err.message ?: "Unable to verify gate PIN"
                    )
                }
            }
        }
    }

    fun onBiometricSuccess() {
        val current = _adminGateState.value
        if (current is AdminGateState.BiometricChallenge) {
            _adminGateState.value = AdminGateState.Unlocked(
                tier = current.tier,
                gateToken = current.gateToken
            )
        }
    }

    fun onBiometricFailed(message: String) {
        _authState.value = AuthState.Error(message)
    }

    fun lockAdminGate() {
        lockoutTickerJob?.cancel()
        _adminGateState.value = AdminGateState.Hidden
        _loginUiState.value = _loginUiState.value.copy(password = "")
    }

    fun loginAdmin(username: String, password: String) {
        val currentGate = _adminGateState.value as? AdminGateState.Unlocked ?: return
        if (username.isBlank() || password.isBlank()) {
            _authState.value = AuthState.Error("Please enter username/email and password")
            return
        }
        _authState.value = AuthState.Loading
        viewModelScope.launch {
            val result = authRepository.login(
                request = LoginRequest(username.trim(), password, gateToken = currentGate.gateToken),
                expectedAdminTier = currentGate.tier
            )
            if (result.isSuccess) {
                lockAdminGate()
                _authState.value = AuthState.Success(result.getOrNull()!!)
            } else {
                _authState.value = AuthState.Error(result.exceptionOrNull()?.message ?: "Invalid credentials")
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
