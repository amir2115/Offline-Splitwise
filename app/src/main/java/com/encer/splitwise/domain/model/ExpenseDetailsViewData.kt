package com.encer.splitwise.domain.model

data class ExpenseDetailsViewData(
    val expense: Expense,
    val payerLabels: List<String>,
    val shareLabels: List<String>,
)
