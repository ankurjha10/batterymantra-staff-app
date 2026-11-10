package com.battery.mantra.ui.screens.partner

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.battery.mantra.data.models.BrandResponse
import com.battery.mantra.data.models.CategoryResponse
import com.battery.mantra.data.models.CityPricingRequestDto
import com.battery.mantra.data.models.CityPricingRequestResponse
import com.battery.mantra.data.models.CityProductStockItem
import com.battery.mantra.data.models.CreateEngineerRequest
import com.battery.mantra.data.models.EngineerResponse
import com.battery.mantra.data.models.NotificationResponse
import com.battery.mantra.data.models.OrderResponse
import com.battery.mantra.data.models.PartnerBulkInventoryUpdateRequest
import com.battery.mantra.data.models.PartnerCityPricingUpdateDto
import com.battery.mantra.data.models.PartnerCreateProductRequest
import com.battery.mantra.data.models.PartnerInventoryItemResponse
import com.battery.mantra.data.models.PartnerResponse
import com.battery.mantra.data.repository.PartnerRepository
import com.battery.mantra.util.AppEventBus
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class PartnerDataState<out T> {
    object Idle : PartnerDataState<Nothing>()
    object Loading : PartnerDataState<Nothing>()
    data class Success<out T>(val data: T) : PartnerDataState<T>()
    data class Error(val message: String) : PartnerDataState<Nothing>()
}

class PartnerViewModel(private val repository: PartnerRepository) : ViewModel() {

    private val _selectedTabIndex = MutableStateFlow(0)
    val selectedTabIndex: StateFlow<Int> = _selectedTabIndex.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _profileState = MutableStateFlow<PartnerDataState<PartnerResponse>>(PartnerDataState.Loading)
    val profileState: StateFlow<PartnerDataState<PartnerResponse>> = _profileState.asStateFlow()

    private val _ordersState = MutableStateFlow<PartnerDataState<List<OrderResponse>>>(PartnerDataState.Loading)
    val ordersState: StateFlow<PartnerDataState<List<OrderResponse>>> = _ordersState.asStateFlow()

    private val _engineersState = MutableStateFlow<PartnerDataState<List<EngineerResponse>>>(PartnerDataState.Loading)
    val engineersState: StateFlow<PartnerDataState<List<EngineerResponse>>> = _engineersState.asStateFlow()

    private val _selectedCityId = MutableStateFlow<String?>(null)
    val selectedCityId: StateFlow<String?> = _selectedCityId.asStateFlow()

    private val _inventoryState = MutableStateFlow<PartnerDataState<List<PartnerInventoryItemResponse>>>(PartnerDataState.Idle)
    val inventoryState: StateFlow<PartnerDataState<List<PartnerInventoryItemResponse>>> = _inventoryState.asStateFlow()

    private val _pendingPricingRequestsState = MutableStateFlow<PartnerDataState<List<CityPricingRequestResponse>>>(PartnerDataState.Idle)
    val pendingPricingRequestsState: StateFlow<PartnerDataState<List<CityPricingRequestResponse>>> = _pendingPricingRequestsState.asStateFlow()

    private val _brands = MutableStateFlow<List<BrandResponse>>(emptyList())
    val brands: StateFlow<List<BrandResponse>> = _brands.asStateFlow()

    private val _categories = MutableStateFlow<List<CategoryResponse>>(emptyList())
    val categories: StateFlow<List<CategoryResponse>> = _categories.asStateFlow()

    private val _notificationsState = MutableStateFlow<PartnerDataState<List<NotificationResponse>>>(PartnerDataState.Idle)
    val notificationsState: StateFlow<PartnerDataState<List<NotificationResponse>>> = _notificationsState.asStateFlow()

    private val _targetOrderFilter = MutableStateFlow<String?>(null)
    val targetOrderFilter: StateFlow<String?> = _targetOrderFilter.asStateFlow()

    private val _targetOrderSearch = MutableStateFlow<String?>(null)
    val targetOrderSearch: StateFlow<String?> = _targetOrderSearch.asStateFlow()

    init {
        fetchInitialData()
        syncFcmToken()
        viewModelScope.launch {
            AppEventBus.refreshEvents.collect {
                fetchOrders()
                fetchNotifications()
                _selectedCityId.value?.let { fetchInventory(it) }
            }
        }
    }

    private fun fetchInitialData() {
        fetchProfile()
        fetchOrders()
        fetchEngineers()
        fetchPendingPricingRequests()
        fetchCatalogMetadata()
        fetchNotifications()
    }

