package com.encer.offlinesplitwise.domain.model

data class MemberBalance(
    val memberId: String,
    val memberName: String,
    val paidTotal: Int,
    val owedTotal: Int,
    val netBalance: Int,
)
