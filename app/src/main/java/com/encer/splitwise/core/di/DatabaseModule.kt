package com.encer.splitwise.core.di

import android.content.Context
import com.encer.splitwise.data.local.dao.ExpenseDao
import com.encer.splitwise.data.local.dao.GroupDao
import com.encer.splitwise.data.local.dao.MemberDao
import com.encer.splitwise.data.local.db.SplitwiseDatabase
import com.encer.splitwise.data.local.dao.SettlementDao
import com.encer.splitwise.data.local.dao.SyncDao
import com.encer.splitwise.data.local.dao.TransactionDao
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
    fun provideDatabase(@ApplicationContext context: Context): SplitwiseDatabase =
        SplitwiseDatabase.create(context)

    @Provides fun provideGroupDao(database: SplitwiseDatabase): GroupDao = database.groupDao()
    @Provides fun provideMemberDao(database: SplitwiseDatabase): MemberDao = database.memberDao()
    @Provides fun provideExpenseDao(database: SplitwiseDatabase): ExpenseDao = database.expenseDao()
    @Provides fun provideTransactionDao(database: SplitwiseDatabase): TransactionDao = database.transactionDao()
    @Provides fun provideSettlementDao(database: SplitwiseDatabase): SettlementDao = database.settlementDao()
    @Provides fun provideSyncDao(database: SplitwiseDatabase): SyncDao = database.syncDao()
}