    private fun syncFcmToken() {
        try {
            FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val token = task.result
                    if (!token.isNullOrBlank()) {
                        viewModelScope.launch { repository.updateFcmToken(token) }
                    }
                }
            }
        } catch (_: Exception) {
            // Firebase may not be initialized in tests/emulators
        }
    }

    fun onTabSelected(index: Int) {
        _selectedTabIndex.value = index
    }

    fun navigateToOrders(filter: String? = null, orderIdSearch: String? = null) {
        _targetOrderFilter.value = filter
        _targetOrderSearch.value = orderIdSearch
        _selectedTabIndex.value = 1
    }

    fun consumeOrderTarget() {
        _targetOrderFilter.value = null
        _targetOrderSearch.value = null
    }

    fun refreshAll() {
        viewModelScope.launch {
            _isRefreshing.value = true
            fetchProfile()
            fetchOrders()
            fetchEngineers()
            fetchPendingPricingRequests()
            fetchCatalogMetadata()
            fetchNotifications()
            _selectedCityId.value?.let { fetchInventory(it) }
            delay(400)
            _isRefreshing.value = false
        }
    }

    fun fetchProfile() {
        viewModelScope.launch {
            if (_profileState.value !is PartnerDataState.Success) {
                _profileState.value = PartnerDataState.Loading
            }
            repository.getProfile()
                .onSuccess { profile ->
                    _profileState.value = PartnerDataState.Success(profile)
                    val cities = profile.operatingCities.orEmpty()
                    if (_selectedCityId.value == null && cities.isNotEmpty()) {
                        val defaultCityId = cities.first().cityId
                        _selectedCityId.value = defaultCityId
                        fetchInventory(defaultCityId)
                    }
                }
                .onFailure { err ->
                    _profileState.value = PartnerDataState.Error(err.message ?: "Failed to load profile")
                }
        }
    }

    fun selectCity(cityId: String) {
        if (_selectedCityId.value == cityId) return
        _selectedCityId.value = cityId
        fetchInventory(cityId)
    }

    fun fetchOrders() {
        viewModelScope.launch {
            if (_ordersState.value !is PartnerDataState.Success) {
                _ordersState.value = PartnerDataState.Loading
            }
            repository.getOrders()
                .onSuccess { _ordersState.value = PartnerDataState.Success(it) }
                .onFailure { _ordersState.value = PartnerDataState.Error(it.message ?: "Failed to load orders") }
        }
    }

    fun fetchEngineers() {
        viewModelScope.launch {
            if (_engineersState.value !is PartnerDataState.Success) {
                _engineersState.value = PartnerDataState.Loading
            }
            repository.getEngineers()
                .onSuccess { _engineersState.value = PartnerDataState.Success(it) }
                .onFailure { _engineersState.value = PartnerDataState.Error(it.message ?: "Failed to load engineers") }
        }
    }

    fun fetchInventory(cityId: String? = _selectedCityId.value) {
        val targetCityId = cityId ?: return
        viewModelScope.launch {
            if (_inventoryState.value !is PartnerDataState.Success) {
                _inventoryState.value = PartnerDataState.Loading
            }
            repository.getInventory(targetCityId)
                .onSuccess { _inventoryState.value = PartnerDataState.Success(it) }
                .onFailure { _inventoryState.value = PartnerDataState.Error(it.message ?: "Failed to load inventory") }
        }
    }

    fun fetchPendingPricingRequests() {
        viewModelScope.launch {
            if (_pendingPricingRequestsState.value !is PartnerDataState.Success) {
                _pendingPricingRequestsState.value = PartnerDataState.Loading
            }
            repository.getPendingPricingRequests()
                .onSuccess { _pendingPricingRequestsState.value = PartnerDataState.Success(it) }
                .onFailure { _pendingPricingRequestsState.value = PartnerDataState.Error(it.message ?: "Failed to load pricing requests") }
        }
    }

    private fun fetchCatalogMetadata() {
        viewModelScope.launch {
            repository.getBrands().onSuccess { _brands.value = it }
            repository.getCategories().onSuccess { _categories.value = it }
        }
    }

    private fun replaceOrderInState(updated: OrderResponse) {
        val current = _ordersState.value
        if (current is PartnerDataState.Success) {
            _ordersState.value = PartnerDataState.Success(
                current.data.map { if (it.orderId == updated.orderId) updated else it }
            )
        }
    }

    // --- Order Actions ---
    fun assignEngineer(
        orderId: String,
        engineerId: String,
        onSuccess: (OrderResponse) -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            repository.assignEngineer(orderId, engineerId)
                .onSuccess { updated ->
                    replaceOrderInState(updated)
                    onSuccess(updated)
                }
                .onFailure { onError(it.message ?: "Failed to assign engineer") }
        }
    }

    fun unassignEngineer(
        orderId: String,
        onSuccess: (OrderResponse) -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            repository.unassignEngineer(orderId)
                .onSuccess { updated ->
                    replaceOrderInState(updated)
                    onSuccess(updated)
                }
                .onFailure { onError(it.message ?: "Failed to unassign engineer") }
        }
    }

    fun updateOrderStatus(
        orderId: String,
        newStatus: String,
        cancellationReason: String? = null,
        onSuccess: (OrderResponse) -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            repository.updateOrderStatus(orderId, newStatus, cancellationReason)
                .onSuccess { updated ->
                    replaceOrderInState(updated)
                    _selectedCityId.value?.let { fetchInventory(it) }
                    onSuccess(updated)
                }
                .onFailure { onError(it.message ?: "Failed to update order status") }
        }
    }

    fun addOrderNote(
        orderId: String,
        note: String,
        onSuccess: (OrderResponse) -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            repository.addOrderNote(orderId, note)
                .onSuccess { updated ->
                    replaceOrderInState(updated)
                    onSuccess(updated)
                }
                .onFailure { onError(it.message ?: "Failed to add note") }
        }
    }

    // --- Engineer Actions ---
    fun createEngineer(
        request: CreateEngineerRequest,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            repository.createEngineer(request)
                .onSuccess {
                    fetchEngineers()
                    onSuccess()
                }
                .onFailure { onError(it.message ?: "Failed to create engineer") }
        }
    }

    fun updateEngineer(
        id: String,
        request: CreateEngineerRequest,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            repository.updateEngineer(id, request)
                .onSuccess {
                    fetchEngineers()
                    onSuccess()
                }
                .onFailure { onError(it.message ?: "Failed to update engineer") }
        }
    }

    fun toggleEngineerStatus(
        engineer: EngineerResponse,
        newIsActive: Boolean,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            if (!newIsActive) {
                repository.deactivateEngineer(engineer.id)
                    .onSuccess {
                        fetchEngineers()
                        onSuccess()
                    }
                    .onFailure { onError(it.message ?: "Failed to suspend engineer") }
            } else {
                val request = CreateEngineerRequest(
                    firstName = engineer.firstName.orEmpty(),
                    lastName = engineer.lastName.orEmpty(),
                    email = engineer.email.orEmpty(),
                    phoneNumber = engineer.phoneNumber.orEmpty(),
                    alternatePhone = engineer.alternatePhone,
                    address = engineer.address,
                    city = engineer.city,
                    password = null,
                    partnerId = engineer.partnerId,
                    isActive = true
                )
                repository.updateEngineer(engineer.id, request)
                    .onSuccess {
                        fetchEngineers()
                        onSuccess()
                    }
                    .onFailure { onError(it.message ?: "Failed to activate engineer") }
            }
        }
    }

    // --- Inventory Actions ---
    fun updateSingleStock(
        productId: String,
        cityId: String,
        stock: Int,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            repository.updateSingleStock(productId, cityId, stock)
                .onSuccess { updatedItem ->
                    val current = _inventoryState.value
                    if (current is PartnerDataState.Success) {
                        _inventoryState.value = PartnerDataState.Success(
                            current.data.map { if (it.productId == updatedItem.productId) updatedItem else it }
                        )
                    } else {
                        fetchInventory(cityId)
                    }
                    onSuccess()
                }
                .onFailure { onError(it.message ?: "Failed to update stock") }
        }
    }

    fun bulkUpdateStock(
        cityId: String,
        updates: List<CityProductStockItem>,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            repository.bulkUpdateInventory(PartnerBulkInventoryUpdateRequest(cityId, updates))
                .onSuccess { res ->
                    fetchInventory(cityId)
                    onSuccess(res.message ?: "${res.updatedCount} items updated")
                }
                .onFailure { onError(it.message ?: "Failed to bulk update inventory") }
        }
    }

    // --- Products & Pricing Actions ---
    fun submitCityPricingRequest(
        productId: String,
        request: CityPricingRequestDto,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            repository.submitCityPricingRequest(productId, request)
                .onSuccess {
                    fetchPendingPricingRequests()
                    onSuccess()
                }
                .onFailure { onError(it.message ?: "Failed to submit price change request") }
        }
    }

    fun updateDirectCityPricing(
        productId: String,
        request: PartnerCityPricingUpdateDto,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            repository.updateDirectCityPricing(productId, request)
                .onSuccess {
                    _selectedCityId.value?.let { fetchInventory(it) }
                    onSuccess()
                }
                .onFailure { onError(it.message ?: "Failed to update pricing") }
        }
    }

    fun requestNewProduct(
        request: PartnerCreateProductRequest,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            repository.requestNewProduct(request)
                .onSuccess { onSuccess() }
                .onFailure { onError(it.message ?: "Failed to submit product request") }
        }
    }

    // --- Notifications ---
    fun fetchNotifications() {
        viewModelScope.launch {
            if (_notificationsState.value !is PartnerDataState.Success) {
                _notificationsState.value = PartnerDataState.Loading
            }
            repository.getNotifications()
                .onSuccess { _notificationsState.value = PartnerDataState.Success(it) }
                .onFailure { _notificationsState.value = PartnerDataState.Error(it.message ?: "Failed to load notifications") }
        }
    }

    fun clearAllNotifications() {
        viewModelScope.launch {
            repository.clearNotifications()
                .onSuccess { _notificationsState.value = PartnerDataState.Success(emptyList()) }
        }
    }

    fun deleteNotification(id: String) {
        val current = _notificationsState.value
        if (current is PartnerDataState.Success) {
            _notificationsState.value = PartnerDataState.Success(current.data.filter { it.id != id })
            viewModelScope.launch {
                repository.deleteNotification(id).onFailure {
                    _notificationsState.value = current
                }
            }
        }
    }

    companion object {
        fun provideFactory(repository: PartnerRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return PartnerViewModel(repository) as T
                }
            }
    }
}
