package com.encer.splitwise.data.repository.mapper

import com.encer.splitwise.data.local.entity.GroupEntity
import com.encer.splitwise.data.local.entity.SyncState
import com.encer.splitwise.domain.model.Group

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
