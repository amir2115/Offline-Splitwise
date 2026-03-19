package com.encer.offlinesplitwise.core.di

import android.content.Context
import com.encer.offlinesplitwise.data.local.dao.ExpenseDao
import com.encer.offlinesplitwise.data.local.dao.GroupDao
import com.encer.offlinesplitwise.data.local.dao.MemberDao
import com.encer.offlinesplitwise.data.local.db.OfflineSplitwiseDatabase
import com.encer.offlinesplitwise.data.local.dao.SettlementDao
import com.encer.offlinesplitwise.data.local.dao.SyncDao
import com.encer.offlinesplitwise.data.local.dao.TransactionDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): OfflineSplitwiseDatabase =
        OfflineSplitwiseDatabase.create(context)

    @Provides fun provideGroupDao(database: OfflineSplitwiseDatabase): GroupDao = database.groupDao()
    @Provides fun provideMemberDao(database: OfflineSplitwiseDatabase): MemberDao = database.memberDao()
    @Provides fun provideExpenseDao(database: OfflineSplitwiseDatabase): ExpenseDao = database.expenseDao()
    @Provides fun provideTransactionDao(database: OfflineSplitwiseDatabase): TransactionDao = database.transactionDao()
    @Provides fun provideSettlementDao(database: OfflineSplitwiseDatabase): SettlementDao = database.settlementDao()
    @Provides fun provideSyncDao(database: OfflineSplitwiseDatabase): SyncDao = database.syncDao()
}
