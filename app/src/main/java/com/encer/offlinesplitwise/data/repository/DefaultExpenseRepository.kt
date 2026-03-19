package com.encer.offlinesplitwise.data.repository

import com.encer.offlinesplitwise.data.local.dao.ExpenseDao
import com.encer.offlinesplitwise.data.local.entity.ExpenseEntity
import com.encer.offlinesplitwise.data.local.entity.ExpensePayerEntity
import com.encer.offlinesplitwise.data.local.entity.ExpenseShareEntity
import com.encer.offlinesplitwise.data.local.entity.SyncState
import com.encer.offlinesplitwise.data.local.dao.TransactionDao
import com.encer.offlinesplitwise.data.repository.mapper.toDomain
import com.encer.offlinesplitwise.data.repository.mapper.toDomainPayer
import com.encer.offlinesplitwise.data.repository.mapper.toDomainShare
import com.encer.offlinesplitwise.data.sync.SyncCoordinator
import com.encer.offlinesplitwise.domain.model.Expense
import com.encer.offlinesplitwise.domain.model.ExpenseDraft
import com.encer.offlinesplitwise.domain.repository.ExpenseRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.util.UUID

class DefaultExpenseRepository @Inject constructor(
    private val expenseDao: ExpenseDao,
    private val transactionDao: TransactionDao,
    private val syncCoordinator: SyncCoordinator
) : ExpenseRepository {
    override fun observeExpenses(groupId: String): Flow<List<Expense>> {
        return combine(
            expenseDao.observeExpenses(groupId),
            transactionDao.observePayersForGroup(groupId),
            transactionDao.observeSharesForGroup(groupId)
        ) { expenses, payers, shares ->
            expenses.map { expense ->
                expense.toDomain(
                    payers = payers.filter { it.expenseId == expense.id }.map { it.toDomainPayer() },
                    shares = shares.filter { it.expenseId == expense.id }.map { it.toDomainShare() }
                )
            }
        }
    }

    override suspend fun getExpense(expenseId: String): Expense? {
        val expense = expenseDao.getExpenseById(expenseId)?.takeIf { it.deletedAt == null } ?: return null
        return expense.toDomain(
            payers = transactionDao.getPayers(expenseId).map { it.toDomainPayer() },
            shares = transactionDao.getShares(expenseId).map { it.toDomainShare() }
        )
    }

    override suspend fun upsertExpense(draft: ExpenseDraft, existingId: String?): String {
        val now = System.currentTimeMillis()
        val expenseId = existingId ?: UUID.randomUUID().toString()
        val current = existingId?.let { expenseDao.getExpenseById(it) }
        expenseDao.upsertExpense(
            ExpenseEntity(
                id = expenseId,
                groupId = draft.groupId,
                title = draft.title.trim(),
                note = draft.note?.trim().orEmpty().ifBlank { null },
                totalAmount = draft.totalAmount,
                splitType = draft.splitType,
                createdAt = current?.createdAt ?: now,
                updatedAt = now,
                deletedAt = null,
                userId = current?.userId,
                syncState = SyncState.PENDING_UPSERT
            )
        )
        transactionDao.deletePayersForExpense(expenseId)
        transactionDao.deleteSharesForExpense(expenseId)
        transactionDao.insertPayers(draft.payers.map { ExpensePayerEntity(expenseId, it.memberId, it.amount) })
        transactionDao.insertShares(draft.shares.map { ExpenseShareEntity(expenseId, it.memberId, it.amount) })
        syncCoordinator.requestSync()
        return expenseId
    }

    override suspend fun deleteExpense(expenseId: String) {
        val current = expenseDao.getExpenseById(expenseId) ?: return
        expenseDao.upsertExpense(current.copy(deletedAt = System.currentTimeMillis(), updatedAt = System.currentTimeMillis(), syncState = SyncState.PENDING_DELETE))
        syncCoordinator.requestSync()
    }
}
