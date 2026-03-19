package com.encer.offlinesplitwise.domain.model

data class GroupSummary(
    val totalExpenses: Int,
    val totalSettlements: Int,
    val membersCount: Int,
    val openBalancesCount: Int,
)
