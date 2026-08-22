package com.battery.mantra.ui.screens.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.battery.mantra.data.models.EngineerResponse
import com.battery.mantra.data.models.OrderResponse
import com.battery.mantra.data.models.PartnerResponse
import com.battery.mantra.data.models.UserResponse
import com.battery.mantra.data.repository.AdminRepository
import com.battery.mantra.data.local.TokenManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.ZoneId

sealed class AdminDataState<out T> {
    object Idle : AdminDataState<Nothing>()
    object Loading : AdminDataState<Nothing>()
    data class Success<out T>(val data: T) : AdminDataState<T>()
    data class Error(val message: String) : AdminDataState<Nothing>()
}

class AdminViewModel(private val repository: AdminRepository, private val tokenManager: TokenManager) : ViewModel() {

    private val _ordersState = MutableStateFlow<AdminDataState<List<OrderResponse>>>(AdminDataState.Idle)
    val ordersState: StateFlow<AdminDataState<List<OrderResponse>>> = _ordersState.asStateFlow()

    private val _partnersState = MutableStateFlow<AdminDataState<List<PartnerResponse>>>(AdminDataState.Idle)
    val partnersState: StateFlow<AdminDataState<List<PartnerResponse>>> = _partnersState.asStateFlow()

    private val _engineersState = MutableStateFlow<AdminDataState<List<EngineerResponse>>>(AdminDataState.Idle)
    val engineersState: StateFlow<AdminDataState<List<EngineerResponse>>> = _engineersState.asStateFlow()

    private val _usersState = MutableStateFlow<AdminDataState<List<UserResponse>>>(AdminDataState.Loading)
    val usersState: StateFlow<AdminDataState<List<UserResponse>>> = _usersState.asStateFlow()

    private val _citiesState = MutableStateFlow<AdminDataState<List<com.battery.mantra.data.models.CityResponse>>>(AdminDataState.Loading)
    val citiesState: StateFlow<AdminDataState<List<com.battery.mantra.data.models.CityResponse>>> = _citiesState.asStateFlow()

    private val _notificationsState = MutableStateFlow<AdminDataState<List<com.battery.mantra.data.models.NotificationResponse>>>(AdminDataState.Idle)
    val notificationsState: StateFlow<AdminDataState<List<com.battery.mantra.data.models.NotificationResponse>>> = _notificationsState.asStateFlow()

    private val _selectedTabIndex = MutableStateFlow(0)
    val selectedTabIndex: StateFlow<Int> = _selectedTabIndex.asStateFlow()

    // Deep Link State for Orders
    private val _targetOrderSearchQuery = MutableStateFlow<String?>(null)
    val targetOrderSearchQuery: StateFlow<String?> = _targetOrderSearchQuery.asStateFlow()

    private val _targetOrderFilter = MutableStateFlow<String?>(null)
    val targetOrderFilter: StateFlow<String?> = _targetOrderFilter.asStateFlow()

    fun onTabSelected(index: Int) {
        _selectedTabIndex.value = index
    }

    fun navigateToOrder(orderId: String, filter: String = "New Orders") {
        _selectedTabIndex.value = 1 // Orders tab
        _targetOrderSearchQuery.value = orderId
        _targetOrderFilter.value = filter
    }

    fun clearTargetOrderState() {
        _targetOrderSearchQuery.value = null
        _targetOrderFilter.value = null
    }

    init {
        fetchAllData()
        updateFcmTokenFromFirebase()
    }

