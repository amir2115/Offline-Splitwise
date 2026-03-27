package com.encer.splitwise.features.expense_details

import com.encer.splitwise.domain.model.Expense
import com.encer.splitwise.domain.model.Member

data class ExpenseDetailsUiState(
    val expense: Expense? = null,
    val members: List<Member> = emptyList(),
)
