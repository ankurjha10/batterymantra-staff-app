package com.battery.mantra.data.models

data class EnquiryResponse(
    val id: Long,
    val enquiryType: String,
    val name: String?,
    val mobileNumber: String,
    val email: String?,
    val companyName: String?,
    val gstin: String?,
    val quantity: String?,
    val message: String?,
    val productId: String?,
    val productName: String?,
    val status: String,
    val createdAt: String?
)

data class UpdateEnquiryStatusRequest(
    val status: String
)
