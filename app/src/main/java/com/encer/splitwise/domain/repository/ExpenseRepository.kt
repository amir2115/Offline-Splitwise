package com.encer.splitwise.domain.repository

import com.encer.splitwise.domain.model.Expense
import com.encer.splitwise.domain.model.ExpenseDraft
import kotlinx.coroutines.flow.Flow

interface ExpenseRepository {
    fun observeExpenses(groupId: String): Flow<List<Expense>>
    suspend fun getExpense(expenseId: String): Expense?
    suspend fun upsertExpense(draft: ExpenseDraft, existingId: String? = null): String
    suspend fun deleteExpense(expenseId: String)
}
