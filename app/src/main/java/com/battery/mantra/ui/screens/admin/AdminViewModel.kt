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

sealed class AdminDataState<out T> {
    object Idle : AdminDataState<Nothing>()
    object Loading : AdminDataState<Nothing>()
    data class Success<out T>(val data: T) : AdminDataState<T>()
    data class Error(val message: String) : AdminDataState<Nothing>()
}

class AdminViewModel(private val repository: AdminRepository, private val tokenManager: TokenManager) : ViewModel() {

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _pendingCallbacksCount = MutableStateFlow(0)
    val pendingCallbacksCount: StateFlow<Int> = _pendingCallbacksCount.asStateFlow()

    private val _pendingEnquiriesCount = MutableStateFlow(0)
    val pendingEnquiriesCount: StateFlow<Int> = _pendingEnquiriesCount.asStateFlow()

    private val _pendingLeavesCount = MutableStateFlow(0)
    val pendingLeavesCount: StateFlow<Int> = _pendingLeavesCount.asStateFlow()

    private val _ordersState = MutableStateFlow<AdminDataState<List<OrderResponse>>>(AdminDataState.Idle)
    val ordersState: StateFlow<AdminDataState<List<OrderResponse>>> = _ordersState.asStateFlow()

    private val _partnersState = MutableStateFlow<AdminDataState<List<PartnerResponse>>>(AdminDataState.Idle)
    fun updateOrderStatus(orderId: String, status: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            repository.updateOrderStatus(orderId, status)
                .onSuccess {
                    fetchOrders()
                    onSuccess()
                }
                .onFailure {
                    onError(it.message ?: "An error occurred")
                }
        }
    }

    val partnersState: StateFlow<AdminDataState<List<PartnerResponse>>> = _partnersState.asStateFlow()

    private val _engineersState = MutableStateFlow<AdminDataState<List<EngineerResponse>>>(AdminDataState.Idle)
    val engineersState: StateFlow<AdminDataState<List<EngineerResponse>>> = _engineersState.asStateFlow()

    private val _usersState = MutableStateFlow<AdminDataState<List<UserResponse>>>(AdminDataState.Loading)
    val usersState: StateFlow<AdminDataState<List<UserResponse>>> = _usersState.asStateFlow()

    private val _citiesState = MutableStateFlow<AdminDataState<List<com.battery.mantra.data.models.CityResponse>>>(AdminDataState.Loading)
    val citiesState: StateFlow<AdminDataState<List<com.battery.mantra.data.models.CityResponse>>> = _citiesState.asStateFlow()

    private val _notificationsState = MutableStateFlow<AdminDataState<List<com.battery.mantra.data.models.NotificationResponse>>>(AdminDataState.Idle)
    val notificationsState: StateFlow<AdminDataState<List<com.battery.mantra.data.models.NotificationResponse>>> = _notificationsState.asStateFlow()

    private val _leavesState = MutableStateFlow<AdminDataState<List<com.battery.mantra.data.models.LeaveRequestResponse>>>(AdminDataState.Loading)
    val leavesState: StateFlow<AdminDataState<List<com.battery.mantra.data.models.LeaveRequestResponse>>> = _leavesState.asStateFlow()

    private val _selectedTabIndex = MutableStateFlow(0)
    val selectedTabIndex: StateFlow<Int> = _selectedTabIndex.asStateFlow()

    private val _productSearchResults = MutableStateFlow<AdminDataState<List<com.battery.mantra.data.models.ProductResponse>>>(AdminDataState.Idle)
    val productSearchResults: StateFlow<AdminDataState<List<com.battery.mantra.data.models.ProductResponse>>> = _productSearchResults.asStateFlow()

    fun searchProducts(keyword: String) {
        if (keyword.isBlank()) {
            _productSearchResults.value = AdminDataState.Idle
            return
        }
        viewModelScope.launch {
            _productSearchResults.value = AdminDataState.Loading
            repository.getProducts(page = 0, size = 50, brandId = null, keyword = keyword)
                .onSuccess {
                    _productSearchResults.value = AdminDataState.Success(it.content)
                }
                .onFailure {
                    _productSearchResults.value = AdminDataState.Error(it.message ?: "Failed to search products")
                }
        }
    }

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
        fetchPendingCounts()
        updateFcmTokenFromFirebase()
        
        // Listen for real-time updates from Push Notifications
        viewModelScope.launch {
            com.battery.mantra.util.AppEventBus.refreshEvents.collect {
                fetchOrders()
                fetchNotifications()
                fetchPendingCounts()
            }
        }
    }

    fun refreshAllData() {
        viewModelScope.launch {
            _isRefreshing.value = true
            fetchOrders()
            fetchPartners()
            fetchEngineers()
            fetchUsers()
            fetchCities()
            fetchNotifications()
            fetchLeaveRequests()
            fetchPendingCounts()
            kotlinx.coroutines.delay(600)
            _isRefreshing.value = false
        }
    }

    fun fetchPendingCounts() {
        viewModelScope.launch {
            repository.getAllCallbacks().onSuccess { callbacks ->
                _pendingCallbacksCount.value = callbacks.count { !it.status.equals("RESOLVED", ignoreCase = true) }
            }
        }
        viewModelScope.launch {
            repository.getAllEnquiries().onSuccess { enquiries ->
                _pendingEnquiriesCount.value = enquiries.count { !it.status.equals("RESOLVED", ignoreCase = true) }
            }
        }
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
                val allNotifications = result.getOrDefault(emptyList())
                _notificationsState.value = AdminDataState.Success(allNotifications)
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
                _notificationsState.value = AdminDataState.Error(result.exceptionOrNull()?.message ?: "Failed to clear notifications")
            }
        }
    }

    fun assignPartner(orderId: String, partnerId: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            repository.assignPartner(orderId, partnerId).fold(
                onSuccess = {
                    fetchOrders() // refresh orders
                    onSuccess()
                },
                onFailure = { err ->
                    onError(err.message ?: "Failed to assign partner")
                }
            )
        }
    }

    fun assignEngineer(orderId: String, engineerId: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            repository.assignEngineer(orderId, engineerId).fold(
                onSuccess = {
                    fetchOrders() // refresh orders
                    onSuccess()
                },
                onFailure = { err ->
                    onError(err.message ?: "Failed to assign engineer")
                }
            )
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
        _ordersState.value = AdminDataState.Loading
        fetchOrders()
        
        _partnersState.value = AdminDataState.Loading
        fetchPartners()
        
        _engineersState.value = AdminDataState.Loading
        fetchEngineers()
        
        _usersState.value = AdminDataState.Loading
        fetchUsers()
        
        _citiesState.value = AdminDataState.Loading
        fetchCities()

        _notificationsState.value = AdminDataState.Loading
        fetchNotifications()

        _leavesState.value = AdminDataState.Loading
        fetchLeaveRequests()
    }

    private fun fetchLeaveRequests() {
        viewModelScope.launch {
            repository.getAllLeaveRequests()
                .onSuccess {
                    _leavesState.value = AdminDataState.Success(it)
                    _pendingLeavesCount.value = it.count { leave -> leave.status.equals("PENDING", ignoreCase = true) }
                }
                .onFailure {
                    _leavesState.value = AdminDataState.Error(it.message ?: "Failed to load leave requests")
                }
        }
    }

    fun updateLeaveStatus(id: String, status: String) {
        viewModelScope.launch {
            repository.updateLeaveStatus(id, status)
                .onSuccess { fetchLeaveRequests() }
        }
    }

    fun loadInventory(engineerId: String, productId: String, quantity: Int, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            repository.loadInventory(engineerId, productId, quantity)
                .onSuccess { onSuccess() }
                .onFailure { onError(it.message ?: "Failed to load inventory") }
        }
    }

    fun unloadInventory(engineerId: String, productId: String, quantity: Int, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            repository.unloadInventory(engineerId, productId, quantity)
                .onSuccess { onSuccess() }
                .onFailure { onError(it.message ?: "Failed to unload inventory") }
        }
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

    fun updatePartner(id: String, request: com.battery.mantra.data.models.CreatePartnerRequest, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            val result = repository.updatePartner(id, request)
            if (result.isSuccess) {
                fetchPartners()
                fetchUsers()
                onSuccess()
            } else {
                onError(result.exceptionOrNull()?.message ?: "Failed to update partner")
            }
        }
    }

    fun togglePartnerStatus(partner: PartnerResponse, isActive: Boolean, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            val result = repository.togglePartnerStatus(partner, isActive)
            if (result.isSuccess) {
                fetchPartners()
                fetchUsers()
                onSuccess()
            } else {
                onError(result.exceptionOrNull()?.message ?: "Failed to update partner status")
            }
        }
    }

    fun togglePartnerStatus(id: String, userId: String?, isActive: Boolean, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            val result = repository.togglePartnerStatus(id, userId, isActive)
            if (result.isSuccess) {
                fetchPartners()
                fetchUsers()
                onSuccess()
            } else {
                onError(result.exceptionOrNull()?.message ?: "Failed to update partner status")
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

    fun updateEngineer(id: String, request: com.battery.mantra.data.models.CreateEngineerRequest, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            val result = repository.updateEngineer(id, request)
            if (result.isSuccess) {
                fetchEngineers()
                fetchUsers()
                onSuccess()
            } else {
                onError(result.exceptionOrNull()?.message ?: "Failed to update engineer")
            }
        }
    }

    fun toggleEngineerStatus(engineer: EngineerResponse, isActive: Boolean, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            val result = repository.toggleEngineerStatus(engineer, isActive)
            if (result.isSuccess) {
                fetchEngineers()
                fetchUsers()
                onSuccess()
            } else {
                onError(result.exceptionOrNull()?.message ?: "Failed to update engineer status")
            }
        }
    }

    fun toggleEngineerStatus(id: String, userId: String?, isActive: Boolean, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            val result = repository.toggleEngineerStatus(id, userId, isActive)
            if (result.isSuccess) {
                fetchEngineers()
                fetchUsers()
                onSuccess()
            } else {
                onError(result.exceptionOrNull()?.message ?: "Failed to update engineer status")
            }
        }
    }

    fun createCustomer(
        request: com.battery.mantra.data.models.AdminCreateCustomerRequest,
        onSuccess: (com.battery.mantra.data.models.UserResponse) -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            repository.createCustomer(request)
                .onSuccess { user ->
                    fetchUsers()
                    onSuccess(user)
                }
                .onFailure { onError(it.message ?: "Failed to create customer") }
        }
    }

    fun updateCustomer(
        userId: String,
        request: com.battery.mantra.data.models.AdminUpdateCustomerRequest,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            repository.updateCustomer(userId, request)
                .onSuccess {
                    fetchUsers()
                    onSuccess()
                }
                .onFailure { onError(it.message ?: "Failed to update customer") }
        }
    }

    fun createAdminOrder(
        request: com.battery.mantra.data.models.AdminCreateOrderRequest,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            repository.createAdminOrder(request)
                .onSuccess {
                    fetchOrders()
                    onSuccess()
                }
                .onFailure { onError(it.message ?: "Failed to create order") }
        }
    }

    fun updateOrder(
        orderId: String,
        request: com.battery.mantra.data.models.AdminUpdateOrderRequest,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            repository.updateOrder(orderId, request)
                .onSuccess {
                    fetchOrders()
                    onSuccess()
                }
                .onFailure { onError(it.message ?: "Failed to update order") }
        }
    }


    fun createSubAdmin(
        request: com.battery.mantra.data.models.AdminCreateSubAdminRequest,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            repository.createSubAdmin(request)
                .onSuccess {
                    fetchUsers() // Refresh users list which includes sub-admins
                    onSuccess()
                }
                .onFailure { onError(it.message ?: "Failed to create sub-admin") }
        }
    }

    fun updateSubAdmin(userId: String, request: com.battery.mantra.data.models.AdminUpdateSubAdminRequest, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            repository.updateSubAdmin(userId, request)
                .onSuccess {
                    fetchUsers()
                    onSuccess()
                }
                .onFailure { onError(it.message ?: "Failed to update sub-admin") }
        }
    }

    fun deleteUser(userId: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            repository.deleteUser(userId)
                .onSuccess {
                    fetchUsers()
                    onSuccess()
                }
                .onFailure { onError(it.message ?: "Failed to delete user") }
        }
    }

    fun toggleUserStatus(userId: String, isActive: Boolean, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            repository.toggleUserStatus(userId, isActive)
                .onSuccess {
                    fetchUsers()
                    onSuccess()
                }
                .onFailure { onError(it.message ?: "Failed to update status") }
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
