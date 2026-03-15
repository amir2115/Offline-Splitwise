package com.encer.offlinesplitwise.domain

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class ValidateExpenseInputUseCase {
    operator fun invoke(
        totalAmount: Int,
        splitType: SplitType,
        payers: List<ExpenseShare>,
        shares: List<ExpenseShare>
    ): ExpenseDraftValidation {
        if (totalAmount <= 0) {
            return ExpenseDraftValidation(false, "مبلغ کل باید بیشتر از صفر باشد.")
        }
        if (payers.isEmpty()) {
            return ExpenseDraftValidation(false, "حداقل یک پرداخت‌کننده لازم است.")
        }
        if (shares.isEmpty()) {
            return ExpenseDraftValidation(false, "حداقل یک نفر باید در تقسیم حضور داشته باشد.")
        }
        if (payers.any { it.amount < 0 } || shares.any { it.amount < 0 }) {
            return ExpenseDraftValidation(false, "مقادیر منفی مجاز نیستند.")
        }
        val paidTotal = payers.sumOf { it.amount }
        if (paidTotal != totalAmount) {
            return ExpenseDraftValidation(false, "جمع پرداخت‌کننده‌ها باید با مبلغ کل برابر باشد.")
        }

        return when (splitType) {
            SplitType.EXACT -> {
                if (shares.sumOf { it.amount } != totalAmount) {
                    ExpenseDraftValidation(false, "جمع سهم‌ها باید با مبلغ کل برابر باشد.")
                } else {
                    ExpenseDraftValidation(true, normalizedShares = shares)
                }
            }

            SplitType.EQUAL -> {
                val memberIds = shares.map { it.memberId }
                val normalized = splitEqually(totalAmount, memberIds)
                ExpenseDraftValidation(true, normalizedShares = normalized)
            }
        }
    }

    fun splitEqually(totalAmount: Int, memberIds: List<Long>): List<ExpenseShare> {
        if (memberIds.isEmpty()) return emptyList()
        val base = totalAmount / memberIds.size
        var remainder = totalAmount % memberIds.size
        return memberIds.sorted().map { memberId ->
            val extra = if (remainder > 0) {
                remainder -= 1
                1
            } else {
                0
            }
            ExpenseShare(memberId = memberId, amount = base + extra)
        }
    }
}

class BalanceCalculator {
    fun calculateBalances(
        members: List<Member>,
        expenses: List<Expense>,
        settlements: List<Settlement>
    ): List<MemberBalance> {
        val paidMap = mutableMapOf<Long, Int>().withDefault { 0 }
        val owedMap = mutableMapOf<Long, Int>().withDefault { 0 }

        expenses.forEach { expense ->
            expense.payers.forEach { payer ->
                paidMap[payer.memberId] = paidMap.getValue(payer.memberId) + payer.amount
            }
            expense.shares.forEach { share ->
                owedMap[share.memberId] = owedMap.getValue(share.memberId) + share.amount
            }
        }

        settlements.forEach { settlement ->
            paidMap[settlement.toMemberId] = paidMap.getValue(settlement.toMemberId) + settlement.amount
            owedMap[settlement.fromMemberId] = owedMap.getValue(settlement.fromMemberId) + settlement.amount
        }

        return members.map { member ->
            val paid = paidMap.getValue(member.id)
            val owed = owedMap.getValue(member.id)
            MemberBalance(
                memberId = member.id,
                memberName = member.name,
                paidTotal = paid,
                owedTotal = owed,
                netBalance = paid - owed
            )
        }
    }

    fun simplify(balances: List<MemberBalance>): List<SimplifiedTransfer> {
        data class Node(val memberId: Long, val memberName: String, var amount: Int)

        val debtors = balances.filter { it.netBalance < 0 }
            .map { Node(it.memberId, it.memberName, -it.netBalance) }
            .sortedByDescending { it.amount }
            .toMutableList()
        val creditors = balances.filter { it.netBalance > 0 }
            .map { Node(it.memberId, it.memberName, it.netBalance) }
            .sortedByDescending { it.amount }
            .toMutableList()

        val result = mutableListOf<SimplifiedTransfer>()
        var debtorIndex = 0
        var creditorIndex = 0
        while (debtorIndex < debtors.size && creditorIndex < creditors.size) {
            val debtor = debtors[debtorIndex]
            val creditor = creditors[creditorIndex]
            val transferAmount = minOf(debtor.amount, creditor.amount)
            if (transferAmount > 0) {
                result += SimplifiedTransfer(
                    fromMemberId = debtor.memberId,
                    fromMemberName = debtor.memberName,
                    toMemberId = creditor.memberId,
                    toMemberName = creditor.memberName,
                    amount = transferAmount
                )
            }
            debtor.amount -= transferAmount
            creditor.amount -= transferAmount
            if (debtor.amount == 0) debtorIndex += 1
            if (creditor.amount == 0) creditorIndex += 1
        }
        return result
    }
}

class ObserveGroupBalancesUseCase(
    private val memberRepository: MemberRepository,
    private val expenseRepository: ExpenseRepository,
    private val settlementRepository: SettlementRepository,
    private val balanceCalculator: BalanceCalculator
) {
    operator fun invoke(groupId: Long): Flow<List<MemberBalance>> {
        return combine(
            memberRepository.observeMembers(groupId),
            expenseRepository.observeExpenses(groupId),
            settlementRepository.observeSettlements(groupId)
        ) { members, expenses, settlements ->
            balanceCalculator.calculateBalances(members, expenses, settlements)
        }
    }
}

class ObserveGroupSummaryUseCase(
    private val memberRepository: MemberRepository,
    private val expenseRepository: ExpenseRepository,
    private val settlementRepository: SettlementRepository,
    private val observeGroupBalancesUseCase: ObserveGroupBalancesUseCase
) {
    operator fun invoke(groupId: Long): Flow<GroupSummary> {
        return combine(
            memberRepository.observeMembers(groupId),
            expenseRepository.observeExpenses(groupId),
            settlementRepository.observeSettlements(groupId),
            observeGroupBalancesUseCase(groupId)
        ) { members, expenses, settlements, balances ->
            GroupSummary(
                totalExpenses = expenses.sumOf { it.totalAmount },
                totalSettlements = settlements.sumOf { it.amount },
                membersCount = members.size,
                openBalancesCount = balances.count { it.netBalance != 0 }
            )
        }
    }
}

class SimplifyDebtsUseCase(
    private val balanceCalculator: BalanceCalculator
) {
    operator fun invoke(balances: List<MemberBalance>): List<SimplifiedTransfer> {
        return balanceCalculator.simplify(balances)
    }
}
