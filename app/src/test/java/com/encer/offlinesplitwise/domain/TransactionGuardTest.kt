package com.encer.offlinesplitwise.domain

import com.encer.offlinesplitwise.domain.usecase.canCreateTransaction
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TransactionGuardTest {
    @Test
    fun `single-member group cannot create new transaction`() {
        assertFalse(canCreateTransaction(memberCount = 1, isEdit = false))
    }

    @Test
    fun `two-member group can create new transaction`() {
        assertTrue(canCreateTransaction(memberCount = 2, isEdit = false))
    }

    @Test
    fun `editing existing transaction stays allowed even with one member`() {
        assertTrue(canCreateTransaction(memberCount = 1, isEdit = true))
    }
}
