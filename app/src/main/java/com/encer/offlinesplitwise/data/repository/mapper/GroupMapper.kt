package com.encer.offlinesplitwise.data.repository.mapper

import com.encer.offlinesplitwise.data.local.entity.GroupEntity
import com.encer.offlinesplitwise.data.local.entity.SyncState
import com.encer.offlinesplitwise.domain.model.Group

fun GroupEntity.toDomain() = Group(
    id = id,
    name = name,
    createdAt = createdAt,
    updatedAt = updatedAt,
    deletedAt = deletedAt,
    userId = userId
)

fun Group.toEntity(syncState: SyncState, updatedAt: Long = this.updatedAt) = GroupEntity(
    id = id,
    name = name,
    createdAt = createdAt,
    updatedAt = updatedAt,
    deletedAt = deletedAt,
    userId = userId,
    syncState = syncState
)
