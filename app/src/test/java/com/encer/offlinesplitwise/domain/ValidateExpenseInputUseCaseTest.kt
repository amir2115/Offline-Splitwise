package com.encer.offlinesplitwise.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ValidateExpenseInputUseCaseTest {
    private val validator = ValidateExpenseInputUseCase()

    @Test
    fun `exact split rejects mismatched shares`() {
        val validation = validator(
            totalAmount = 700,
            splitType = SplitType.EXACT,
            payers = listOf(ExpenseShare(1, 700)),
            shares = listOf(ExpenseShare(1, 300), ExpenseShare(2, 300))
        )

        assertFalse(validation.isValid)
        assertEquals("جمع سهم‌ها باید با مبلغ کل برابر باشد.", validation.message)
    }

    @Test
    fun `equal split rejects invalid payer total`() {
        val validation = validator(
            totalAmount = 900,
            splitType = SplitType.EQUAL,
            payers = listOf(ExpenseShare(1, 500), ExpenseShare(2, 200)),
            shares = listOf(ExpenseShare(1, 0), ExpenseShare(2, 0), ExpenseShare(3, 0))
        )

        assertFalse(validation.isValid)
        assertEquals("جمع پرداخت‌کننده‌ها باید با مبلغ کل برابر باشد.", validation.message)
    }

    @Test
    fun `exact split accepts valid manual shares`() {
        val validation = validator(
            totalAmount = 900,
            splitType = SplitType.EXACT,
            payers = listOf(ExpenseShare(1, 600), ExpenseShare(2, 300)),
            shares = listOf(ExpenseShare(1, 300), ExpenseShare(2, 300), ExpenseShare(3, 300))
        )

        assertTrue(validation.isValid)
        assertEquals(900, validation.normalizedShares.sumOf { it.amount })
    }
}
