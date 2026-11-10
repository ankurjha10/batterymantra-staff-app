package com.battery.mantra.data.repository

import com.battery.mantra.data.local.AuthSessionStore
import com.battery.mantra.data.models.AdminGateRequest
import com.battery.mantra.data.models.AdminGateResponse
import com.battery.mantra.data.models.GateConfigResponse
import com.battery.mantra.data.models.LoginRequest
import com.battery.mantra.data.models.LoginResponse
import com.battery.mantra.data.models.PasswordChangeRequest
import com.battery.mantra.data.models.SendOtpRequest
import com.battery.mantra.data.models.UpdateGatePinRequest
import com.battery.mantra.data.models.UpdateUserGatePinRequest
import com.battery.mantra.data.models.VerifyOtpRequest
import com.battery.mantra.data.remote.BatteryMantraApi
import com.google.gson.JsonParser
import kotlinx.coroutines.flow.Flow
import okhttp3.ResponseBody
import retrofit2.Response
import kotlin.coroutines.cancellation.CancellationException

class InvalidGatePinException(message: String) : Exception(message)
class GateRateLimitedException(message: String) : Exception(message)
class GateReverifyRequiredException(message: String) : Exception(message)

class AuthRepository(
    private val api: BatteryMantraApi,
    private val sessionStore: AuthSessionStore
) {
    val adminBiometricEnabled: Flow<Boolean> get() = sessionStore.adminBiometricEnabled

    private fun extractErrorMessage(response: Response<*>, fallback: String): String {
        val rawError = try {
            response.errorBody()?.string().orEmpty()
        } catch (_: Exception) {
            ""
        }
        val parsed = if (rawError.isNotBlank()) {
            try {
                JsonParser.parseString(rawError)
                    .asJsonObject
                    .get("message")
                    ?.takeIf { !it.isJsonNull }
                    ?.asString
                    ?.takeIf { it.isNotBlank() } ?: rawError
            } catch (_: Exception) {
                rawError
            }
        } else {
            ""
        }
        return parsed.ifBlank { fallback }
    }

    private fun normalizeAdminRole(role: String): String =
        when (role.uppercase()) {
            "SUBADMIN", "SUB_ADMIN" -> "SUB_ADMIN"
            else -> role.uppercase()
        }

    suspend fun login(request: LoginRequest, expectedAdminTier: String? = null): Result<LoginResponse> {
        return try {
            val response = api.login(request)
            val body = response.body()
            if (response.isSuccessful && body != null) {
                val normalizedRole = normalizeAdminRole(body.role)
                if (expectedAdminTier != null) {
                    if (normalizedRole !in setOf("ADMIN", "SUB_ADMIN")) {
                        return Result.failure(
                            GateReverifyRequiredException("Only Admin or Sub-Admin accounts can sign in here.")
                        )
                    }
                    if (normalizedRole != normalizeAdminRole(expectedAdminTier)) {
                        return Result.failure(
                            GateReverifyRequiredException("Account role does not match the unlocked gate tier.")
                        )
                    }
                }
                sessionStore.saveTokens(body.token, body.refreshToken, body.role, body.permissions)
                Result.success(body)
            } else {
                val msg = extractErrorMessage(response, "Login failed (${response.code()})")
                when (response.code()) {
                    403 -> Result.failure(GateReverifyRequiredException(msg))
                    401 -> {
                        val isGateTokenIssue = request.gateToken != null && (
                            msg.contains("gate", ignoreCase = true) ||
                                msg.contains("expired", ignoreCase = true)
                            )
                        if (isGateTokenIssue) {
                            Result.failure(GateReverifyRequiredException(msg))
                        } else {
                            Result.failure(Exception(msg))
                        }
                    }
                    else -> Result.failure(Exception(msg))
                }
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend inline fun sendRoleOtp(
        crossinline call: suspend () -> Response<ResponseBody>
    ): Result<Unit> {
        return try {
            val response = call()
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception(extractErrorMessage(response, "Failed to send OTP")))
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend inline fun verifyRoleOtp(
        expectedRole: String,
        mismatchMessage: String,
        crossinline call: suspend () -> Response<LoginResponse>
    ): Result<LoginResponse> {
        return try {
            val response = call()
            val body = response.body()
            if (response.isSuccessful && body != null) {
                if (!body.role.equals(expectedRole, ignoreCase = true)) {
                    return Result.failure(Exception(mismatchMessage))
                }
                sessionStore.saveTokens(body.token, body.refreshToken, body.role, body.permissions)
                Result.success(body)
            } else {
                Result.failure(Exception(extractErrorMessage(response, "Invalid OTP")))
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun sendEngineerOtp(phoneNumber: String): Result<Unit> =
        sendRoleOtp { api.sendEngineerOtp(SendOtpRequest(phoneNumber)) }

    suspend fun verifyEngineerOtp(phoneNumber: String, otp: String): Result<LoginResponse> =
        verifyRoleOtp(
            expectedRole = "ENGINEER",
            mismatchMessage = "This account is not registered as an Engineer. Please use the Partner tab."
        ) { api.verifyEngineerOtp(VerifyOtpRequest(phoneNumber, otp)) }

    suspend fun sendPartnerOtp(phoneNumber: String): Result<Unit> =
        sendRoleOtp { api.sendPartnerOtp(SendOtpRequest(phoneNumber)) }

    suspend fun verifyPartnerOtp(phoneNumber: String, otp: String): Result<LoginResponse> =
        verifyRoleOtp(
            expectedRole = "PARTNER",
            mismatchMessage = "This account is not registered as a Partner. Please use the Engineer tab."
        ) { api.verifyPartnerOtp(VerifyOtpRequest(phoneNumber, otp)) }

    suspend fun verifyAdminGate(pin: String): Result<AdminGateResponse> {
        return try {
            val response = api.verifyAdminGate(AdminGateRequest(pin))
            val body = response.body()
            if (response.isSuccessful && body != null) {
                Result.success(body)
            } else when (response.code()) {
                401 -> Result.failure(InvalidGatePinException("Invalid gate PIN"))
                429 -> Result.failure(GateRateLimitedException("Too many attempts. Please wait 1 minute."))
                else -> Result.failure(Exception(extractErrorMessage(response, "Gate verification unavailable (${response.code()})")))
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getGateConfig(): Result<GateConfigResponse> {
        return try {
            val response = api.getGateConfig()
            val body = response.body()
            if (response.isSuccessful && body != null) {
                Result.success(body)
            } else {
                Result.failure(Exception(extractErrorMessage(response, "Failed to load security configuration")))
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateGatePin(request: UpdateGatePinRequest): Result<Unit> {
        return try {
            val response = api.updateGatePin(request)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception(extractErrorMessage(response, "Failed to update gate PIN")))
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateUserGatePin(userId: String, request: UpdateUserGatePinRequest): Result<Unit> {
        return try {
            val response = api.updateUserGatePin(userId, request)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception(extractErrorMessage(response, "Failed to update personal gate PIN")))
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun changePassword(request: PasswordChangeRequest): Result<Unit> {
        return try {
            val response = api.changePassword(request)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception(extractErrorMessage(response, "Password change failed (${response.code()})")))
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun isAdminBiometricEnabled(): Boolean = sessionStore.isAdminBiometricEnabled()

    suspend fun setAdminBiometricEnabled(enabled: Boolean) = sessionStore.setAdminBiometricEnabled(enabled)

    suspend fun getAdminGateFailedAttempts(): Int = sessionStore.getAdminGateFailedAttempts()

    suspend fun getAdminGateLockoutRemainingMs(): Long = sessionStore.getAdminGateLockoutRemainingMs()

    suspend fun recordFailedAdminGateAttempt(): Pair<Int, Long> = sessionStore.recordFailedAdminGateAttempt()

    suspend fun recordGateRateLimitCooldown(): Long = sessionStore.recordGateRateLimitCooldown()

    suspend fun resetAdminGateAttempts() = sessionStore.resetAdminGateAttempts()

    suspend fun logout() {
        sessionStore.clearTokens()
        try {
            com.google.firebase.installations.FirebaseInstallations.getInstance().delete()
        } catch (e: Exception) {
            android.util.Log.e("AuthRepository", "Failed to delete FCM token on logout", e)
        }
    }
}
