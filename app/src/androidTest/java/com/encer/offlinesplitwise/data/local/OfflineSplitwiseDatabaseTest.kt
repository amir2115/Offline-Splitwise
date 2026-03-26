package com.encer.offlinesplitwise.data.local

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.encer.offlinesplitwise.data.local.db.OfflineSplitwiseDatabase
import com.encer.offlinesplitwise.data.local.entity.ExpenseEntity
import com.encer.offlinesplitwise.data.local.entity.ExpensePayerEntity
import com.encer.offlinesplitwise.data.local.entity.ExpenseShareEntity
import com.encer.offlinesplitwise.data.local.entity.GroupEntity
import com.encer.offlinesplitwise.data.local.entity.MemberEntity
import com.encer.offlinesplitwise.data.local.entity.SettlementEntity
import com.encer.offlinesplitwise.domain.model.SplitType
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class OfflineSplitwiseDatabaseTest {
    private lateinit var database: OfflineSplitwiseDatabase

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            OfflineSplitwiseDatabase::class.java
        ).build()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun deletingGroupCascadesMembersExpensesAndSettlements() = runBlocking {
        val groupId = "group-trip"
        val aliId = "member-ali"
        val saraId = "member-sara"
        val expenseId = "expense-dinner"

        database.groupDao().upsert(
            GroupEntity(id = groupId, name = "Trip", createdAt = 1, updatedAt = 1)
        )
        database.memberDao().upsert(
            MemberEntity(id = aliId, groupId = groupId, username = "Ali", createdAt = 1, updatedAt = 1)
        )
        database.memberDao().upsert(
            MemberEntity(id = saraId, groupId = groupId, username = "Sara", createdAt = 1, updatedAt = 1)
        )
        database.expenseDao().upsertExpense(
            ExpenseEntity(
                id = expenseId,
                groupId = groupId,
                title = "Dinner",
                note = "",
                totalAmount = 400,
                splitType = SplitType.EQUAL,
                createdAt = 1,
                updatedAt = 1
            )
        )
        database.transactionDao().insertPayers(listOf(ExpensePayerEntity(expenseId, aliId, 400)))
        database.transactionDao().insertShares(
            listOf(
                ExpenseShareEntity(expenseId, aliId, 200),
                ExpenseShareEntity(expenseId, saraId, 200)
            )
        )
        database.settlementDao().upsert(
            SettlementEntity(
                id = "settlement-1",
                groupId = groupId,
                fromMemberId = saraId,
                toMemberId = aliId,
                amount = 100,
                note = "",
                createdAt = 2,
                updatedAt = 2,
            )
        )

        database.groupDao().hardDeleteById(groupId)

        assertEquals(emptyList<MemberEntity>(), database.memberDao().observeMembers(groupId).firstValue())
        assertEquals(emptyList<ExpenseEntity>(), database.expenseDao().observeExpenses(groupId).firstValue())
        assertEquals(emptyList<SettlementEntity>(), database.settlementDao().observeSettlements(groupId).firstValue())
    }
}
