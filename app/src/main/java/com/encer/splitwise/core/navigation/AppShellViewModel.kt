package com.encer.splitwise.core.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.encer.splitwise.data.preferences.SessionRepository
import com.encer.splitwise.data.preferences.SettingsRepository
import com.encer.splitwise.data.sync.NetworkMonitor
import com.encer.splitwise.data.sync.SyncCoordinator
import com.encer.splitwise.data.update.AppUpdateChecker
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.launch

@HiltViewModel
class AppShellViewModel @Inject constructor(
    val settingsRepository: SettingsRepository,
    val sessionRepository: SessionRepository,
    val syncCoordinator: SyncCoordinator,
    val networkMonitor: NetworkMonitor,
    val appUpdateChecker: AppUpdateChecker,
) : ViewModel() {
    fun refreshConnectionStatus() {
        viewModelScope.launch {
            networkMonitor.refreshApiReachability()
        }
    }

    init {
        viewModelScope.launch {
            syncCoordinator.restoreSessionAndSync()
            networkMonitor.observeStatus().collect { status ->
                if (status.hasInternet && status.isApiReachable) {
                    syncCoordinator.requestSync()
                    appUpdateChecker.refreshUpdatePolicy()
                }
            }
        }
    }
}
