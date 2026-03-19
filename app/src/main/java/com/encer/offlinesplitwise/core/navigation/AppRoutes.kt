package com.encer.offlinesplitwise.core.navigation

import android.net.Uri

object AppRoutes {
    const val GROUPS = "groups"
    const val SETTINGS = "settings"
    const val GROUP_PATTERN = "group/{groupId}"
    const val MEMBERS_PATTERN = "members/{groupId}"
    const val EXPENSE_PATTERN = "expense/{groupId}?expenseId={expenseId}"
    const val SETTLEMENT_PATTERN = "settlement/{groupId}?settlementId={settlementId}&fromMemberId={fromMemberId}&toMemberId={toMemberId}&amount={amount}"
    const val BALANCES_PATTERN = "balances/{groupId}"
    const val EXPENSE_DETAILS_PATTERN = "expenseDetail/{groupId}/{expenseId}"

    fun group(groupId: String) = "group/$groupId"
    fun members(groupId: String) = "members/$groupId"
    fun expense(groupId: String, expenseId: String? = null) =
        if (expenseId.isNullOrBlank()) "expense/$groupId" else "expense/$groupId?expenseId=${Uri.encode(expenseId)}"

    fun settlement(
        groupId: String,
        settlementId: String? = null,
        fromMemberId: String? = null,
        toMemberId: String? = null,
        amount: String? = null,
    ): String {
        val params = buildList {
            settlementId?.takeIf { it.isNotBlank() }?.let { add("settlementId=${Uri.encode(it)}") }
            fromMemberId?.takeIf { it.isNotBlank() }?.let { add("fromMemberId=${Uri.encode(it)}") }
            toMemberId?.takeIf { it.isNotBlank() }?.let { add("toMemberId=${Uri.encode(it)}") }
            amount?.takeIf { it.isNotBlank() }?.let { add("amount=${Uri.encode(it)}") }
        }
        return if (params.isEmpty()) {
            "settlement/$groupId"
        } else {
            "settlement/$groupId?${params.joinToString("&")}"
        }
    }

    fun balances(groupId: String) = "balances/$groupId"
    fun expenseDetails(groupId: String, expenseId: String) = "expenseDetail/$groupId/$expenseId"
}
