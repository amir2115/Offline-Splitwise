package com.encer.splitwise.data.preferences

import android.content.Context
import com.encer.splitwise.data.remote.model.HealthCheckResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class HealthStatusState(
    val lastResponse: HealthCheckResponse? = null,
    val lastSuccessfulAt: Long? = null,
    val lastFailureAt: Long? = null,
    val lastError: String? = null,
) {
    val isHealthy: Boolean
        get() = lastSuccessfulAt != null && (lastFailureAt == null || lastSuccessfulAt >= lastFailureAt)
}

class HealthStatusRepository(context: Context) {
    private val preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val _state = MutableStateFlow(loadState())

    fun observeState(): StateFlow<HealthStatusState> = _state
    fun currentState(): HealthStatusState = _state.value

    fun recordSuccess(payload: HealthCheckResponse, observedAt: Long = System.currentTimeMillis()) {
        preferences.edit()
            .putString(KEY_STATUS, payload.status)
            .putInt(KEY_MIN_SUPPORTED_VERSION_CODE, payload.minSupportedVersionCode ?: Int.MIN_VALUE)
            .putInt(KEY_LATEST_VERSION_CODE, payload.latestVersionCode ?: Int.MIN_VALUE)
            .putString(KEY_UPDATE_MODE, payload.updateMode)
            .putString(KEY_STORE_URL, payload.storeUrl)
            .putString(KEY_UPDATE_TITLE, payload.updateTitle)
            .putString(KEY_UPDATE_MESSAGE, payload.updateMessage)
            .putLong(KEY_LAST_SUCCESSFUL_AT, observedAt)
            .remove(KEY_LAST_ERROR)
            .apply()
        _state.value = loadState()
    }

    fun recordFailure(message: String?, observedAt: Long = System.currentTimeMillis()) {
        preferences.edit()
            .putLong(KEY_LAST_FAILURE_AT, observedAt)
            .putString(KEY_LAST_ERROR, message)
            .apply()
        _state.value = loadState()
    }

    private fun loadState(): HealthStatusState {
        val minSupportedVersionCode = preferences.getInt(KEY_MIN_SUPPORTED_VERSION_CODE, Int.MIN_VALUE)
            .takeIf { it != Int.MIN_VALUE }
        val latestVersionCode = preferences.getInt(KEY_LATEST_VERSION_CODE, Int.MIN_VALUE)
            .takeIf { it != Int.MIN_VALUE }
        val status = preferences.getString(KEY_STATUS, null)
        val storeUrl = preferences.getString(KEY_STORE_URL, null)
        val updateMode = preferences.getString(KEY_UPDATE_MODE, null)
        val updateTitle = preferences.getString(KEY_UPDATE_TITLE, null)
        val updateMessage = preferences.getString(KEY_UPDATE_MESSAGE, null)
        val lastSuccessfulAt = preferences.getLong(KEY_LAST_SUCCESSFUL_AT, 0L).takeIf { it > 0L }
        val lastFailureAt = preferences.getLong(KEY_LAST_FAILURE_AT, 0L).takeIf { it > 0L }
        val response = if (
            status != null ||
            minSupportedVersionCode != null ||
            latestVersionCode != null ||
            storeUrl != null ||
            updateMode != null ||
            updateTitle != null ||
            updateMessage != null
        ) {
            HealthCheckResponse(
                status = status,
                minSupportedVersionCode = minSupportedVersionCode,
                latestVersionCode = latestVersionCode,
                updateMode = updateMode,
                storeUrl = storeUrl,
                updateTitle = updateTitle,
                updateMessage = updateMessage,
            )
        } else {
            null
        }
        return HealthStatusState(
            lastResponse = response,
            lastSuccessfulAt = lastSuccessfulAt,
            lastFailureAt = lastFailureAt,
            lastError = preferences.getString(KEY_LAST_ERROR, null),
        )
    }

    private companion object {
        const val PREFS_NAME = "health_status"
        const val KEY_STATUS = "status"
        const val KEY_MIN_SUPPORTED_VERSION_CODE = "min_supported_version_code"
        const val KEY_LATEST_VERSION_CODE = "latest_version_code"
        const val KEY_UPDATE_MODE = "update_mode"
        const val KEY_STORE_URL = "store_url"
        const val KEY_UPDATE_TITLE = "update_title"
        const val KEY_UPDATE_MESSAGE = "update_message"
        const val KEY_LAST_SUCCESSFUL_AT = "last_successful_at"
        const val KEY_LAST_FAILURE_AT = "last_failure_at"
        const val KEY_LAST_ERROR = "last_error"
    }
}
