package com.encer.offlinesplitwise.data.preferences

import android.content.Context
import android.content.SharedPreferences
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets
import java.security.KeyStore
import java.util.UUID
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class SessionRepository(context: Context) {
    private val preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val cipher = SessionPreferenceCipher()

    init {
        migrateLegacyPlaintextIfNeeded()
    }

    private val _session = MutableStateFlow(loadSession())
    private val _lastSyncedAt = MutableStateFlow(
        preferences.getLong(SessionKeys.LAST_SYNCED_AT, 0L).takeIf { it > 0L }
    )

    fun observeSession(): StateFlow<AuthSession?> = _session
    fun observeLastSyncedAt(): StateFlow<Long?> = _lastSyncedAt

    fun currentSession(): AuthSession? = _session.value
    fun currentAccessToken(): String? = _session.value?.accessToken
    fun currentRefreshToken(): String? = _session.value?.refreshToken
    fun currentDataOwnerUserId(): String? = getEncryptedString(SessionKeys.DATA_OWNER_USER_ID)

    fun saveSession(session: AuthSession) {
        preferences.edit()
            .putEncryptedString(SessionKeys.ACCESS_TOKEN, session.accessToken)
            .putEncryptedString(SessionKeys.REFRESH_TOKEN, session.refreshToken)
            .putEncryptedString(SessionKeys.USER_ID, session.userId)
            .putEncryptedString(SessionKeys.USER_NAME, session.name)
            .putEncryptedString(SessionKeys.USER_USERNAME, session.username)
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
        val current = getEncryptedString(SessionKeys.DEVICE_ID)
        if (!current.isNullOrBlank()) return current
        val generated = UUID.randomUUID().toString()
        preferences.edit().putEncryptedString(SessionKeys.DEVICE_ID, generated).apply()
        return generated
    }

    fun setLastSyncedAt(timestamp: Long?) {
        preferences.edit().apply {
            if (timestamp == null) remove(SessionKeys.LAST_SYNCED_AT) else putLong(SessionKeys.LAST_SYNCED_AT, timestamp)
        }.apply()
        _lastSyncedAt.value = timestamp
    }

    fun setDataOwnerUserId(userId: String?) {
        preferences.edit().apply {
            if (userId.isNullOrBlank()) remove(SessionKeys.DATA_OWNER_USER_ID) else putEncryptedString(SessionKeys.DATA_OWNER_USER_ID, userId)
        }.apply()
    }

    private fun loadSession(): AuthSession? {
        val accessToken = getEncryptedString(SessionKeys.ACCESS_TOKEN)
        val refreshToken = getEncryptedString(SessionKeys.REFRESH_TOKEN)
        val userId = getEncryptedString(SessionKeys.USER_ID)
        val name = getEncryptedString(SessionKeys.USER_NAME)
        val username = getEncryptedString(SessionKeys.USER_USERNAME)
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

    private fun getEncryptedString(key: String): String? {
        val stored = preferences.getString(key, null) ?: return null
        return runCatching { cipher.decrypt(stored) }.getOrNull()
    }

    private fun SharedPreferences.Editor.putEncryptedString(key: String, value: String): SharedPreferences.Editor {
        return putString(key, cipher.encrypt(value))
    }

    private fun migrateLegacyPlaintextIfNeeded() {
        if (preferences.getInt(KEY_STORAGE_VERSION, 1) >= STORAGE_VERSION) return

        preferences.edit().apply {
            ENCRYPTED_STRING_KEYS.forEach { key ->
                preferences.getString(key, null)?.takeIf { it.isNotBlank() }?.let { plainValue ->
                    putEncryptedString(key, plainValue)
                }
            }
            putInt(KEY_STORAGE_VERSION, STORAGE_VERSION)
        }.apply()
    }

    private companion object {
        const val PREFS_NAME = "session_state"
        const val KEY_STORAGE_VERSION = "storage_version"
        const val STORAGE_VERSION = 2

        val ENCRYPTED_STRING_KEYS = listOf(
            SessionKeys.ACCESS_TOKEN,
            SessionKeys.REFRESH_TOKEN,
            SessionKeys.USER_ID,
            SessionKeys.USER_NAME,
            SessionKeys.USER_USERNAME,
            SessionKeys.DEVICE_ID,
            SessionKeys.DATA_OWNER_USER_ID,
        )
    }
}

private class SessionPreferenceCipher {
    private val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }

    fun encrypt(value: String): String {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, getOrCreateSecretKey())
        val encrypted = cipher.doFinal(value.toByteArray(StandardCharsets.UTF_8))
        val payload = ByteBuffer.allocate(Int.SIZE_BYTES + cipher.iv.size + encrypted.size)
            .putInt(cipher.iv.size)
            .put(cipher.iv)
            .put(encrypted)
            .array()
        return Base64.encodeToString(payload, Base64.NO_WRAP)
    }

    fun decrypt(value: String): String {
        val decoded = Base64.decode(value, Base64.NO_WRAP)
        val buffer = ByteBuffer.wrap(decoded)
        val ivLength = buffer.int
        val iv = ByteArray(ivLength).also(buffer::get)
        val encrypted = ByteArray(buffer.remaining()).also(buffer::get)
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.DECRYPT_MODE, getOrCreateSecretKey(), GCMParameterSpec(GCM_TAG_LENGTH, iv))
        return cipher.doFinal(encrypted).toString(StandardCharsets.UTF_8)
    }

    private fun getOrCreateSecretKey(): SecretKey {
        (keyStore.getKey(KEY_ALIAS, null) as? SecretKey)?.let { return it }
        val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE)
        keyGenerator.init(
            KeyGenParameterSpec.Builder(
                KEY_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setUserAuthenticationRequired(false)
                .build()
        )
        return keyGenerator.generateKey()
    }

    private companion object {
        const val ANDROID_KEYSTORE = "AndroidKeyStore"
        const val KEY_ALIAS = "offline_splitwise_session_key"
        const val TRANSFORMATION = "AES/GCM/NoPadding"
        const val GCM_TAG_LENGTH = 128
    }
}
