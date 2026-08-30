package com.battery.mantra.ui.screens.engineer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.battery.mantra.data.repository.EngineerRepository
import com.battery.mantra.data.models.UserResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class EngineerDashboardState {
    object Loading : EngineerDashboardState()
    data class Success(
        val activeJobs: List<com.battery.mantra.data.models.OrderResponse>,
        val historyJobs: List<com.battery.mantra.data.models.OrderResponse>
    ) : EngineerDashboardState()
    data class Error(val message: String) : EngineerDashboardState()
}

class EngineerViewModel(private val repository: EngineerRepository) : ViewModel() {
    private val _uiState = MutableStateFlow<EngineerDashboardState>(EngineerDashboardState.Loading)
    val uiState: StateFlow<EngineerDashboardState> = _uiState.asStateFlow()

    private val _selectedTabIndex = MutableStateFlow(1)
    val selectedTabIndex: StateFlow<Int> = _selectedTabIndex.asStateFlow()

    private val _isOnDuty = MutableStateFlow(true)
    val isOnDuty: StateFlow<Boolean> = _isOnDuty.asStateFlow()

    private val _profileState = MutableStateFlow<UserResponse?>(null)
    val profileState: StateFlow<UserResponse?> = _profileState.asStateFlow()

    fun onTabSelected(index: Int) {
        _selectedTabIndex.value = index
    }

    fun setDutyStatus(isOnDuty: Boolean) {
        viewModelScope.launch {
            val result = repository.updateDutyStatus(isOnDuty)
            if (result.isSuccess) {
                _isOnDuty.value = result.getOrNull()?.isActive ?: isOnDuty
            }
        }
    }

    init {
        fetchProfile()
        fetchJobs()
    }

    private fun fetchProfile() {
        viewModelScope.launch {
            val result = repository.getProfile()
            if (result.isSuccess) {
                val profile = result.getOrNull()
                _profileState.value = profile
                _isOnDuty.value = profile?.isActive ?: false
            }
        }
    }

    fun fetchJobs() {
        _uiState.value = EngineerDashboardState.Loading
        viewModelScope.launch {
            val activeJobsResult = repository.getActiveJobs()
            val historyJobsResult = repository.getHistoryJobs()

            if (activeJobsResult.isSuccess && historyJobsResult.isSuccess) {
                _uiState.value = EngineerDashboardState.Success(
                    activeJobs = activeJobsResult.getOrDefault(emptyList()),
                    historyJobs = historyJobsResult.getOrDefault(emptyList())
                )
            } else {
                _uiState.value = EngineerDashboardState.Error(
                    activeJobsResult.exceptionOrNull()?.message 
                        ?: historyJobsResult.exceptionOrNull()?.message 
                        ?: "Failed to load jobs"
                )
            }
        }
    }

    fun logCall(orderId: String) {
        viewModelScope.launch {
            repository.logCall(orderId)
        }
    }

    private val _attendance = MutableStateFlow<List<com.battery.mantra.data.models.AttendanceResponse>>(emptyList())
    val attendance: StateFlow<List<com.battery.mantra.data.models.AttendanceResponse>> = _attendance.asStateFlow()

    private val _leaves = MutableStateFlow<List<com.battery.mantra.data.models.LeaveRequestResponse>>(emptyList())
    val leaves: StateFlow<List<com.battery.mantra.data.models.LeaveRequestResponse>> = _leaves.asStateFlow()

    fun fetchAttendance() {
        viewModelScope.launch {
            val result = repository.getAttendance()
            if (result.isSuccess) {
                _attendance.value = result.getOrDefault(emptyList())
            }
        }
    }

    fun fetchLeaves() {
        viewModelScope.launch {
            val result = repository.getMyLeaves()
            if (result.isSuccess) {
                _leaves.value = result.getOrDefault(emptyList())
            }
        }
    }

    fun checkIn() {
        viewModelScope.launch {
            val result = repository.checkIn()
            if (result.isSuccess) {
                fetchAttendance()
            }
        }
    }

    fun checkOut() {
        viewModelScope.launch {
            val result = repository.checkOut()
            if (result.isSuccess) {
                fetchAttendance()
            }
        }
    }

    fun applyLeave(startDate: String, endDate: String, reason: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            val result = repository.applyLeave(startDate, endDate, reason)
            if (result.isSuccess) {
                fetchLeaves()
                onSuccess()
            } else {
                onError(result.exceptionOrNull()?.message ?: "Failed to apply leave")
            }
        }
    }

    companion object {
        fun provideFactory(repository: EngineerRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return EngineerViewModel(repository) as T
                }
            }
    }
}
