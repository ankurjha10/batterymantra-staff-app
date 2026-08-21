package com.battery.mantra.ui.screens.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.battery.mantra.data.models.EnquiryResponse
import com.battery.mantra.data.repository.AdminRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class EnquiriesUiState(
    val isLoading: Boolean = false,
    val enquiries: List<EnquiryResponse> = emptyList(),
    val error: String? = null,
    val selectedFilter: String = "All"
)

class AdminEnquiriesViewModel(private val repository: AdminRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(EnquiriesUiState())
    val uiState: StateFlow<EnquiriesUiState> = _uiState.asStateFlow()

    init {
        loadEnquiries()
    }

    private fun loadEnquiries() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            val typeFilter = when (_uiState.value.selectedFilter) {
                "Quotation" -> "QUOTATION"
                "Corporate" -> "CORPORATE"
                else -> null
            }
            val result = repository.getAllEnquiries(typeFilter)
            if (result.isSuccess) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    enquiries = result.getOrDefault(emptyList())
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = result.exceptionOrNull()?.message ?: "Failed to load enquiries"
                )
            }
        }
    }

    fun setFilter(type: String) {
        _uiState.value = _uiState.value.copy(selectedFilter = type)
        loadEnquiries()
    }

    fun updateEnquiryStatus(id: Long, status: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            val result = repository.updateEnquiryStatus(id, status)
            if (result.isSuccess) {
                loadEnquiries()
                onSuccess()
            } else {
                onError(result.exceptionOrNull()?.message ?: "Failed to update enquiry status")
            }
        }
    }

    companion object {
        fun provideFactory(repository: AdminRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return AdminEnquiriesViewModel(repository) as T
                }
            }
    }
}
