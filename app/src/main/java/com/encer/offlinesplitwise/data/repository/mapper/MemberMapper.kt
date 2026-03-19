package com.encer.offlinesplitwise.data.repository.mapper

import com.encer.offlinesplitwise.data.local.entity.MemberEntity
import com.encer.offlinesplitwise.data.local.entity.SyncState
import com.encer.offlinesplitwise.domain.model.Member
import java.util.Locale

fun MemberEntity.toDomain() = Member(
    id = id,
    groupId = groupId,
    name = name,
    createdAt = createdAt,
    updatedAt = updatedAt,
    deletedAt = deletedAt,
    isArchived = isArchived,
    userId = userId
)

fun Member.toEntity(syncState: SyncState, updatedAt: Long = this.updatedAt) = MemberEntity(
    id = id,
    groupId = groupId,
    name = name,
    createdAt = createdAt,
    updatedAt = updatedAt,
    deletedAt = deletedAt,
    isArchived = isArchived,
    userId = userId,
    syncState = syncState
)

fun normalizeMemberIdentity(value: String): String = value.trim().lowercase(Locale.ROOT)
