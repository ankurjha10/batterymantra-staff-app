package com.battery.mantra.data.models

data class CreateEngineerRequest(
    val firstName: String,
    val lastName: String,
    val email: String,
    val phoneNumber: String,
    val alternatePhone: String? = null,
    val address: String? = null,
    val city: String? = null,
    val password: String? = null,
    val partnerId: String? = null,
    val isActive: Boolean = true
)
