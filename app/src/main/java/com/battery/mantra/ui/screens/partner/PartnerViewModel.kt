package com.battery.mantra.ui.screens.partner

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.battery.mantra.util.AppEventBus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PartnerViewModel : ViewModel() {
    private val _selectedTabIndex = MutableStateFlow(0)
    val selectedTabIndex: StateFlow<Int> = _selectedTabIndex.asStateFlow()

    init {
        viewModelScope.launch {
            AppEventBus.refreshEvents.collect {
                fetchPartnerOrders()
            }
        }
    }

    fun onTabSelected(index: Int) {
        _selectedTabIndex.value = index
    }

    fun fetchPartnerOrders() {
        // Automatically refreshed when real-time push notification is received
    }
}

