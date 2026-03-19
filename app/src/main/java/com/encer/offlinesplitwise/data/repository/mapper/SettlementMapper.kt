package com.encer.offlinesplitwise.data.repository.mapper

import com.encer.offlinesplitwise.data.local.entity.SettlementEntity
import com.encer.offlinesplitwise.data.local.entity.SyncState
import com.encer.offlinesplitwise.domain.model.Settlement

fun SettlementEntity.toDomain() = Settlement(
    id = id,
    groupId = groupId,
    fromMemberId = fromMemberId,
    toMemberId = toMemberId,
    amount = amount,
    note = note,
    createdAt = createdAt,
    updatedAt = updatedAt,
    deletedAt = deletedAt,
    userId = userId
)

fun Settlement.toEntity(
    id: String = this.id,
    createdAt: Long = this.createdAt,
    updatedAt: Long = this.updatedAt,
    syncState: SyncState,
) = SettlementEntity(
    id = id,
    groupId = groupId,
    fromMemberId = fromMemberId,
    toMemberId = toMemberId,
    amount = amount,
    note = note?.trim().orEmpty().ifBlank { null },
    createdAt = createdAt,
    updatedAt = updatedAt,
    deletedAt = deletedAt,
    userId = userId,
    syncState = syncState
)
