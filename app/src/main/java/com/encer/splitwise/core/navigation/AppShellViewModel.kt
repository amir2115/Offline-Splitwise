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
import kotlinx.coroutines.Job
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
    private var postAuthBootstrapJob: Job? = null

    fun refreshConnectionStatus() {
        viewModelScope.launch {
            networkMonitor.refreshApiReachability()
        }
    }

    private fun launchPostAuthBootstrap() {
        if (postAuthBootstrapJob?.isActive == true) return
        postAuthBootstrapJob = viewModelScope.launch {
            postAuthBootstrapper.run()
        }
    }

    suspend fun login(username: String, password: String): Result<*> {
        val result = syncCoordinator.login(username, password)
        if (result.isSuccess) {
            launchPostAuthBootstrap()
        }
        return result
    }

    suspend fun register(name: String, username: String, password: String): Result<*> {
        val result = syncCoordinator.register(name, username, password)
        if (result.isSuccess) {
            launchPostAuthBootstrap()
        }
        return result
    }

    init {
        viewModelScope.launch {
            if (sessionRepository.currentSession() != null) {
                launchPostAuthBootstrap()
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
