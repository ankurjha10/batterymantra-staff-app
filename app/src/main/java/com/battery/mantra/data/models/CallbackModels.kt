package com.battery.mantra.data.models

data class CallbackResponse(
    val callbackId: Long,
    val mobileNumber: String,
    val status: String,
    val createdAt: String?
)

data class UpdateCallbackStatusRequest(
    val status: String
)
