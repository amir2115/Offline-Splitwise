package com.encer.offlinesplitwise.data.repository

import com.encer.offlinesplitwise.data.local.ExpenseDao
import com.encer.offlinesplitwise.data.local.ExpenseEntity
import com.encer.offlinesplitwise.data.local.ExpensePayerEntity
import com.encer.offlinesplitwise.data.local.ExpenseShareEntity
import com.encer.offlinesplitwise.data.local.GroupDao
import com.encer.offlinesplitwise.data.local.GroupEntity
import com.encer.offlinesplitwise.data.local.MemberDao
import com.encer.offlinesplitwise.data.local.MemberEntity
import com.encer.offlinesplitwise.data.local.SettlementDao
import com.encer.offlinesplitwise.data.local.SettlementEntity
import com.encer.offlinesplitwise.data.local.TransactionDao
import com.encer.offlinesplitwise.domain.Expense
import com.encer.offlinesplitwise.domain.ExpenseDraft
import com.encer.offlinesplitwise.domain.ExpenseRepository
import com.encer.offlinesplitwise.domain.ExpenseShare
import com.encer.offlinesplitwise.domain.Group
import com.encer.offlinesplitwise.domain.GroupRepository
import com.encer.offlinesplitwise.domain.Member
import com.encer.offlinesplitwise.domain.MemberRepository
import com.encer.offlinesplitwise.domain.Settlement
import com.encer.offlinesplitwise.domain.SettlementRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

class DefaultGroupRepository(
    private val groupDao: GroupDao
) : GroupRepository {
    override fun observeGroups(): Flow<List<Group>> = groupDao.observeGroups().map { list ->
        list.map { it.toDomain() }
    }

    override suspend fun createGroup(name: String): Long {
        return groupDao.insert(GroupEntity(name = name.trim(), createdAt = System.currentTimeMillis()))
    }

    override suspend fun updateGroup(group: Group) {
        groupDao.update(group.toEntity())
    }

    override suspend fun deleteGroup(groupId: Long) {
        groupDao.deleteById(groupId)
    }

    override suspend fun getGroup(groupId: Long): Group? = groupDao.getById(groupId)?.toDomain()
}

class DefaultMemberRepository(
    private val memberDao: MemberDao
) : MemberRepository {
    override fun observeMembers(groupId: Long): Flow<List<Member>> = memberDao.observeMembers(groupId).map { list ->
        list.map { it.toDomain() }
    }

    override suspend fun addMember(groupId: Long, name: String): Long {
        return memberDao.insert(
            MemberEntity(
                groupId = groupId,
                name = name.trim(),
                createdAt = System.currentTimeMillis()
            )
        )
    }

    override suspend fun updateMember(member: Member) {
        memberDao.update(member.toEntity())
    }

    override suspend fun deleteMember(memberId: Long) {
        memberDao.deleteById(memberId)
    }

    override suspend fun getMember(memberId: Long): Member? = memberDao.getById(memberId)?.toDomain()
}

class DefaultExpenseRepository(
    private val expenseDao: ExpenseDao,
    private val transactionDao: TransactionDao
) : ExpenseRepository {
    override fun observeExpenses(groupId: Long): Flow<List<Expense>> {
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

    override suspend fun getExpense(expenseId: Long): Expense? {
        val expense = expenseDao.getExpenseById(expenseId) ?: return null
        return expense.toDomain(
            payers = transactionDao.getPayers(expenseId).map { it.toDomainPayer() },
            shares = transactionDao.getShares(expenseId).map { it.toDomainShare() }
        )
    }

    override suspend fun upsertExpense(draft: ExpenseDraft, existingId: Long?): Long {
        val now = System.currentTimeMillis()
        val expenseId = if (existingId == null) {
            expenseDao.insertExpense(
                ExpenseEntity(
                    groupId = draft.groupId,
                    title = draft.title.trim(),
                    note = draft.note.trim(),
                    totalAmount = draft.totalAmount,
                    splitType = draft.splitType,
                    createdAt = now,
                    updatedAt = now
                )
            )
        } else {
            val current = expenseDao.getExpenseById(existingId)
                ?: error("Expense $existingId not found")
            expenseDao.updateExpense(
                current.copy(
                    title = draft.title.trim(),
                    note = draft.note.trim(),
                    totalAmount = draft.totalAmount,
                    splitType = draft.splitType,
                    updatedAt = now
                )
            )
            existingId
        }

        transactionDao.deletePayersForExpense(expenseId)
        transactionDao.deleteSharesForExpense(expenseId)
        transactionDao.insertPayers(draft.payers.map { ExpensePayerEntity(expenseId, it.memberId, it.amount) })
        transactionDao.insertShares(draft.shares.map { ExpenseShareEntity(expenseId, it.memberId, it.amount) })
        return expenseId
    }

    override suspend fun deleteExpense(expenseId: Long) {
        expenseDao.deleteExpense(expenseId)
    }
}

class DefaultSettlementRepository(
    private val settlementDao: SettlementDao
) : SettlementRepository {
    override fun observeSettlements(groupId: Long): Flow<List<Settlement>> {
        return settlementDao.observeSettlements(groupId).map { list -> list.map { it.toDomain() } }
    }

    override suspend fun getSettlement(settlementId: Long): Settlement? = settlementDao.getById(settlementId)?.toDomain()

    override suspend fun upsertSettlement(settlement: Settlement): Long {
        return if (settlement.id == 0L) {
            settlementDao.insert(settlement.toEntity())
        } else {
            settlementDao.update(settlement.toEntity())
            settlement.id
        }
    }

    override suspend fun deleteSettlement(settlementId: Long) {
        settlementDao.deleteById(settlementId)
    }
}

private fun GroupEntity.toDomain() = Group(id = id, name = name, createdAt = createdAt)
private fun Group.toEntity() = GroupEntity(id = id, name = name, createdAt = createdAt)

private fun MemberEntity.toDomain() = Member(
    id = id,
    groupId = groupId,
    name = name,
    createdAt = createdAt,
    isArchived = isArchived
)
private fun Member.toEntity() = MemberEntity(
    id = id,
    groupId = groupId,
    name = name,
    createdAt = createdAt,
    isArchived = isArchived
)

private fun ExpenseEntity.toDomain(
    payers: List<ExpenseShare>,
    shares: List<ExpenseShare>
) = Expense(
    id = id,
    groupId = groupId,
    title = title,
    note = note,
    totalAmount = totalAmount,
    splitType = splitType,
    createdAt = createdAt,
    updatedAt = updatedAt,
    payers = payers,
    shares = shares
)

private fun ExpensePayerEntity.toDomainPayer() = ExpenseShare(memberId = memberId, amount = amountPaid)
private fun ExpenseShareEntity.toDomainShare() = ExpenseShare(memberId = memberId, amount = amountOwed)

private fun SettlementEntity.toDomain() = Settlement(
    id = id,
    groupId = groupId,
    fromMemberId = fromMemberId,
    toMemberId = toMemberId,
    amount = amount,
    note = note,
    createdAt = createdAt
)
private fun Settlement.toEntity() = SettlementEntity(
    id = id,
    groupId = groupId,
    fromMemberId = fromMemberId,
    toMemberId = toMemberId,
    amount = amount,
    note = note,
    createdAt = createdAt
)
