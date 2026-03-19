package com.encer.offlinesplitwise.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BalanceCalculatorTest {
    private val calculator = BalanceCalculator()

    @Test
    fun `equal split keeps totals exact with deterministic remainder`() {
        val validator = ValidateExpenseInputUseCase()

        val validation = validator(
            totalAmount = 1000,
            splitType = SplitType.EQUAL,
            payers = listOf(ExpenseShare("1", 1000)),
            shares = listOf(ExpenseShare("3", 0), ExpenseShare("1", 0), ExpenseShare("2", 0))
        )

        assertTrue(validation.isValid)
        assertEquals(listOf(334, 333, 333), validation.normalizedShares.map { it.amount })
        assertEquals(1000, validation.normalizedShares.sumOf { it.amount })
    }

    @Test
    fun `calculator produces expected balances for mixed expenses and settlements`() {
        val members = listOf(
            Member(id = "1", groupId = "9", name = "A", createdAt = 1, updatedAt = 1),
            Member(id = "2", groupId = "9", name = "B", createdAt = 1, updatedAt = 1),
            Member(id = "3", groupId = "9", name = "C", createdAt = 1, updatedAt = 1)
        )
        val expenses = listOf(
            Expense(
                id = "10",
                groupId = "9",
                title = "round 1",
                note = "",
                totalAmount = 800,
                splitType = SplitType.EXACT,
                createdAt = 1,
                updatedAt = 1,
                payers = listOf(ExpenseShare("1", 800)),
                shares = listOf(
                    ExpenseShare("1", 200),
                    ExpenseShare("2", 500),
                    ExpenseShare("3", 100)
                )
            ),
            Expense(
                id = "11",
                groupId = "9",
                title = "round 2",
                note = "",
                totalAmount = 600,
                splitType = SplitType.EXACT,
                createdAt = 2,
                updatedAt = 2,
                payers = listOf(ExpenseShare("2", 600)),
                shares = listOf(
                    ExpenseShare("1", 200),
                    ExpenseShare("2", 100),
                    ExpenseShare("3", 300)
                )
            )
        )

        val balances = calculator.calculateBalances(members, expenses, settlements = emptyList())

        assertEquals(400, balances.first { it.memberId == "1" }.netBalance)
        assertEquals(0, balances.first { it.memberId == "2" }.netBalance)
        assertEquals(-400, balances.first { it.memberId == "3" }.netBalance)

        val simplified = calculator.simplify(balances)
        assertEquals(1, simplified.size)
        assertEquals("3", simplified.single().fromMemberId)
        assertEquals("1", simplified.single().toMemberId)
        assertEquals(400, simplified.single().amount)
    }

    @Test
    fun `settlement reduces outstanding balances`() {
        val members = listOf(
            Member(id = "1", groupId = "1", name = "Ali", createdAt = 1, updatedAt = 1),
            Member(id = "2", groupId = "1", name = "Sara", createdAt = 1, updatedAt = 1)
        )
        val expenses = listOf(
            Expense(
                id = "1",
                groupId = "1",
                title = "Lunch",
                note = "",
                totalAmount = 500,
                splitType = SplitType.EQUAL,
                createdAt = 1,
                updatedAt = 1,
                payers = listOf(ExpenseShare("1", 500)),
                shares = listOf(ExpenseShare("1", 250), ExpenseShare("2", 250))
            )
        )
        val settlements = listOf(
            Settlement(
                id = "1",
                groupId = "1",
                fromMemberId = "2",
                toMemberId = "1",
                amount = 100,
                note = "",
                createdAt = 2,
                updatedAt = 2
            )
        )

        val balances = calculator.calculateBalances(members, expenses, settlements)

        assertEquals(150, balances.first { it.memberId == "1" }.netBalance)
        assertEquals(-150, balances.first { it.memberId == "2" }.netBalance)
    }
}
