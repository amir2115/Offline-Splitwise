package com.encer.offlinesplitwise.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.encer.offlinesplitwise.data.local.entity.ExpenseEntity
import com.encer.offlinesplitwise.data.local.entity.ExpensePayerEntity
import com.encer.offlinesplitwise.data.local.entity.ExpenseShareEntity

@Dao
interface SyncDao {
    @Transaction
    suspend fun replaceExpenses(expenses: List<ExpenseEntity>, payers: List<ExpensePayerEntity>, shares: List<ExpenseShareEntity>) {
        upsertExpenses(expenses)
        replaceExpenseParticipants(payers, shares)
    }

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertExpenses(expenses: List<ExpenseEntity>)

    @Query("DELETE FROM expense_payers WHERE expenseId IN (:expenseIds)")
    suspend fun deletePayersForExpenses(expenseIds: List<String>)

    @Query("DELETE FROM expense_shares WHERE expenseId IN (:expenseIds)")
    suspend fun deleteSharesForExpenses(expenseIds: List<String>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertPayers(payers: List<ExpensePayerEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertShares(shares: List<ExpenseShareEntity>)

    @Transaction
    suspend fun replaceExpenseParticipants(payers: List<ExpensePayerEntity>, shares: List<ExpenseShareEntity>) {
        val expenseIds = (payers.map { it.expenseId } + shares.map { it.expenseId }).distinct()
        if (expenseIds.isNotEmpty()) {
            deletePayersForExpenses(expenseIds)
            deleteSharesForExpenses(expenseIds)
        }
        if (payers.isNotEmpty()) upsertPayers(payers)
        if (shares.isNotEmpty()) upsertShares(shares)
    }
}
