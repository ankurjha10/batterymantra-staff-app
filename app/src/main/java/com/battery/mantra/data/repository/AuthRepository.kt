package com.battery.mantra.data.repository

import com.battery.mantra.data.local.TokenManager
import com.battery.mantra.data.models.LoginRequest
import com.battery.mantra.data.models.LoginResponse
import com.battery.mantra.data.remote.BatteryMantraApi

class AuthRepository(
    private val api: BatteryMantraApi,
    private val tokenManager: TokenManager
) {
    suspend fun login(request: LoginRequest): Result<LoginResponse> {
        return try {
            val response = api.login(request)
            if (response.isSuccessful && response.body() != null) {
                val loginResponse = response.body()!!
                tokenManager.saveTokens(loginResponse.token, loginResponse.refreshToken, loginResponse.role, loginResponse.permissions)
                Result.success(loginResponse)
            } else {
                Result.failure(Exception("Login failed: ${response.code()} ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun logout() {
        tokenManager.clearTokens()
    }
}
