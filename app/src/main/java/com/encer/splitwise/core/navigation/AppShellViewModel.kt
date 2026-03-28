package com.encer.splitwise.core.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.encer.splitwise.data.preferences.HealthStatusRepository
import com.encer.splitwise.data.preferences.SessionRepository
import com.encer.splitwise.data.preferences.SettingsRepository
import com.encer.splitwise.data.sync.NetworkMonitor
import com.encer.splitwise.data.sync.SyncCoordinator
import com.encer.splitwise.data.update.AppUpdateChecker
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@HiltViewModel
class AppShellViewModel @Inject constructor(
    val settingsRepository: SettingsRepository,
    val sessionRepository: SessionRepository,
    val healthStatusRepository: HealthStatusRepository,
    val syncCoordinator: SyncCoordinator,
    val networkMonitor: NetworkMonitor,
    val appUpdateChecker: AppUpdateChecker,
    private val postAuthBootstrapper: PostAuthBootstrapper,
) : ViewModel() {
    fun refreshConnectionStatus() {
        viewModelScope.launch {
            networkMonitor.refreshApiReachability()
        }
    }

    suspend fun login(username: String, password: String): Result<*> {
        val result = syncCoordinator.login(username, password)
        if (result.isSuccess) {
            postAuthBootstrapper.run()
        }
        return result
    }

    suspend fun register(name: String, username: String, password: String): Result<*> {
        val result = syncCoordinator.register(name, username, password)
        if (result.isSuccess) {
            postAuthBootstrapper.run()
        }
        return result
    }

    init {
        viewModelScope.launch {
            if (sessionRepository.currentSession() != null) {
                postAuthBootstrapper.run()
            }
            networkMonitor.observeStatus().collect { status ->
                if (status.hasInternet && status.isApiReachable) {
                    syncCoordinator.requestSync()
                    appUpdateChecker.refreshUpdatePolicy()
                }
            }
        }
    }
}
