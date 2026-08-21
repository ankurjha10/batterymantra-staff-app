package com.battery.mantra.ui.screens.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.battery.mantra.data.models.CallbackResponse
import com.battery.mantra.data.repository.AdminRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class CallbacksUiState(
    val isLoading: Boolean = false,
    val callbacks: List<CallbackResponse> = emptyList(),
    val error: String? = null
)

class AdminCallbacksViewModel(private val repository: AdminRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(CallbacksUiState())
    val uiState: StateFlow<CallbacksUiState> = _uiState.asStateFlow()

    init {
        fetchCallbacks()
    }

    fun fetchCallbacks() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            val result = repository.getAllCallbacks()
            if (result.isSuccess) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    callbacks = result.getOrDefault(emptyList())
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = result.exceptionOrNull()?.message ?: "Failed to load callbacks"
                )
            }
        }
    }

    fun updateCallbackStatus(callbackId: Long, status: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            val result = repository.updateCallbackStatus(callbackId, status)
            if (result.isSuccess) {
                fetchCallbacks()
                onSuccess()
            } else {
                onError(result.exceptionOrNull()?.message ?: "Failed to update callback status")
            }
        }
    }

    companion object {
        fun provideFactory(repository: AdminRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return AdminCallbacksViewModel(repository) as T
                }
            }
    }
}
