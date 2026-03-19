package com.encer.offlinesplitwise.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.encer.offlinesplitwise.data.local.entity.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpenseDao {
    @Query("SELECT * FROM expenses WHERE groupId = :groupId AND deletedAt IS NULL ORDER BY createdAt DESC")
    fun observeExpenses(groupId: String): Flow<List<ExpenseEntity>>

    @Query("SELECT * FROM expenses ORDER BY createdAt DESC")
    suspend fun getAll(): List<ExpenseEntity>

    @Query("SELECT * FROM expenses WHERE syncState = :syncState")
    suspend fun getBySyncState(syncState: SyncState): List<ExpenseEntity>

    @Query("SELECT * FROM expenses WHERE id = :expenseId LIMIT 1")
    suspend fun getExpenseById(expenseId: String): ExpenseEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertExpense(expense: ExpenseEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertExpenses(expenses: List<ExpenseEntity>)

    @Query("DELETE FROM expenses WHERE id = :expenseId")
    suspend fun hardDeleteExpense(expenseId: String)

    @Query("DELETE FROM expenses WHERE id IN (:expenseIds)")
    suspend fun hardDeleteExpenses(expenseIds: List<String>)
}
