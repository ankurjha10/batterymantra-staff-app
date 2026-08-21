package com.battery.mantra.ui.screens.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.battery.mantra.data.models.CouponResponse
import com.battery.mantra.data.repository.AdminRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class CouponsUiState(
    val isLoading: Boolean = false,
    val coupons: List<CouponResponse> = emptyList(),
    val error: String? = null
)

class AdminCouponsViewModel(private val repository: AdminRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(CouponsUiState())
    val uiState: StateFlow<CouponsUiState> = _uiState.asStateFlow()

    init {
        fetchCoupons()
    }

    fun fetchCoupons() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            val result = repository.getAdminCoupons()
            if (result.isSuccess) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    coupons = result.getOrDefault(emptyList())
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = result.exceptionOrNull()?.message ?: "Failed to load coupons"
                )
            }
        }
    }

    fun createCoupon(request: com.battery.mantra.data.models.CouponRequest, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            val result = repository.createAdminCoupon(request)
            if (result.isSuccess) {
                fetchCoupons()
                onSuccess()
            } else {
                onError(result.exceptionOrNull()?.message ?: "Failed to create coupon")
            }
        }
    }

    fun updateCoupon(id: String, request: com.battery.mantra.data.models.CouponRequest, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            val result = repository.updateAdminCoupon(id, request)
            if (result.isSuccess) {
                fetchCoupons()
                onSuccess()
            } else {
                onError(result.exceptionOrNull()?.message ?: "Failed to update coupon")
            }
        }
    }

    fun deleteCoupon(id: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            val result = repository.deleteAdminCoupon(id)
            if (result.isSuccess) {
                fetchCoupons()
                onSuccess()
            } else {
                onError(result.exceptionOrNull()?.message ?: "Failed to delete coupon")
            }
        }
    }

    companion object {
        fun provideFactory(repository: AdminRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return AdminCouponsViewModel(repository) as T
                }
            }
    }
}
