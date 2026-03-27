package com.encer.splitwise.data.local.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.encer.splitwise.data.local.dao.*
import com.encer.splitwise.data.local.entity.*

@Database(
    entities = [
        GroupEntity::class,
        MemberEntity::class,
        ExpenseEntity::class,
        ExpensePayerEntity::class,
        ExpenseShareEntity::class,
        SettlementEntity::class,
    ],
    version = 4,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class SplitwiseDatabase : RoomDatabase() {
    abstract fun groupDao(): GroupDao
    abstract fun memberDao(): MemberDao
    abstract fun expenseDao(): ExpenseDao
    abstract fun transactionDao(): TransactionDao
    abstract fun settlementDao(): SettlementDao
    abstract fun syncDao(): SyncDao

    companion object {
        fun create(context: Context): SplitwiseDatabase {
            return Room.databaseBuilder(
                context,
                SplitwiseDatabase::class.java,
                "splitwise.db"
            ).addMigrations(MIGRATION_3_4).build()
        }
    }
}
