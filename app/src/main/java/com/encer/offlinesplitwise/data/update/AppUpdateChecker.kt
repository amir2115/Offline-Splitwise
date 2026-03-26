package com.encer.offlinesplitwise.data.update

import android.content.Context
import androidx.core.content.pm.PackageInfoCompat
import com.encer.offlinesplitwise.BuildConfig
import com.encer.offlinesplitwise.data.remote.network.ApiClient
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

enum class AppUpdateMode {
    NONE,
    SOFT,
    HARD,
}

data class AppUpdateState(
    val mode: AppUpdateMode = AppUpdateMode.NONE,
    val storeUrl: String? = null,
    val title: String? = null,
    val message: String? = null,
) {
    val isVisible: Boolean get() = mode != AppUpdateMode.NONE && !storeUrl.isNullOrBlank()
}

@Singleton
class AppUpdateChecker @Inject constructor(
    @ApplicationContext private val context: Context,
    private val apiClient: ApiClient,
) {
    private val _updateState = MutableStateFlow(AppUpdateState())

    fun observeUpdateState(): StateFlow<AppUpdateState> = _updateState

    suspend fun refreshUpdatePolicy() {
        val payload = runCatching { apiClient.health() }.getOrNull() ?: return
        val currentVersionCode = PackageInfoCompat.getLongVersionCode(
            context.packageManager.getPackageInfo(context.packageName, 0)
        ).toInt()
        _updateState.value = resolveAppUpdateState(currentVersionCode = currentVersionCode, payload = payload)
    }

    fun dismissSoftUpdate() {
        if (_updateState.value.mode == AppUpdateMode.SOFT) {
            _updateState.value = AppUpdateState()
        }
    }
}

fun resolveAppUpdateState(
    currentVersionCode: Int,
    payload: com.encer.offlinesplitwise.data.remote.model.HealthCheckResponse,
): AppUpdateState {
    val minSupported = payload.minSupportedVersionCode
    val latest = payload.latestVersionCode
    val mode = when {
        minSupported != null && currentVersionCode < minSupported -> AppUpdateMode.HARD
        latest != null && currentVersionCode < latest && payload.updateMode.equals("soft", ignoreCase = true) -> AppUpdateMode.SOFT
        latest != null && currentVersionCode < latest && payload.updateMode.equals("hard", ignoreCase = true) -> AppUpdateMode.HARD
        else -> AppUpdateMode.NONE
    }

    return AppUpdateState(
        mode = mode,
        storeUrl = payload.storeUrl ?: BuildConfig.DEFAULT_STORE_URL.takeIf { it.isNotBlank() },
        title = payload.updateTitle,
        message = payload.updateMessage,
    )
}
