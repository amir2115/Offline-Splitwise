package com.encer.offlinesplitwise.domain.repository

import com.encer.offlinesplitwise.domain.model.Expense
import com.encer.offlinesplitwise.domain.model.ExpenseDraft
import kotlinx.coroutines.flow.Flow

interface ExpenseRepository {
    fun observeExpenses(groupId: String): Flow<List<Expense>>
    suspend fun getExpense(expenseId: String): Expense?
    suspend fun upsertExpense(draft: ExpenseDraft, existingId: String? = null): String
    suspend fun deleteExpense(expenseId: String)
}
