package com.encer.splitwise.domain.usecase

import com.encer.splitwise.core.common.MessageKey
import com.encer.splitwise.domain.model.ExpenseDraftValidation
import com.encer.splitwise.domain.model.ExpenseShare
import com.encer.splitwise.domain.model.SplitType
import com.encer.splitwise.domain.usecase.base.BaseUseCase
import javax.inject.Inject

data class ValidateExpenseInputParams(
    val totalAmount: Int,
    val splitType: SplitType,
    val payers: List<ExpenseShare>,
    val shares: List<ExpenseShare>,
)

class ValidateExpenseInputUseCase @Inject constructor() : BaseUseCase<ValidateExpenseInputParams, ExpenseDraftValidation>() {
    override fun invoke(params: ValidateExpenseInputParams): ExpenseDraftValidation {
        val totalAmount = params.totalAmount
        val splitType = params.splitType
        val payers = params.payers
        val shares = params.shares
        if (totalAmount <= 0) return ExpenseDraftValidation(false, MessageKey.EXPENSE_TOTAL_POSITIVE)
        if (payers.isEmpty()) return ExpenseDraftValidation(false, MessageKey.EXPENSE_AT_LEAST_ONE_PAYER)
        if (shares.isEmpty()) return ExpenseDraftValidation(false, MessageKey.EXPENSE_AT_LEAST_ONE_SHARE)
        if (payers.any { it.amount < 0 } || shares.any { it.amount < 0 }) {
            return ExpenseDraftValidation(false, MessageKey.EXPENSE_NEGATIVE_VALUES)
        }
        if (payers.sumOf { it.amount } != totalAmount) {
            return ExpenseDraftValidation(false, MessageKey.EXPENSE_PAYER_TOTAL_MISMATCH)
        }
        return when (splitType) {
            SplitType.EXACT ->
                if (shares.sumOf { it.amount } != totalAmount) {
                    ExpenseDraftValidation(false, MessageKey.EXPENSE_SHARE_TOTAL_MISMATCH)
                } else {
                    ExpenseDraftValidation(true, normalizedShares = shares)
                }
            SplitType.EQUAL -> ExpenseDraftValidation(true, normalizedShares = splitEqually(totalAmount, shares.map { it.memberId }))
        }
    }

    fun splitEqually(totalAmount: Int, memberIds: List<String>): List<ExpenseShare> {
        if (memberIds.isEmpty()) return emptyList()
        val base = totalAmount / memberIds.size
        var remainder = totalAmount % memberIds.size
        return memberIds.sorted().map { memberId ->
            val extra = if (remainder > 0) {
                remainder -= 1
                1
            } else {
                0
            }
            ExpenseShare(memberId = memberId, amount = base + extra)
        }
    }
}
