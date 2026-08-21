package com.battery.mantra.data.models

import com.google.gson.annotations.SerializedName

data class PartnerResponse(
    @SerializedName("id") val id: String,
    @SerializedName("userId") val userId: String?,
    @SerializedName("businessName") val businessName: String?,
    @SerializedName("contactPerson") val contactPerson: String?,
    @SerializedName("email") val email: String?,
    @SerializedName("phoneNumber") val phoneNumber: String?,
    @SerializedName("active") val active: Boolean?,
    @SerializedName("isActive") val isActive: Boolean?
)

data class EngineerResponse(
    @SerializedName("id") val id: String,
    @SerializedName("userId") val userId: String?,
    @SerializedName("firstName") val firstName: String?,
    @SerializedName("lastName") val lastName: String?,
    @SerializedName("email") val email: String?,
    @SerializedName("phoneNumber") val phoneNumber: String?,
    @SerializedName("partnerId") val partnerId: String?,
    @SerializedName("active") val active: Boolean?,
    @SerializedName("isActive") val isActive: Boolean?
)

data class UserResponse(
    @SerializedName("userId") val userId: String,
    @SerializedName("name") val name: String?,
    @SerializedName("email") val email: String?,
    @SerializedName("role") val role: String?,
    @SerializedName("createdAt") val createdAt: String?
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
