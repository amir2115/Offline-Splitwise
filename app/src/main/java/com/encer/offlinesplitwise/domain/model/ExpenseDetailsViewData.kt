package com.encer.offlinesplitwise.domain.model

data class ExpenseDetailsViewData(
    val expense: Expense,
    val payerLabels: List<String>,
    val shareLabels: List<String>,
)
