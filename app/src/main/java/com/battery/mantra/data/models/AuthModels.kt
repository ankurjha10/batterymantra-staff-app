package com.battery.mantra.data.models

import com.google.gson.annotations.SerializedName

data class LoginRequest(
    @SerializedName("username") val username: String,
    @SerializedName("password") val password: String,
    @SerializedName("gateToken") val gateToken: String? = null
)

data class LoginResponse(
    @SerializedName("token") val token: String,
    @SerializedName("refreshToken") val refreshToken: String,
    @SerializedName("id") val id: String,
    @SerializedName("role") val role: String,
    @SerializedName("name") val name: String,
    @SerializedName("permissions") val permissions: List<String>? = null
)

data class PasswordChangeRequest(
    @SerializedName("currentPassword") val currentPassword: String,
    @SerializedName("newPassword") val newPassword: String
)

data class SendOtpRequest(
    @SerializedName("phoneNumber") val phoneNumber: String
)

data class VerifyOtpRequest(
    @SerializedName("phoneNumber") val phoneNumber: String,
    @SerializedName("otp") val otp: String
)

data class AdminGateRequest(
    @SerializedName("pin") val pin: String
)

data class AdminGateResponse(
    @SerializedName("tier") val tier: String,
    @SerializedName("gateToken") val gateToken: String
)

data class GateUserPinStatus(
    @SerializedName("userId") val userId: String,
    @SerializedName("username") val username: String? = null,
    @SerializedName("email") val email: String? = null,
    @SerializedName("role") val role: String? = null,
    @SerializedName("isActive") val isActive: Boolean = true,
    @SerializedName("personalPinConfigured") val personalPinConfigured: Boolean = false,
    @SerializedName("gatePinUpdatedAt") val gatePinUpdatedAt: String? = null
)

data class GateConfigResponse(
    @SerializedName("adminPinConfigured") val adminPinConfigured: Boolean = false,
    @SerializedName("subAdminPinConfigured") val subAdminPinConfigured: Boolean = false,
    @SerializedName("updatedAt") val updatedAt: String? = null,
    @SerializedName("users") val users: List<GateUserPinStatus> = emptyList()
)

data class UpdateGatePinRequest(
    @SerializedName("targetRole") val targetRole: String,
    @SerializedName("newPin") val newPin: String,
    @SerializedName("currentPassword") val currentPassword: String
)

data class UpdateUserGatePinRequest(
    @SerializedName("newPin") val newPin: String,
    @SerializedName("currentPassword") val currentPassword: String
)
