package com.encer.offlinesplitwise.data.local.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.encer.offlinesplitwise.data.local.dao.*
import com.encer.offlinesplitwise.data.local.entity.*

@Database(
    entities = [
        GroupEntity::class,
        MemberEntity::class,
        ExpenseEntity::class,
        ExpensePayerEntity::class,
        ExpenseShareEntity::class,
        SettlementEntity::class,
    ],
    version = 3,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class OfflineSplitwiseDatabase : RoomDatabase() {
    abstract fun groupDao(): GroupDao
    abstract fun memberDao(): MemberDao
    abstract fun expenseDao(): ExpenseDao
    abstract fun transactionDao(): TransactionDao
    abstract fun settlementDao(): SettlementDao
    abstract fun syncDao(): SyncDao

    companion object {
        fun create(context: Context): OfflineSplitwiseDatabase {
            return Room.databaseBuilder(
                context,
                OfflineSplitwiseDatabase::class.java,
                "offline_splitwise.db"
            ).fallbackToDestructiveMigration().build()
        }
    }
}
