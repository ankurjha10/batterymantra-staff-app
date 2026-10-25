package com.battery.mantra.data.models

import com.google.gson.annotations.SerializedName

data class PartnerResponse(
    @SerializedName("id") val id: String,
    @SerializedName("userId") val userId: String?,
    @SerializedName("businessName") val businessName: String?,
    @SerializedName("contactPerson") val contactPerson: String?,
    @SerializedName("email") val email: String?,
    @SerializedName("phoneNumber") val phoneNumber: String?,
    @SerializedName("alternatePhone") val alternatePhone: String? = null,
    @SerializedName("address") val address: String? = null,
    @SerializedName("operatingCities") val operatingCities: List<CityResponse>? = null,
    @SerializedName("active") val active: Boolean?,
    @SerializedName("isActive") val isActive: Boolean?
)

data class OrderStatusUpdateRequest(
    @SerializedName("orderStatus") val orderStatus: String
)

data class EngineerResponse(
    @SerializedName("id") val id: String,
    @SerializedName("userId") val userId: String?,
    @SerializedName("firstName") val firstName: String?,
    @SerializedName("lastName") val lastName: String?,
    @SerializedName("email") val email: String?,
    @SerializedName("phoneNumber") val phoneNumber: String?,
    @SerializedName("alternatePhone") val alternatePhone: String? = null,
    @SerializedName("address") val address: String? = null,
    @SerializedName("city") val city: String? = null,
    @SerializedName("partnerId") val partnerId: String?,
    @SerializedName("partnerBusinessName") val partnerBusinessName: String? = null,
    @SerializedName("active") val active: Boolean?,
    @SerializedName("isActive") val isActive: Boolean?
)

data class UserResponse(
    @SerializedName("userId") val userId: String = "",
    @SerializedName("name") val name: String? = null,
    @SerializedName("email") val email: String? = null,
    @SerializedName("phoneNumber") val phone: String? = null,
    @SerializedName("role") val role: String? = null,
    @SerializedName("permissions") val permissions: List<String>? = emptyList(),
    @SerializedName("active") val active: Boolean? = true,
    @SerializedName("isActive") val isActive: Boolean? = true,
    @SerializedName("createdAt") val createdAt: String? = null,
    @SerializedName("totalJobsCompleted") val totalJobsCompleted: Int? = null,
    @SerializedName("assignedPartnerName") val assignedPartnerName: String? = null,
    @SerializedName("addressLine1") val addressLine1: String? = null,
    @SerializedName("addressLine2") val addressLine2: String? = null,
    @SerializedName("city") val city: String? = null,
    @SerializedName("state") val state: String? = null,
    @SerializedName("pincode") val pincode: String? = null
)

data class CityResponse(
    @SerializedName("cityId") val cityId: String,
    @SerializedName("cityName") val cityName: String
)

data class FcmTokenRequest(
    @SerializedName("fcm_token") val fcmToken: String
)

data class NotificationResponse(
    @SerializedName("id") val id: String,
    @SerializedName("title") val title: String,
    @SerializedName("message") val message: String,
    @SerializedName("createdAt") val createdAt: String
)

data class AdminCreateCustomerRequest(
    val name: String,
    val phone: String,
    val email: String? = null,
    val addressLine1: String,
    val addressLine2: String? = null,
    val city: String,
    val state: String,
    val pincode: String
)

data class AdminUpdateCustomerRequest(
    @SerializedName("name") val name: String? = null,
    @SerializedName("phone") val phone: String? = null,
    @SerializedName("email") val email: String? = null,
    @SerializedName("addressLine1") val addressLine1: String? = null,
    @SerializedName("addressLine2") val addressLine2: String? = null,
    @SerializedName("city") val city: String? = null,
    @SerializedName("state") val state: String? = null,
    @SerializedName("pincode") val pincode: String? = null
)

data class AdminCreateOrderRequest(
    val customerId: String,
    val items: List<AdminOrderItemRequest>,
    val paymentMethod: String,
    val deliveryMethod: String,
    val discount: Double = 0.0,
    val installationDate: String? = null
)

data class AdminOrderItemRequest(
    @SerializedName("productId") val productId: String,
    @SerializedName("quantity") val quantity: Int = 1,
    @SerializedName("exchangeOldBattery") val exchangeOldBattery: Boolean = false
)

data class AdminUpdateOrderRequest(
    @SerializedName("recipientName") val recipientName: String? = null,
    @SerializedName("recipientPhone") val recipientPhone: String? = null,
    @SerializedName("addressLine1") val addressLine1: String? = null,
    @SerializedName("addressLine2") val addressLine2: String? = null,
    @SerializedName("city") val city: String? = null,
    @SerializedName("state") val state: String? = null,
    @SerializedName("pincode") val pincode: String? = null,
    @SerializedName("items") val items: List<AdminOrderItemRequest>? = null,
    @SerializedName("manualDiscount") val manualDiscount: Double? = null,
    @SerializedName("totalAmount") val totalAmount: Double? = null,
    @SerializedName("deliveryMethod") val deliveryMethod: String? = null,
    @SerializedName("installationDate") val installationDate: String? = null,
    @SerializedName("paymentMethod") val paymentMethod: String? = null,
    @SerializedName("paymentStatus") val paymentStatus: String? = null,
    @SerializedName("paymentMode") val paymentMode: String? = null,
    @SerializedName("engineerNotes") val engineerNotes: String? = null,
    @SerializedName("selectedCity") val selectedCity: String? = null
)



data class AdminCreateSubAdminRequest(
    val name: String,
    val phone: String,
    val email: String,
    val password: String,
    val role: String = "SUB_ADMIN",
    val permissions: List<String> = emptyList()
)

data class AdminUpdateSubAdminRequest(
    val name: String,
    val phone: String,
    val permissions: List<String> = emptyList()
)

