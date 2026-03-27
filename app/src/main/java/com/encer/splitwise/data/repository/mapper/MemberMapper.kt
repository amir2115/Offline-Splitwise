package com.encer.splitwise.data.repository.mapper

import com.encer.splitwise.data.local.entity.MemberEntity
import com.encer.splitwise.data.local.entity.SyncState
import com.encer.splitwise.domain.model.Member
import java.util.Locale

fun MemberEntity.toDomain() = Member(
    id = id,
    groupId = groupId,
    username = username,
    createdAt = createdAt,
    updatedAt = updatedAt,
    deletedAt = deletedAt,
    isArchived = isArchived,
    userId = userId,
    membershipStatus = membershipStatus,
)

fun Member.toEntity(syncState: SyncState, updatedAt: Long = this.updatedAt) = MemberEntity(
    id = id,
    groupId = groupId,
    username = username,
    createdAt = createdAt,
    updatedAt = updatedAt,
    deletedAt = deletedAt,
    isArchived = isArchived,
    userId = userId,
    membershipStatus = membershipStatus,
    syncState = syncState
)

fun normalizeMemberIdentity(value: String): String = value.trim().lowercase(Locale.ROOT)
