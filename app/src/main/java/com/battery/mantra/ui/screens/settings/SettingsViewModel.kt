package com.battery.mantra.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.battery.mantra.data.models.PasswordChangeRequest
import com.battery.mantra.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class PasswordChangeState {
    object Idle : PasswordChangeState()
    object Loading : PasswordChangeState()
    object Success : PasswordChangeState()
    data class Error(val message: String) : PasswordChangeState()
}

class SettingsViewModel(private val authRepository: AuthRepository) : ViewModel() {
    private val _passwordChangeState = MutableStateFlow<PasswordChangeState>(PasswordChangeState.Idle)
    val passwordChangeState: StateFlow<PasswordChangeState> = _passwordChangeState.asStateFlow()

    fun changePassword(request: PasswordChangeRequest) {
        viewModelScope.launch {
            _passwordChangeState.value = PasswordChangeState.Loading
            val result = authRepository.changePassword(request)
            if (result.isSuccess) {
                _passwordChangeState.value = PasswordChangeState.Success
            } else {
                _passwordChangeState.value = PasswordChangeState.Error(result.exceptionOrNull()?.message ?: "Unknown error")
            }
        }
    }

    fun resetPasswordState() {
        _passwordChangeState.value = PasswordChangeState.Idle
    }

    companion object {
        fun provideFactory(authRepository: AuthRepository): androidx.lifecycle.ViewModelProvider.Factory =
            object : androidx.lifecycle.ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return SettingsViewModel(authRepository) as T
                }
            }
    }
}
