package com.encer.offlinesplitwise.data.preferences

import android.content.Context
import java.util.UUID
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class SessionRepository(context: Context) {
    private val preferences = context.getSharedPreferences("session_state", Context.MODE_PRIVATE)
    private val _session = MutableStateFlow(loadSession())
    private val _lastSyncedAt = MutableStateFlow(
        preferences.getLong(SessionKeys.LAST_SYNCED_AT, 0L).takeIf { it > 0L }
    )

    fun observeSession(): StateFlow<AuthSession?> = _session
    fun observeLastSyncedAt(): StateFlow<Long?> = _lastSyncedAt

    fun currentSession(): AuthSession? = _session.value
    fun currentAccessToken(): String? = _session.value?.accessToken
    fun currentRefreshToken(): String? = _session.value?.refreshToken

    fun saveSession(session: AuthSession) {
        preferences.edit()
            .putString(SessionKeys.ACCESS_TOKEN, session.accessToken)
            .putString(SessionKeys.REFRESH_TOKEN, session.refreshToken)
            .putString(SessionKeys.USER_ID, session.userId)
            .putString(SessionKeys.USER_NAME, session.name)
            .putString(SessionKeys.USER_USERNAME, session.username)
            .apply()
        _session.value = session
    }

    fun updateTokens(accessToken: String, refreshToken: String) {
        val current = _session.value ?: return
        saveSession(current.copy(accessToken = accessToken, refreshToken = refreshToken))
    }

    fun clearSession() {
        preferences.edit()
            .remove(SessionKeys.ACCESS_TOKEN)
            .remove(SessionKeys.REFRESH_TOKEN)
            .remove(SessionKeys.USER_ID)
            .remove(SessionKeys.USER_NAME)
            .remove(SessionKeys.USER_USERNAME)
            .remove(SessionKeys.LAST_SYNCED_AT)
            .apply()
        _session.value = null
        _lastSyncedAt.value = null
    }

    fun getDeviceId(): String {
        val current = preferences.getString(SessionKeys.DEVICE_ID, null)
        if (!current.isNullOrBlank()) return current
        val generated = UUID.randomUUID().toString()
        preferences.edit().putString(SessionKeys.DEVICE_ID, generated).apply()
        return generated
    }

    fun setLastSyncedAt(timestamp: Long?) {
        preferences.edit().apply {
            if (timestamp == null) remove(SessionKeys.LAST_SYNCED_AT) else putLong(SessionKeys.LAST_SYNCED_AT, timestamp)
        }.apply()
        _lastSyncedAt.value = timestamp
    }

    private fun loadSession(): AuthSession? {
        val accessToken = preferences.getString(SessionKeys.ACCESS_TOKEN, null)
        val refreshToken = preferences.getString(SessionKeys.REFRESH_TOKEN, null)
        val userId = preferences.getString(SessionKeys.USER_ID, null)
        val name = preferences.getString(SessionKeys.USER_NAME, null)
        val username = preferences.getString(SessionKeys.USER_USERNAME, null)
        return if (
            accessToken.isNullOrBlank() ||
            refreshToken.isNullOrBlank() ||
            userId.isNullOrBlank() ||
            name.isNullOrBlank() ||
            username.isNullOrBlank()
        ) {
            null
        } else {
            AuthSession(
                accessToken = accessToken,
                refreshToken = refreshToken,
                userId = userId,
                name = name,
                username = username,
            )
        }
    }
}
