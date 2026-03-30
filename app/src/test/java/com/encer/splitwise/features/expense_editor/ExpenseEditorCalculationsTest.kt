package com.encer.splitwise.features.expense_editor

import com.encer.splitwise.domain.model.SplitType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ExpenseEditorCalculationsTest {

    @Test
    fun `computeExpenseEditorState calculates remaining payer and base share amounts`() {
        val members = listOf(
            MemberDraftUi(memberId = "1", username = "a", payerAmountInput = "8000", exactShareInput = "3000"),
            MemberDraftUi(memberId = "2", username = "b", payerAmountInput = "2000", exactShareInput = "1000"),
            MemberDraftUi(memberId = "3", username = "c"),
        )

        val state = computeExpenseEditorState(
            totalAmount = 13000,
            splitType = SplitType.EXACT,
            members = members,
            taxEnabled = false,
            taxPercentInput = "",
            serviceCharges = emptyList(),
        )

        assertEquals(10000, state.payerTotal)
        assertEquals(3000, state.remainingPayerAmount)
        assertEquals(4000, state.baseShareTotal)
        assertEquals(9000, state.remainingBaseShareAmount)
    }

    @Test
    fun `computeExpenseEditorState derives base tax and service breakdown`() {
        val state = computeExpenseEditorState(
            totalAmount = 100000,
            splitType = SplitType.EXACT,
            members = listOf(
                MemberDraftUi(memberId = "1", username = "a", exactShareInput = "59091"),
                MemberDraftUi(memberId = "2", username = "b", exactShareInput = "27273"),
            ),
            taxEnabled = true,
            taxPercentInput = "10",
            serviceCharges = listOf(
                ServiceChargeDraftUi(
                    id = "svc1",
                    title = "service",
                    amountInput = "5000",
                    selectedMemberIds = setOf("1", "2"),
                )
            ),
        )

        assertEquals(5000, state.serviceChargeTotalPreview)
        assertEquals(86364, state.baseAmountPreview)
        assertEquals(8636, state.taxAmountPreview)
        assertEquals(100000, state.finalShareTotal)
        assertEquals(67500, state.memberBreakdowns.getValue("1").finalShare)
        assertEquals(32500, state.memberBreakdowns.getValue("2").finalShare)
    }

    @Test
    fun `service charge without members is invalid`() {
        val state = computeExpenseEditorState(
            totalAmount = 10000,
            splitType = SplitType.EQUAL,
            members = listOf(
                MemberDraftUi(memberId = "1", username = "a"),
                MemberDraftUi(memberId = "2", username = "b"),
            ),
            taxEnabled = false,
            taxPercentInput = "",
            serviceCharges = listOf(
                ServiceChargeDraftUi(id = "svc1", title = "service", amountInput = "2000", selectedMemberIds = emptySet())
            ),
        )

        assertTrue(state.hasInvalidServiceCharges)
    }

    @Test
    fun `splitAmountDeterministically distributes remainder predictably`() {
        val split = splitAmountDeterministically(5, listOf("b", "a"))
        assertEquals(3, split.getValue("a"))
        assertEquals(2, split.getValue("b"))
    }

    @Test
    fun `distributeProportionally keeps exact total`() {
        val distributed = distributeProportionally(
            total = 550,
            weights = mapOf("1" to 3000, "2" to 2000)
        )

        assertEquals(550, distributed.values.sum())
        assertFalse(distributed.values.any { it < 0 })
    }
}
