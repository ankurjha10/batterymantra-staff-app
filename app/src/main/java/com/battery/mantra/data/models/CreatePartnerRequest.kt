package com.battery.mantra.data.models

data class CreatePartnerRequest(
    val businessName: String,
    val contactPerson: String,
    val email: String,
    val phoneNumber: String,
    val alternatePhone: String? = null,
    val address: String? = null,
    val password: String? = null,
    val operatingCityIds: List<String>? = null,
    val isActive: Boolean = true
)
