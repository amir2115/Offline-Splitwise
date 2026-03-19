package com.encer.offlinesplitwise.core.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.encer.offlinesplitwise.data.preferences.SessionRepository
import com.encer.offlinesplitwise.data.preferences.SettingsRepository
import com.encer.offlinesplitwise.data.sync.NetworkMonitor
import com.encer.offlinesplitwise.data.sync.SyncCoordinator
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.launch

@HiltViewModel
class AppShellViewModel @Inject constructor(
    val settingsRepository: SettingsRepository,
    val sessionRepository: SessionRepository,
    val syncCoordinator: SyncCoordinator,
    val networkMonitor: NetworkMonitor,
) : ViewModel() {
    init {
        viewModelScope.launch {
            syncCoordinator.restoreSessionAndSync()
            networkMonitor.observeStatus().collect { status ->
                if (status.hasInternet && status.isApiReachable) {
                    syncCoordinator.requestSync()
                }
            }
        }
    }
}
