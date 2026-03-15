package com.encer.offlinesplitwise.domain

import kotlinx.coroutines.flow.Flow

enum class SplitType {
    EQUAL,
    EXACT
}

data class Group(
    val id: Long = 0,
    val name: String,
    val createdAt: Long,
)

data class Member(
    val id: Long = 0,
    val groupId: Long,
    val name: String,
    val createdAt: Long,
    val isArchived: Boolean = false,
)

data class ExpenseShare(
    val memberId: Long,
    val amount: Int,
)

data class Expense(
    val id: Long = 0,
    val groupId: Long,
    val title: String,
    val note: String,
    val totalAmount: Int,
    val splitType: SplitType,
    val createdAt: Long,
    val updatedAt: Long,
    val payers: List<ExpenseShare>,
    val shares: List<ExpenseShare>,
)

data class Settlement(
    val id: Long = 0,
    val groupId: Long,
    val fromMemberId: Long,
    val toMemberId: Long,
    val amount: Int,
    val note: String,
    val createdAt: Long,
)

data class ExpenseDraft(
    val groupId: Long,
    val title: String,
    val note: String,
    val totalAmount: Int,
    val splitType: SplitType,
    val payers: List<ExpenseShare>,
    val shares: List<ExpenseShare>,
)

data class MemberBalance(
    val memberId: Long,
    val memberName: String,
    val paidTotal: Int,
    val owedTotal: Int,
    val netBalance: Int,
)

data class SimplifiedTransfer(
    val fromMemberId: Long,
    val fromMemberName: String,
    val toMemberId: Long,
    val toMemberName: String,
    val amount: Int,
)

data class GroupSummary(
    val totalExpenses: Int,
    val totalSettlements: Int,
    val membersCount: Int,
    val openBalancesCount: Int,
)

data class ExpenseDraftValidation(
    val isValid: Boolean,
    val message: String? = null,
    val normalizedShares: List<ExpenseShare> = emptyList(),
)

data class ExpenseDetailsViewData(
    val expense: Expense,
    val payerLabels: List<String>,
    val shareLabels: List<String>,
)

interface GroupRepository {
    fun observeGroups(): Flow<List<Group>>
    suspend fun createGroup(name: String): Long
    suspend fun updateGroup(group: Group)
    suspend fun deleteGroup(groupId: Long)
    suspend fun getGroup(groupId: Long): Group?
}

interface MemberRepository {
    fun observeMembers(groupId: Long): Flow<List<Member>>
    suspend fun addMember(groupId: Long, name: String): Long
    suspend fun updateMember(member: Member)
    suspend fun deleteMember(memberId: Long)
    suspend fun getMember(memberId: Long): Member?
}

interface ExpenseRepository {
    fun observeExpenses(groupId: Long): Flow<List<Expense>>
    suspend fun getExpense(expenseId: Long): Expense?
    suspend fun upsertExpense(draft: ExpenseDraft, existingId: Long? = null): Long
    suspend fun deleteExpense(expenseId: Long)
}

interface SettlementRepository {
    fun observeSettlements(groupId: Long): Flow<List<Settlement>>
    suspend fun getSettlement(settlementId: Long): Settlement?
    suspend fun upsertSettlement(settlement: Settlement): Long
    suspend fun deleteSettlement(settlementId: Long)
}
