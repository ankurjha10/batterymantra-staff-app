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

    suspend fun sendEngineerOtp(phoneNumber: String): Result<Unit> {
        return try {
            val response = api.sendEngineerOtp(com.battery.mantra.data.models.SendOtpRequest(phoneNumber))
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                val rawError = response.errorBody()?.string() ?: ""
                val errorMsg = try {
                    org.json.JSONObject(rawError).optString("message", rawError)
                } catch (e: Exception) {
                    rawError
                }
                Result.failure(Exception(if (errorMsg.isNotBlank()) errorMsg else "Failed to send OTP"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun verifyEngineerOtp(phoneNumber: String, otp: String): Result<LoginResponse> {
        return try {
            val response = api.verifyEngineerOtp(com.battery.mantra.data.models.VerifyOtpRequest(phoneNumber, otp))
            if (response.isSuccessful && response.body() != null) {
                val loginResponse = response.body()!!
                tokenManager.saveTokens(loginResponse.token, loginResponse.refreshToken, loginResponse.role, loginResponse.permissions)
                Result.success(loginResponse)
            } else {
                val rawError = response.errorBody()?.string() ?: ""
                val errorMsg = try {
                    org.json.JSONObject(rawError).optString("message", rawError)
                } catch (e: Exception) {
                    rawError
                }
                Result.failure(Exception(if (errorMsg.isNotBlank()) errorMsg else "Invalid OTP"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun changePassword(request: com.battery.mantra.data.models.PasswordChangeRequest): Result<Unit> {
        return try {
            val response = api.changePassword(request)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Password change failed: ${response.code()} ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun logout() {
        tokenManager.clearTokens()
        try {
            com.google.firebase.installations.FirebaseInstallations.getInstance().delete()
        } catch (e: Exception) {
            android.util.Log.e("AuthRepository", "Failed to delete FCM token on logout", e)
        }
    }
}
