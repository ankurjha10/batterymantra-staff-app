package com.battery.mantra.data.repository

import com.battery.mantra.data.local.AuthSessionStore
import com.battery.mantra.data.local.TokenManager
import com.battery.mantra.data.models.AdminGateResponse
import com.battery.mantra.data.models.LoginRequest
import com.battery.mantra.data.models.LoginResponse
import com.battery.mantra.data.remote.BatteryMantraApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.runBlocking
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
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
    fun adminLogin_enforcesUnlockedTierStrictly() = runBlocking {
        val store = FakeAuthSessionStore()
        val api = fakeApi { method ->
            if (method == "login") Response.success(loginResponse("ADMIN"))
            else error("Unexpected call: $method")
        }
        val repo = AuthRepository(api, store)

        val mismatched = repo.login(
            request = LoginRequest("admin@test.com", "secret", gateToken = "gate-sub"),
            expectedAdminTier = "SUB_ADMIN"
        )
        assertTrue(mismatched.isFailure)
        assertNull(store.savedJwt)

        val matched = repo.login(
            request = LoginRequest("admin@test.com", "secret", gateToken = "gate-admin"),
            expectedAdminTier = "ADMIN"
        )
        assertTrue(matched.isSuccess)
        assertEquals("ADMIN", store.savedRole)
    }

    @Test
    fun adminLogin_rejectsNonAdminRolesWhenUnlocked() = runBlocking {
        val store = FakeAuthSessionStore()
        val api = fakeApi { method ->
            if (method == "login") Response.success(loginResponse("PARTNER"))
            else error("Unexpected call: $method")
        }
        val repo = AuthRepository(api, store)

        val result = repo.login(
            request = LoginRequest("partner@test.com", "secret", gateToken = "gate"),
            expectedAdminTier = "ADMIN"
        )
        assertTrue(result.isFailure)
        assertNull(store.savedJwt)
    }

    @Test
    fun verifyAdminGate_returnsInvalidPinExceptionOn401() = runBlocking {
        val store = FakeAuthSessionStore()
        val errorBody = """{"message":"Wrong gate PIN"}""".toResponseBody("application/json".toMediaType())
        val api = fakeApi { method ->
            if (method == "verifyAdminGate") Response.error<AdminGateResponse>(401, errorBody)
            else error("Unexpected call: $method")
        }
        val repo = AuthRepository(api, store)

        val result = repo.verifyAdminGate("0000")

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is InvalidGatePinException)
        assertEquals("Wrong gate PIN", result.exceptionOrNull()?.message)
    }

    @Test
    fun gateLockout_locksAfterThreeFailedAttempts() = runBlocking {
        val store = FakeAuthSessionStore()
        val t0 = 1_000_000L

        val (left1, lock1) = store.recordFailedAdminGateAttempt(t0)
        assertEquals(2, left1)
        assertEquals(0L, lock1)

        val (left2, lock2) = store.recordFailedAdminGateAttempt(t0 + 1000)
        assertEquals(1, left2)
        assertEquals(0L, lock2)

        val (left3, lock3) = store.recordFailedAdminGateAttempt(t0 + 2000)
        assertEquals(0, left3)
        assertTrue(lock3 > t0 + 2000)
        assertTrue(store.getAdminGateLockoutRemainingMs(t0 + 3000) > 0L)
        assertFalse(store.getAdminGateLockoutRemainingMs(lock3 + 1) > 0L)
    }
}

