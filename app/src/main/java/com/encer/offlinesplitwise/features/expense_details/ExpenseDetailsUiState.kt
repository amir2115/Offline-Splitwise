package com.encer.offlinesplitwise.features.expense_details

import com.encer.offlinesplitwise.domain.model.Expense
import com.encer.offlinesplitwise.domain.model.Member

data class ExpenseDetailsUiState(
    val expense: Expense? = null,
    val members: List<Member> = emptyList(),
)
