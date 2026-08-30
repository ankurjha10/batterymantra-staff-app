package com.battery.mantra.ui.screens.engineer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.battery.mantra.data.models.EngineerCompleteJobRequest
import com.battery.mantra.data.models.OrderResponse
import com.battery.mantra.data.repository.EngineerRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class JobExecutionState {
    object Loading : JobExecutionState()
    data class Success(val order: OrderResponse) : JobExecutionState()
    data class Error(val message: String) : JobExecutionState()
}

class JobExecutionViewModel(
    private val repository: EngineerRepository,
    private val orderId: String
) : ViewModel() {

    private val _uiState = MutableStateFlow<JobExecutionState>(JobExecutionState.Loading)
    val uiState: StateFlow<JobExecutionState> = _uiState.asStateFlow()

    private val _step = MutableStateFlow(1)
    val step: StateFlow<Int> = _step.asStateFlow()

    private val _otpSent = MutableStateFlow(false)
    val otpSent: StateFlow<Boolean> = _otpSent.asStateFlow()

    private val _isSubmitting = MutableStateFlow(false)
    val isSubmitting: StateFlow<Boolean> = _isSubmitting.asStateFlow()

    init {
        loadOrderDetails()
    }

    private fun loadOrderDetails() {
        _uiState.value = JobExecutionState.Loading
        viewModelScope.launch {
            val result = repository.getActiveJobs()
            if (result.isSuccess) {
                val orders = result.getOrDefault(emptyList())
                val order = orders.find { it.orderId == orderId }
                if (order != null) {
                    _uiState.value = JobExecutionState.Success(order)
                } else {
                    _uiState.value = JobExecutionState.Error("Order not found or not active")
                }
            } else {
                _uiState.value = JobExecutionState.Error(result.exceptionOrNull()?.message ?: "Failed to load order")
            }
        }
    }

    fun setStep(newStep: Int) {
        _step.value = newStep
    }

    fun sendOtp(onResult: (String) -> Unit) {
        viewModelScope.launch {
            val result = repository.sendCompletionOtp(orderId)
            if (result.isSuccess) {
                _otpSent.value = true
                onResult("OTP sent successfully")
            } else {
                onResult(result.exceptionOrNull()?.message ?: "Failed to send OTP")
            }
        }
    }

    fun completeJob(serialNumber: String, oldBatteryCollected: Boolean, paymentMode: String, otp: String, onResult: (Boolean, String) -> Unit) {
        if (otp.isBlank() || serialNumber.isBlank()) {
            onResult(false, "Please fill in all required fields")
            return
        }

        _isSubmitting.value = true
        viewModelScope.launch {
            val request = EngineerCompleteJobRequest(
                otp = otp,
                serialNumber = serialNumber,
                oldBatteryCollected = oldBatteryCollected,
                paymentMode = paymentMode
            )
            val result = repository.completeJob(orderId, request)
            _isSubmitting.value = false
            
            if (result.isSuccess) {
                onResult(true, "Job completed successfully")
            } else {
                onResult(false, result.exceptionOrNull()?.message ?: "Failed to complete job")
            }
        }
    }

    companion object {
        fun provideFactory(
            repository: EngineerRepository,
            orderId: String
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return JobExecutionViewModel(repository, orderId) as T
            }
        }
    }
}
