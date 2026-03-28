package com.encer.splitwise.core.di

import com.encer.splitwise.data.local.dao.ExpenseDao
import com.encer.splitwise.data.local.dao.GroupDao
import com.encer.splitwise.data.local.dao.MemberDao
import com.encer.splitwise.data.local.db.SplitwiseDatabase
import com.encer.splitwise.data.local.dao.SettlementDao
import com.encer.splitwise.data.local.dao.SyncDao
import com.encer.splitwise.data.local.dao.TransactionDao
import com.encer.splitwise.data.preferences.HealthStatusRepository
import com.encer.splitwise.data.preferences.SessionRepository
import com.encer.splitwise.data.remote.network.ApiClient
import com.encer.splitwise.data.sync.NetworkMonitor
import com.encer.splitwise.data.sync.SyncCoordinator
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

@Module
@InstallIn(SingletonComponent::class)
object SyncModule {
    @Provides
    @Singleton
    fun provideApplicationScope(): CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    @Provides
    @Singleton
    fun provideNetworkMonitor(
        @dagger.hilt.android.qualifiers.ApplicationContext context: android.content.Context,
        apiClient: ApiClient,
        healthStatusRepository: HealthStatusRepository,
        applicationScope: CoroutineScope,
    ): NetworkMonitor = NetworkMonitor(context, apiClient, healthStatusRepository, applicationScope)

    @Provides
    @Singleton
    fun provideSyncCoordinator(
        database: SplitwiseDatabase,
        groupDao: GroupDao,
        memberDao: MemberDao,
        expenseDao: ExpenseDao,
        transactionDao: TransactionDao,
        settlementDao: SettlementDao,
        syncDao: SyncDao,
        sessionRepository: SessionRepository,
        apiClient: ApiClient,
        networkMonitor: NetworkMonitor,
        applicationScope: CoroutineScope,
    ): SyncCoordinator = SyncCoordinator(
        database = database,
        groupDao = groupDao,
        memberDao = memberDao,
        expenseDao = expenseDao,
        transactionDao = transactionDao,
        settlementDao = settlementDao,
        syncDao = syncDao,
        sessionRepository = sessionRepository,
        apiClient = apiClient,
        networkMonitor = networkMonitor,
        scope = applicationScope
    )
}
