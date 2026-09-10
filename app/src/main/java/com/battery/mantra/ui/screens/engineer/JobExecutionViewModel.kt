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

    private val _step = MutableStateFlow(2)
    val step: StateFlow<Int> = _step.asStateFlow()

    private val _otpSent = MutableStateFlow(false)
    val otpSent: StateFlow<Boolean> = _otpSent.asStateFlow()

    private val _isSubmitting = MutableStateFlow(false)
    val isSubmitting: StateFlow<Boolean> = _isSubmitting.asStateFlow()

    private val _qrImageUrl = MutableStateFlow<String?>(null)
    val qrImageUrl: StateFlow<String?> = _qrImageUrl.asStateFlow()

    private val _qrLoading = MutableStateFlow(false)
    val qrLoading: StateFlow<Boolean> = _qrLoading.asStateFlow()

    private val _qrPaymentVerified = MutableStateFlow(false)
    val qrPaymentVerified: StateFlow<Boolean> = _qrPaymentVerified.asStateFlow()

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

    fun generateQrCode(onResult: (String) -> Unit) {
        _qrLoading.value = true
        viewModelScope.launch {
            val result = repository.generateQrCode(orderId)
            _qrLoading.value = false
            if (result.isSuccess) {
                _qrImageUrl.value = result.getOrNull()?.imageUrl
                onResult("QR Code generated successfully!")
            } else {
                onResult(result.exceptionOrNull()?.message ?: "Failed to generate QR code")
            }
        }
    }

    fun verifyQrPayment(onResult: (Boolean, String) -> Unit) {
        _qrLoading.value = true
        viewModelScope.launch {
            val result = repository.checkQrPaymentStatus(orderId)
            _qrLoading.value = false
            if (result.isSuccess) {
                val status = result.getOrNull()
                if (status?.paymentStatus?.uppercase() == "PAID") {
                    _qrPaymentVerified.value = true
                    onResult(true, "Payment verified successfully! ✅")
                } else {
                    onResult(false, "Payment not received yet. Please wait and try again.")
                }
            } else {
                onResult(false, result.exceptionOrNull()?.message ?: "Failed to verify payment")
            }
        }
    }

    suspend fun checkPaymentStatusSilent(): Boolean {
        val result = repository.checkQrPaymentStatus(orderId)
        if (result.isSuccess) {
            val status = result.getOrNull()
            if (status?.paymentStatus?.uppercase() == "PAID") {
                _qrPaymentVerified.value = true
                return true
            }
        }
        return false
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

    fun completeJob(oldBatteryCollected: Boolean, paymentMode: String, otp: String, onResult: (Boolean, String) -> Unit) {
        if (otp.isBlank()) {
            onResult(false, "Please enter the OTP")
            return
        }

        _isSubmitting.value = true
        viewModelScope.launch {
            val request = EngineerCompleteJobRequest(
                otp = otp,
                serialNumber = "N/A",
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
