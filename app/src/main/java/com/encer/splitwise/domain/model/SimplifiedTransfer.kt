package com.encer.splitwise.domain.model

data class SimplifiedTransfer(
    val fromMemberId: String,
    val fromMemberName: String,
    val toMemberId: String,
    val toMemberName: String,
    val amount: Int,
)
