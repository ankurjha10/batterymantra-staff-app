package com.battery.mantra.data.models

import com.google.gson.annotations.SerializedName

data class LoginRequest(
    @SerializedName("username") val username: String,
    @SerializedName("password") val password: String
)

data class LoginResponse(
    @SerializedName("token") val token: String,
    @SerializedName("refreshToken") val refreshToken: String,
    @SerializedName("id") val id: String,
    @SerializedName("role") val role: String,
    @SerializedName("name") val name: String,
    @SerializedName("permissions") val permissions: List<String>? = null
)
