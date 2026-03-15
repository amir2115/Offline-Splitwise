package com.encer.offlinesplitwise.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import androidx.room.Update
import com.encer.offlinesplitwise.domain.SplitType
import kotlinx.coroutines.flow.Flow

class Converters {
    @TypeConverter
    fun splitTypeToString(splitType: SplitType): String = splitType.name

    @TypeConverter
    fun stringToSplitType(value: String): SplitType = SplitType.valueOf(value)
}

@Dao
interface GroupDao {
    @Query("SELECT * FROM groups ORDER BY createdAt DESC")
    fun observeGroups(): Flow<List<GroupEntity>>

    @Insert
    suspend fun insert(group: GroupEntity): Long

    @Update
    suspend fun update(group: GroupEntity)

    @Query("DELETE FROM groups WHERE id = :groupId")
    suspend fun deleteById(groupId: Long)

    @Query("SELECT * FROM groups WHERE id = :groupId LIMIT 1")
    suspend fun getById(groupId: Long): GroupEntity?
}

@Dao
interface MemberDao {
    @Query("SELECT * FROM members WHERE groupId = :groupId AND isArchived = 0 ORDER BY createdAt ASC")
    fun observeMembers(groupId: Long): Flow<List<MemberEntity>>

    @Insert
    suspend fun insert(member: MemberEntity): Long

    @Update
    suspend fun update(member: MemberEntity)

    @Query("DELETE FROM members WHERE id = :memberId")
    suspend fun deleteById(memberId: Long)

    @Query("SELECT * FROM members WHERE id = :memberId LIMIT 1")
    suspend fun getById(memberId: Long): MemberEntity?
}

@Dao
interface ExpenseDao {
    @Query("SELECT * FROM expenses WHERE groupId = :groupId ORDER BY createdAt DESC")
    fun observeExpenses(groupId: Long): Flow<List<ExpenseEntity>>

    @Query("SELECT * FROM expenses WHERE id = :expenseId LIMIT 1")
    suspend fun getExpenseById(expenseId: Long): ExpenseEntity?

    @Insert
    suspend fun insertExpense(expense: ExpenseEntity): Long

    @Update
    suspend fun updateExpense(expense: ExpenseEntity)

    @Query("DELETE FROM expenses WHERE id = :expenseId")
    suspend fun deleteExpense(expenseId: Long)
}

@Dao
interface TransactionDao {
    @Query("SELECT * FROM expense_payers WHERE expenseId = :expenseId")
    suspend fun getPayers(expenseId: Long): List<ExpensePayerEntity>

    @Query("SELECT * FROM expense_shares WHERE expenseId = :expenseId")
    suspend fun getShares(expenseId: Long): List<ExpenseShareEntity>

    @Query("""
        SELECT p.* FROM expense_payers p
        INNER JOIN expenses e ON e.id = p.expenseId
        WHERE e.groupId = :groupId
    """)
    fun observePayersForGroup(groupId: Long): Flow<List<ExpensePayerEntity>>

    @Query("""
        SELECT s.* FROM expense_shares s
        INNER JOIN expenses e ON e.id = s.expenseId
        WHERE e.groupId = :groupId
    """)
    fun observeSharesForGroup(groupId: Long): Flow<List<ExpenseShareEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayers(payers: List<ExpensePayerEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertShares(shares: List<ExpenseShareEntity>)

    @Query("DELETE FROM expense_payers WHERE expenseId = :expenseId")
    suspend fun deletePayersForExpense(expenseId: Long)

    @Query("DELETE FROM expense_shares WHERE expenseId = :expenseId")
    suspend fun deleteSharesForExpense(expenseId: Long)
}

@Dao
interface SettlementDao {
    @Query("SELECT * FROM settlements WHERE groupId = :groupId ORDER BY createdAt DESC")
    fun observeSettlements(groupId: Long): Flow<List<SettlementEntity>>

    @Query("SELECT * FROM settlements WHERE id = :settlementId LIMIT 1")
    suspend fun getById(settlementId: Long): SettlementEntity?

    @Insert
    suspend fun insert(settlement: SettlementEntity): Long

    @Update
    suspend fun update(settlement: SettlementEntity)

    @Query("DELETE FROM settlements WHERE id = :settlementId")
    suspend fun deleteById(settlementId: Long)
}
