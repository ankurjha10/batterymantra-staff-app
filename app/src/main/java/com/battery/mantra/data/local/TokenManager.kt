package com.battery.mantra.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

interface AuthSessionStore {
    val adminBiometricEnabled: Flow<Boolean>
    suspend fun saveTokens(jwt: String, refresh: String, role: String, permissions: List<String>?)
    suspend fun clearTokens()
    suspend fun isAdminBiometricEnabled(): Boolean
    suspend fun setAdminBiometricEnabled(enabled: Boolean)
    suspend fun getAdminGateFailedAttempts(): Int
    suspend fun getAdminGateLockoutRemainingMs(nowMs: Long = System.currentTimeMillis()): Long
    suspend fun recordFailedAdminGateAttempt(nowMs: Long = System.currentTimeMillis()): Pair<Int, Long>
    suspend fun recordGateRateLimitCooldown(durationMs: Long = TokenManager.GATE_RATE_LIMIT_COOLDOWN_MS, nowMs: Long = System.currentTimeMillis()): Long
    suspend fun resetAdminGateAttempts()
}

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class TokenManager(private val context: Context) : AuthSessionStore {
    companion object {
        private val JWT_TOKEN_KEY = stringPreferencesKey("jwt_token")
        private val REFRESH_TOKEN_KEY = stringPreferencesKey("refresh_token")
        private val ROLE_KEY = stringPreferencesKey("user_role")
        private val PERMISSIONS_KEY = androidx.datastore.preferences.core.stringSetPreferencesKey("user_permissions")
        private val CLEARED_NOTIFS_KEY = longPreferencesKey("cleared_notifs_time")
        private val LANGUAGE_KEY = stringPreferencesKey("app_language")
        private val ADMIN_GATE_FAILED_ATTEMPTS_KEY = intPreferencesKey("admin_gate_failed_attempts")
        private val ADMIN_GATE_LOCKED_UNTIL_KEY = longPreferencesKey("admin_gate_locked_until")
        private val ADMIN_BIOMETRIC_ENABLED_KEY = booleanPreferencesKey("admin_biometric_enabled")

        const val MAX_ADMIN_GATE_ATTEMPTS = 3
        const val ADMIN_GATE_LOCKOUT_MS = 15 * 60 * 1000L
        const val GATE_RATE_LIMIT_COOLDOWN_MS = 60 * 1000L
    }

    @Volatile private var cachedJwt: String? = null
    @Volatile private var cachedRefresh: String? = null
    @Volatile private var cachedRole: String? = null
    @Volatile private var cachedPermissions: Set<String>? = null

    val jwtToken: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[JWT_TOKEN_KEY].also { cachedJwt = it }
    }

    val refreshToken: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[REFRESH_TOKEN_KEY].also { cachedRefresh = it }
    }

    val userRole: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[ROLE_KEY].also { cachedRole = it }
    }

    val userPermissions: Flow<Set<String>> = context.dataStore.data.map { preferences ->
        preferences[PERMISSIONS_KEY].also { cachedPermissions = it } ?: emptySet()
    }

    fun getCachedJwt(): String? {
        val memoryToken = cachedJwt
        if (!memoryToken.isNullOrEmpty()) return memoryToken
        return runBlocking {
            context.dataStore.data.first()[JWT_TOKEN_KEY].also { cachedJwt = it }
        }
    }

    fun getCachedRefresh(): String? {
        val memoryRefresh = cachedRefresh
        if (!memoryRefresh.isNullOrEmpty()) return memoryRefresh
        return runBlocking {
            context.dataStore.data.first()[REFRESH_TOKEN_KEY].also { cachedRefresh = it }
        }
    }

    fun getCachedRole(): String? {
        val memoryRole = cachedRole
        if (!memoryRole.isNullOrEmpty()) return memoryRole
        return runBlocking {
            context.dataStore.data.first()[ROLE_KEY].also { cachedRole = it }
        }
    }

    fun getCachedPermissions(): Set<String> {
        val memoryPerms = cachedPermissions
        if (memoryPerms != null) return memoryPerms
        return runBlocking {
            (context.dataStore.data.first()[PERMISSIONS_KEY] ?: emptySet()).also { cachedPermissions = it }
        }
    }

    override suspend fun saveTokens(jwt: String, refresh: String, role: String, permissions: List<String>?) {
        cachedJwt = jwt
        cachedRefresh = refresh
        cachedRole = role
        val permsSet = permissions?.toSet() ?: emptySet()
        cachedPermissions = permsSet
        context.dataStore.edit { preferences ->
            preferences[JWT_TOKEN_KEY] = jwt
            preferences[REFRESH_TOKEN_KEY] = refresh
            preferences[ROLE_KEY] = role
            preferences[PERMISSIONS_KEY] = permsSet
        }
    }

    override suspend fun clearTokens() {
        cachedJwt = null
        cachedRefresh = null
        cachedRole = null
        cachedPermissions = null
        context.dataStore.edit { preferences ->
            preferences.remove(JWT_TOKEN_KEY)
            preferences.remove(REFRESH_TOKEN_KEY)
            preferences.remove(ROLE_KEY)
            preferences.remove(PERMISSIONS_KEY)
        }
    }

    suspend fun setNotificationsClearedTime(timeMillis: Long) {
        context.dataStore.edit { preferences ->
            preferences[CLEARED_NOTIFS_KEY] = timeMillis
        }
    }

    fun getNotificationsClearedTime(): Long {
        return runBlocking {
            context.dataStore.data.first()[CLEARED_NOTIFS_KEY] ?: 0L
        }
    }

    suspend fun saveLanguage(languageCode: String) {
        context.dataStore.edit { preferences ->
            preferences[LANGUAGE_KEY] = languageCode
        }
    }

    fun getLanguage(): String {
        return runBlocking {
            context.dataStore.data.first()[LANGUAGE_KEY] ?: "en"
        }
    }

    override val adminBiometricEnabled: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[ADMIN_BIOMETRIC_ENABLED_KEY] ?: false
    }

    override suspend fun isAdminBiometricEnabled(): Boolean {
        return context.dataStore.data.first()[ADMIN_BIOMETRIC_ENABLED_KEY] ?: false
    }

    override suspend fun setAdminBiometricEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[ADMIN_BIOMETRIC_ENABLED_KEY] = enabled
        }
    }

    override suspend fun getAdminGateFailedAttempts(): Int {
        val prefs = context.dataStore.data.first()
        val lockedUntil = prefs[ADMIN_GATE_LOCKED_UNTIL_KEY] ?: 0L
        if (lockedUntil > 0L && System.currentTimeMillis() >= lockedUntil) {
            resetAdminGateAttempts()
            return 0
        }
        return prefs[ADMIN_GATE_FAILED_ATTEMPTS_KEY] ?: 0
    }

    override suspend fun getAdminGateLockoutRemainingMs(nowMs: Long): Long {
        val prefs = context.dataStore.data.first()
        val lockedUntil = prefs[ADMIN_GATE_LOCKED_UNTIL_KEY] ?: 0L
        if (lockedUntil <= 0L) return 0L
        val remaining = lockedUntil - nowMs
        if (remaining <= 0L) {
            resetAdminGateAttempts()
            return 0L
        }
        return remaining
    }

    override suspend fun recordFailedAdminGateAttempt(nowMs: Long): Pair<Int, Long> {
        var attemptsLeft = MAX_ADMIN_GATE_ATTEMPTS
        var lockedUntil = 0L
        context.dataStore.edit { preferences ->
            val currentFailed = (preferences[ADMIN_GATE_FAILED_ATTEMPTS_KEY] ?: 0) + 1
            if (currentFailed >= MAX_ADMIN_GATE_ATTEMPTS) {
                lockedUntil = nowMs + ADMIN_GATE_LOCKOUT_MS
                attemptsLeft = 0
                preferences[ADMIN_GATE_FAILED_ATTEMPTS_KEY] = 0
                preferences[ADMIN_GATE_LOCKED_UNTIL_KEY] = lockedUntil
            } else {
                attemptsLeft = MAX_ADMIN_GATE_ATTEMPTS - currentFailed
                preferences[ADMIN_GATE_FAILED_ATTEMPTS_KEY] = currentFailed
                preferences[ADMIN_GATE_LOCKED_UNTIL_KEY] = 0L
            }
        }
        return attemptsLeft to lockedUntil
    }

    override suspend fun recordGateRateLimitCooldown(durationMs: Long, nowMs: Long): Long {
        val lockedUntil = nowMs + durationMs
        context.dataStore.edit { preferences ->
            preferences[ADMIN_GATE_LOCKED_UNTIL_KEY] = lockedUntil
        }
        return lockedUntil
    }

    override suspend fun resetAdminGateAttempts() {
        context.dataStore.edit { preferences ->
            preferences[ADMIN_GATE_FAILED_ATTEMPTS_KEY] = 0
            preferences[ADMIN_GATE_LOCKED_UNTIL_KEY] = 0L
        }
    }
}
