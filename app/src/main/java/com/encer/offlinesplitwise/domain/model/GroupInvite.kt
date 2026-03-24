package com.encer.offlinesplitwise.domain.model

data class GroupInvite(
    val id: String,
    val groupId: String,
    val memberId: String,
    val username: String,
    val inviterUserId: String,
    val inviteeUserId: String,
    val status: String,
    val groupName: String,
    val inviterUsername: String,
    val inviteeUsername: String,
    val respondedAt: Long?,
    val createdAt: Long,
    val updatedAt: Long,
)
