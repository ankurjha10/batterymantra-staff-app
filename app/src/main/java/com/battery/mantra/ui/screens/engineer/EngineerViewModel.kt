package com.battery.mantra.ui.screens.engineer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.battery.mantra.data.repository.EngineerRepository
import com.battery.mantra.data.repository.EngineerTask
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class EngineerDashboardState {
    object Loading : EngineerDashboardState()
    data class Success(
        val activeJobs: List<EngineerTask>,
        val historyJobs: List<EngineerTask>
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

    fun onTabSelected(index: Int) {
        _selectedTabIndex.value = index
    }

    fun setDutyStatus(isOnDuty: Boolean) {
        _isOnDuty.value = isOnDuty
    }

    init {
        fetchJobs()
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
