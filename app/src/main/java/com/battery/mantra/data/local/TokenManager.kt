package com.battery.mantra.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class TokenManager(private val context: Context) {
    companion object {
        private val JWT_TOKEN_KEY = stringPreferencesKey("jwt_token")
        private val REFRESH_TOKEN_KEY = stringPreferencesKey("refresh_token")
        private val ROLE_KEY = stringPreferencesKey("user_role")
        private val PERMISSIONS_KEY = androidx.datastore.preferences.core.stringSetPreferencesKey("user_permissions")
        private val CLEARED_NOTIFS_KEY = androidx.datastore.preferences.core.longPreferencesKey("cleared_notifs_time")
    }

    private var cachedJwt: String? = null
    private var cachedRefresh: String? = null
    private var cachedRole: String? = null
    private var cachedPermissions: Set<String>? = null

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
        return cachedJwt ?: runBlocking {
            context.dataStore.data.first()[JWT_TOKEN_KEY]
        }
    }

    fun getCachedRefresh(): String? {
        return cachedRefresh ?: runBlocking {
            context.dataStore.data.first()[REFRESH_TOKEN_KEY]
        }
    }

    fun getCachedRole(): String? {
        return cachedRole ?: runBlocking {
            context.dataStore.data.first()[ROLE_KEY]
        }
    }

    fun getCachedPermissions(): Set<String> {
        return cachedPermissions ?: runBlocking {
            context.dataStore.data.first()[PERMISSIONS_KEY] ?: emptySet()
        }
    }

    suspend fun saveTokens(jwt: String, refresh: String, role: String, permissions: List<String>?) {
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

    suspend fun clearTokens() {
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
}
