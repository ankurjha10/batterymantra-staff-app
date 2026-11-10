package com.battery.mantra.data.repository

import com.battery.mantra.data.local.AuthSessionStore
import com.battery.mantra.data.local.TokenManager
import com.battery.mantra.data.models.AdminGateResponse
import com.battery.mantra.data.models.GateConfigResponse
import com.battery.mantra.data.models.GateUserPinStatus
import com.battery.mantra.data.models.LoginRequest
import com.battery.mantra.data.models.LoginResponse
import com.battery.mantra.data.models.UpdateUserGatePinRequest
import com.battery.mantra.data.remote.BatteryMantraApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.runBlocking
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.Response
import java.lang.reflect.Proxy

private class FakeAuthSessionStore : AuthSessionStore {
    var savedRole: String? = null
    var savedJwt: String? = null
    private val bioFlow = MutableStateFlow(false)
    private var failedAttempts = 0
    private var lockedUntil = 0L

    override val adminBiometricEnabled: Flow<Boolean> = bioFlow

    override suspend fun saveTokens(jwt: String, refresh: String, role: String, permissions: List<String>?) {
        savedJwt = jwt
        savedRole = role
    }

    override suspend fun clearTokens() {
        savedJwt = null
        savedRole = null
    }

    override suspend fun isAdminBiometricEnabled(): Boolean = bioFlow.value

    override suspend fun setAdminBiometricEnabled(enabled: Boolean) {
        bioFlow.value = enabled
    }

    override suspend fun getAdminGateFailedAttempts(): Int = failedAttempts

    override suspend fun getAdminGateLockoutRemainingMs(nowMs: Long): Long =
        (lockedUntil - nowMs).coerceAtLeast(0L)

    override suspend fun recordFailedAdminGateAttempt(nowMs: Long): Pair<Int, Long> {
        failedAttempts++
        return if (failedAttempts >= TokenManager.MAX_ADMIN_GATE_ATTEMPTS) {
            lockedUntil = nowMs + TokenManager.ADMIN_GATE_LOCKOUT_MS
            failedAttempts = 0
            0 to lockedUntil
        } else {
            (TokenManager.MAX_ADMIN_GATE_ATTEMPTS - failedAttempts) to 0L
        }
    }

    override suspend fun recordGateRateLimitCooldown(durationMs: Long, nowMs: Long): Long {
        lockedUntil = nowMs + durationMs
        return lockedUntil
    }

    override suspend fun resetAdminGateAttempts() {
        failedAttempts = 0
        lockedUntil = 0L
    }
}

private fun fakeApi(handler: (methodName: String) -> Any?): BatteryMantraApi {
    return Proxy.newProxyInstance(
        BatteryMantraApi::class.java.classLoader,
        arrayOf(BatteryMantraApi::class.java)
    ) { _, method, _ ->
        handler(method.name)
    } as BatteryMantraApi
}

class AuthSecurityTest {

    private fun loginResponse(role: String) = LoginResponse(
        token = "jwt-123",
        refreshToken = "refresh-123",
        id = "user-1",
        role = role,
        name = "Test User",
        permissions = emptyList()
    )

    @Test
    fun verifyEngineerOtp_rejectsPartnerRoleWithoutSavingTokens() = runBlocking {
        val store = FakeAuthSessionStore()
        val api = fakeApi { method ->
            if (method == "verifyEngineerOtp") Response.success(loginResponse("PARTNER"))
            else error("Unexpected call: $method")
        }
        val repo = AuthRepository(api, store)

        val result = repo.verifyEngineerOtp("9999999999", "1234")

        assertTrue(result.isFailure)
        assertNull(store.savedJwt)
        assertNull(store.savedRole)
    }

    @Test
    fun verifyPartnerOtp_rejectsEngineerRoleWithoutSavingTokens() = runBlocking {
        val store = FakeAuthSessionStore()
        val api = fakeApi { method ->
            if (method == "verifyPartnerOtp") Response.success(loginResponse("ENGINEER"))
            else error("Unexpected call: $method")
        }
        val repo = AuthRepository(api, store)

        val result = repo.verifyPartnerOtp("9999999999", "1234")

        assertTrue(result.isFailure)
        assertNull(store.savedJwt)
        assertNull(store.savedRole)
    }

    @Test
    fun verifyPartnerOtp_acceptsPartnerRoleAndSavesTokens() = runBlocking {
        val store = FakeAuthSessionStore()
        val api = fakeApi { method ->
            if (method == "verifyPartnerOtp") Response.success(loginResponse("PARTNER"))
            else error("Unexpected call: $method")
        }
        val repo = AuthRepository(api, store)

        val result = repo.verifyPartnerOtp("9999999999", "1234")

        assertTrue(result.isSuccess)
        assertEquals("PARTNER", store.savedRole)
        assertEquals("jwt-123", store.savedJwt)
    }

