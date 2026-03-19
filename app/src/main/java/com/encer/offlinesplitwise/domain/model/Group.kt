package com.encer.offlinesplitwise.domain.model

data class Group(
    val id: String,
    val name: String,
    val createdAt: Long,
    val updatedAt: Long,
    val deletedAt: Long? = null,
    val userId: String? = null,
)
