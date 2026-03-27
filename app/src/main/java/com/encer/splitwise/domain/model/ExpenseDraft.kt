package com.encer.splitwise.domain.model

data class ExpenseDraft(
    val groupId: String,
    val title: String,
    val note: String?,
    val totalAmount: Int,
    val splitType: SplitType,
    val payers: List<ExpenseShare>,
    val shares: List<ExpenseShare>,
)