    @Test
    fun adminLogin_enforcesUnlockedTierAndRequiresReverifyOn403OrExpiredGateToken() = runBlocking {
        val store = FakeAuthSessionStore()
        val expiredBody = """{"message":"Invalid or expired gate token"}""".toResponseBody("application/json".toMediaType())
        val forbiddenBody = """{"message":"Please enter your personal Gate PIN"}""".toResponseBody("application/json".toMediaType())
        val badCredsBody = """{"message":"Invalid password"}""".toResponseBody("application/json".toMediaType())

        val expiredRepo = AuthRepository(
            fakeApi { Response.error<LoginResponse>(401, expiredBody) },
            store
        )
        val expiredResult = expiredRepo.login(LoginRequest("admin@test.com", "pass", "gate-jwt"), "ADMIN")
        assertTrue(expiredResult.exceptionOrNull() is GateReverifyRequiredException)

        val forbiddenRepo = AuthRepository(
            fakeApi { Response.error<LoginResponse>(403, forbiddenBody) },
            store
        )
        val forbiddenResult = forbiddenRepo.login(LoginRequest("sub@test.com", "pass", "gate-jwt"), "SUB_ADMIN")
        assertTrue(forbiddenResult.exceptionOrNull() is GateReverifyRequiredException)
        assertEquals("Please enter your personal Gate PIN", forbiddenResult.exceptionOrNull()?.message)

        val badCredsRepo = AuthRepository(
            fakeApi { Response.error<LoginResponse>(401, badCredsBody) },
            store
        )
        val badCredsResult = badCredsRepo.login(LoginRequest("admin@test.com", "wrong", "gate-jwt"), "ADMIN")
        assertTrue(badCredsResult.isFailure)
        assertTrue(badCredsResult.exceptionOrNull() !is GateReverifyRequiredException)
    }

    @Test
    fun verifyAdminGate_returnsSpecificExceptionsFor401And429() = runBlocking {
        val store = FakeAuthSessionStore()
        val body401 = """{"message":"Unauthorized"}""".toResponseBody("application/json".toMediaType())
        val repo401 = AuthRepository(
            fakeApi { Response.error<AdminGateResponse>(401, body401) },
            store
        )
        val res401 = repo401.verifyAdminGate("0000")
        assertTrue(res401.exceptionOrNull() is InvalidGatePinException)
        assertEquals("Invalid gate PIN", res401.exceptionOrNull()?.message)

        val body429 = """{"message":"Rate limit"}""".toResponseBody("application/json".toMediaType())
        val repo429 = AuthRepository(
            fakeApi { Response.error<AdminGateResponse>(429, body429) },
            store
        )
        val res429 = repo429.verifyAdminGate("0000")
        assertTrue(res429.exceptionOrNull() is GateRateLimitedException)
        assertEquals("Too many attempts. Please wait 1 minute.", res429.exceptionOrNull()?.message)
    }

    @Test
    fun gateConfigAndPersonalPinUpdate_workForSuperAdmin() = runBlocking {
        val store = FakeAuthSessionStore()
        val sampleConfig = GateConfigResponse(
            adminPinConfigured = true,
            subAdminPinConfigured = true,
            updatedAt = "2026-11-11T00:00:00",
            users = listOf(
                GateUserPinStatus(
                    userId = "u-1",
                    username = "subadmin1",
                    email = "sub@test.com",
                    role = "SUB_ADMIN",
                    isActive = true,
                    personalPinConfigured = false
                )
            )
        )
        val api = fakeApi { method ->
            when (method) {
                "getGateConfig" -> Response.success(sampleConfig)
                "updateUserGatePin" -> Response.success("{}".toResponseBody("application/json".toMediaType()))
                else -> error("Unexpected call: $method")
            }
        }
        val repo = AuthRepository(api, store)

        val cfg = repo.getGateConfig()
        assertTrue(cfg.isSuccess)
        assertEquals(1, cfg.getOrNull()?.users?.size)
        assertEquals("subadmin1", cfg.getOrNull()?.users?.first()?.username)

        val updateRes = repo.updateUserGatePin("u-1", UpdateUserGatePinRequest("9876", "superpass"))
        assertTrue(updateRes.isSuccess)
    }

    @Test
    fun gateRateLimitCooldown_persists60Seconds() = runBlocking {
        val store = FakeAuthSessionStore()
        val t0 = 1_000_000L
        store.recordGateRateLimitCooldown(60_000L, t0)
        assertTrue(store.getAdminGateLockoutRemainingMs(t0 + 10_000L) > 0L)
        assertEquals(0L, store.getAdminGateLockoutRemainingMs(t0 + 60_001L))
    }
}
