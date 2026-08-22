package com.battery.mantra.util

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

object AppEventBus {
    private val _refreshEvents = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val refreshEvents = _refreshEvents.asSharedFlow()

    fun emitRefreshEvent(type: String = "general") {
        _refreshEvents.tryEmit(type)
    }
}
