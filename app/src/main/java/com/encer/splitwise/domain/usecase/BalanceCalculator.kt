package com.encer.splitwise.domain.usecase

import com.encer.splitwise.domain.model.Expense
import com.encer.splitwise.domain.model.Member
import com.encer.splitwise.domain.model.MemberBalance
import com.encer.splitwise.domain.model.Settlement
import com.encer.splitwise.domain.model.SimplifiedTransfer
import javax.inject.Inject

class BalanceCalculator @Inject constructor() {
    fun calculateBalances(
        members: List<Member>,
        expenses: List<Expense>,
        settlements: List<Settlement>
    ): List<MemberBalance> {
        val paidMap = mutableMapOf<String, Int>().withDefault { 0 }
        val owedMap = mutableMapOf<String, Int>().withDefault { 0 }
        expenses.forEach { expense ->
            expense.payers.forEach { payer -> paidMap[payer.memberId] = paidMap.getValue(payer.memberId) + payer.amount }
            expense.shares.forEach { share -> owedMap[share.memberId] = owedMap.getValue(share.memberId) + share.amount }
        }
        settlements.forEach { settlement ->
            paidMap[settlement.fromMemberId] = paidMap.getValue(settlement.fromMemberId) + settlement.amount
            owedMap[settlement.toMemberId] = owedMap.getValue(settlement.toMemberId) + settlement.amount
        }
        return members.map { member ->
            val paid = paidMap.getValue(member.id)
            val owed = owedMap.getValue(member.id)
            MemberBalance(member.id, member.username, paid, owed, paid - owed)
        }
    }

    fun simplify(balances: List<MemberBalance>): List<SimplifiedTransfer> {
        data class Node(val memberId: String, val memberName: String, var amount: Int)

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
                result += SimplifiedTransfer(debtor.memberId, debtor.memberName, creditor.memberId, creditor.memberName, transferAmount)
            }
            debtor.amount -= transferAmount
            creditor.amount -= transferAmount
            if (debtor.amount == 0) debtorIndex += 1
            if (creditor.amount == 0) creditorIndex += 1
        }
        return result
    }
}
