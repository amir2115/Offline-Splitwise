package com.encer.splitwise.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.encer.splitwise.data.local.entity.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Query("SELECT * FROM expense_payers WHERE expenseId = :expenseId")
    suspend fun getPayers(expenseId: String): List<ExpensePayerEntity>

    @Query("SELECT * FROM expense_shares WHERE expenseId = :expenseId")
    suspend fun getShares(expenseId: String): List<ExpenseShareEntity>

    @Query(
        """
        SELECT p.* FROM expense_payers p
        INNER JOIN expenses e ON e.id = p.expenseId
        WHERE e.groupId = :groupId AND e.deletedAt IS NULL
        """
    )
    fun observePayersForGroup(groupId: String): Flow<List<ExpensePayerEntity>>

    @Query(
        """
        SELECT s.* FROM expense_shares s
        INNER JOIN expenses e ON e.id = s.expenseId
        WHERE e.groupId = :groupId AND e.deletedAt IS NULL
        """
    )
    fun observeSharesForGroup(groupId: String): Flow<List<ExpenseShareEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayers(payers: List<ExpensePayerEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertShares(shares: List<ExpenseShareEntity>)

    @Query("DELETE FROM expense_payers WHERE expenseId = :expenseId")
    suspend fun deletePayersForExpense(expenseId: String)

    @Query("DELETE FROM expense_shares WHERE expenseId = :expenseId")
    suspend fun deleteSharesForExpense(expenseId: String)
}