    private fun updateFcmTokenFromFirebase() {
        com.google.firebase.messaging.FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val token = task.result
                updateFcmTokenOnServer(token)
            }
        }
    }

    private fun updateFcmTokenOnServer(token: String) {
        viewModelScope.launch {
            repository.updateFcmToken(token)
            // Silently fail/succeed since it's a background sync
        }
    }

    fun fetchNotifications() {
        _notificationsState.value = AdminDataState.Loading
        viewModelScope.launch {
            val result = repository.getNotifications()
            if (result.isSuccess) {
                val clearedTime = tokenManager.getNotificationsClearedTime()
                val allNotifications = result.getOrDefault(emptyList())
                val filteredNotifications = allNotifications.filter {
                    try {
                        // Attempt to parse standard ISO 8601 string
                        val formatter = DateTimeFormatter.ISO_DATE_TIME
                        val timeMillis = LocalDateTime.parse(it.createdAt, formatter)
                            .atZone(ZoneId.systemDefault())
                            .toInstant()
                            .toEpochMilli()
                        timeMillis > clearedTime
                    } catch (e: Exception) {
                        // Fallback if parsing fails, just show it
                        true
                    }
                }
                _notificationsState.value = AdminDataState.Success(filteredNotifications)
            } else {
                _notificationsState.value = AdminDataState.Error(result.exceptionOrNull()?.message ?: "Failed to fetch notifications")
            }
        }
    }

    fun clearAllNotifications() {
        viewModelScope.launch {
            val result = repository.clearNotifications()
            if (result.isSuccess) {
                _notificationsState.value = AdminDataState.Success(emptyList())
            } else {
                // If the backend doesn't support it yet, we just fall back to local clear (the user's request)
                tokenManager.setNotificationsClearedTime(System.currentTimeMillis())
                _notificationsState.value = AdminDataState.Success(emptyList())
            }
        }
    }

    fun deleteNotification(id: String) {
        val currentState = _notificationsState.value
        if (currentState is AdminDataState.Success) {
            // Optimistically update UI
            val updatedList = currentState.data.filter { it.id != id }
            _notificationsState.value = AdminDataState.Success(updatedList)
            
            viewModelScope.launch {
                val result = repository.deleteNotification(id)
                if (result.isFailure) {
                    // Revert on failure
                    _notificationsState.value = currentState
                }
            }
        }
    }

    private fun fetchAllData() {
        fetchOrders()
        fetchPartners()
        fetchEngineers()
        fetchUsers()
        fetchCities()
        fetchNotifications()
    }

    fun fetchCities() {
        _citiesState.value = AdminDataState.Loading
        viewModelScope.launch {
            val result = repository.getCities()
            if (result.isSuccess) {
                _citiesState.value = AdminDataState.Success(result.getOrDefault(emptyList()))
            } else {
                _citiesState.value = AdminDataState.Error(result.exceptionOrNull()?.message ?: "Failed to fetch cities")
            }
        }
    }

    fun fetchOrders() {
        _ordersState.value = AdminDataState.Loading
        viewModelScope.launch {
            val result = repository.getOrders()
            if (result.isSuccess) {
                _ordersState.value = AdminDataState.Success(result.getOrDefault(emptyList()))
            } else {
                _ordersState.value = AdminDataState.Error(result.exceptionOrNull()?.message ?: "Failed to fetch orders")
            }
        }
    }

    fun fetchPartners() {
        _partnersState.value = AdminDataState.Loading
        viewModelScope.launch {
            val result = repository.getPartners()
            if (result.isSuccess) {
                _partnersState.value = AdminDataState.Success(result.getOrDefault(emptyList()))
            } else {
                _partnersState.value = AdminDataState.Error(result.exceptionOrNull()?.message ?: "Failed to fetch partners")
            }
        }
    }

    fun fetchEngineers() {
        _engineersState.value = AdminDataState.Loading
        viewModelScope.launch {
            val result = repository.getEngineers()
            if (result.isSuccess) {
                _engineersState.value = AdminDataState.Success(result.getOrDefault(emptyList()))
            } else {
                _engineersState.value = AdminDataState.Error(result.exceptionOrNull()?.message ?: "Failed to fetch engineers")
            }
        }
    }

    fun fetchUsers() {
        _usersState.value = AdminDataState.Loading
        viewModelScope.launch {
            val result = repository.getUsers()
            if (result.isSuccess) {
                _usersState.value = AdminDataState.Success(result.getOrDefault(emptyList()))
            } else {
                _usersState.value = AdminDataState.Error(result.exceptionOrNull()?.message ?: "Failed to fetch users")
            }
        }
    }

    fun createPartner(request: com.battery.mantra.data.models.CreatePartnerRequest, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            val result = repository.createPartner(request)
            if (result.isSuccess) {
                fetchPartners()
                onSuccess()
            } else {
                onError(result.exceptionOrNull()?.message ?: "Failed to create partner")
            }
        }
    }

    fun createEngineer(request: com.battery.mantra.data.models.CreateEngineerRequest, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            val result = repository.createEngineer(request)
            if (result.isSuccess) {
                fetchEngineers()
                onSuccess()
            } else {
                onError(result.exceptionOrNull()?.message ?: "Failed to create engineer")
            }
        }
    }

    companion object {
        fun provideFactory(
            repository: AdminRepository,
            tokenManager: TokenManager
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return AdminViewModel(repository, tokenManager) as T
            }
        }
    }
}
