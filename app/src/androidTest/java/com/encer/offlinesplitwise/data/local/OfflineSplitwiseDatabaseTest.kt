package com.encer.offlinesplitwise.data.local

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
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
        val groupId = database.groupDao().insert(GroupEntity(name = "Trip", createdAt = 1))
        val aliId = database.memberDao().insert(MemberEntity(groupId = groupId, name = "Ali", createdAt = 1))
        val saraId = database.memberDao().insert(MemberEntity(groupId = groupId, name = "Sara", createdAt = 1))
        val expenseId = database.expenseDao().insertExpense(
            ExpenseEntity(
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
        database.settlementDao().insert(
            SettlementEntity(
                groupId = groupId,
                fromMemberId = saraId,
                toMemberId = aliId,
                amount = 100,
                note = "",
                createdAt = 2
            )
        )

        database.groupDao().deleteById(groupId)

        assertEquals(emptyList<MemberEntity>(), database.memberDao().observeMembers(groupId).firstValue())
        assertEquals(emptyList<ExpenseEntity>(), database.expenseDao().observeExpenses(groupId).firstValue())
        assertEquals(emptyList<SettlementEntity>(), database.settlementDao().observeSettlements(groupId).firstValue())
    }
}
