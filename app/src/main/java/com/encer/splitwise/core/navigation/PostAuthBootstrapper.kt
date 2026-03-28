package com.encer.splitwise.core.navigation

import com.encer.splitwise.data.sync.NetworkMonitor
import com.encer.splitwise.data.sync.SyncCoordinator
import com.encer.splitwise.data.update.AppUpdateChecker
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PostAuthBootstrapper internal constructor(
    private val refreshReachability: suspend () -> Unit,
    private val refreshUpdatePolicy: suspend () -> Unit,
    private val restoreAndSyncSession: suspend () -> Unit,
) {
    @Inject
    constructor(
        networkMonitor: NetworkMonitor,
        appUpdateChecker: AppUpdateChecker,
        syncCoordinator: SyncCoordinator,
    ) : this(
        refreshReachability = { networkMonitor.refreshApiReachability(forceRequest = true) },
        refreshUpdatePolicy = { appUpdateChecker.refreshUpdatePolicy() },
        restoreAndSyncSession = { syncCoordinator.restoreSessionAndSync(forceNetworkRequest = true) },
    )

    suspend fun run() {
        refreshReachability()
        refreshUpdatePolicy()
        restoreAndSyncSession()
    }
}
