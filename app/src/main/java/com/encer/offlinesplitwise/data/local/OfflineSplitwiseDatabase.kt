package com.encer.offlinesplitwise.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [
        GroupEntity::class,
        MemberEntity::class,
        ExpenseEntity::class,
        ExpensePayerEntity::class,
        ExpenseShareEntity::class,
        SettlementEntity::class,
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class OfflineSplitwiseDatabase : RoomDatabase() {
    abstract fun groupDao(): GroupDao
    abstract fun memberDao(): MemberDao
    abstract fun expenseDao(): ExpenseDao
    abstract fun transactionDao(): TransactionDao
    abstract fun settlementDao(): SettlementDao

    companion object {
        fun create(context: Context): OfflineSplitwiseDatabase {
            return Room.databaseBuilder(
                context,
                OfflineSplitwiseDatabase::class.java,
                "offline_splitwise.db"
            ).fallbackToDestructiveMigration(false).build()
        }
    }
}
