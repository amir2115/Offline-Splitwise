package com.encer.offlinesplitwise.domain.model

data class Settlement(
    val id: String,
    val groupId: String,
    val fromMemberId: String,
    val toMemberId: String,
    val amount: Int,
    val note: String?,
    val createdAt: Long,
    val updatedAt: Long,
    val deletedAt: Long? = null,
    val userId: String? = null,
)
