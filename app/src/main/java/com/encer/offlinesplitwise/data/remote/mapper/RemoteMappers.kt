package com.encer.offlinesplitwise.data.remote.mapper

import com.encer.offlinesplitwise.data.local.entity.ExpenseEntity
import com.encer.offlinesplitwise.data.local.entity.ExpensePayerEntity
import com.encer.offlinesplitwise.data.local.entity.ExpenseShareEntity
import com.encer.offlinesplitwise.data.local.entity.GroupEntity
import com.encer.offlinesplitwise.data.local.entity.MemberEntity
import com.encer.offlinesplitwise.data.local.entity.SettlementEntity
import com.encer.offlinesplitwise.data.local.entity.SyncState
import com.encer.offlinesplitwise.data.remote.model.*
import com.encer.offlinesplitwise.data.remote.network.* 
import com.encer.offlinesplitwise.domain.model.SplitType

fun RemoteGroup.toEntity() = GroupEntity(
    id = id,
    name = name,
    createdAt = parseIsoInstant(createdAt),
    updatedAt = parseIsoInstant(updatedAt),
    deletedAt = deletedAt?.let(::parseIsoInstant),
    userId = userId,
    syncState = SyncState.SYNCED,
)

fun RemoteMember.toEntity() = MemberEntity(
    id = id,
    groupId = groupId,
    name = name,
    createdAt = parseIsoInstant(createdAt),
    updatedAt = parseIsoInstant(updatedAt),
    deletedAt = deletedAt?.let(::parseIsoInstant),
    isArchived = isArchived,
    userId = userId,
    syncState = SyncState.SYNCED,
)

fun RemoteExpense.toEntity() = ExpenseEntity(
    id = id,
    groupId = groupId,
    title = title,
    note = note,
    totalAmount = totalAmount,
    splitType = runCatching { SplitType.valueOf(splitType) }.getOrDefault(SplitType.EQUAL),
    createdAt = parseIsoInstant(createdAt),
    updatedAt = parseIsoInstant(updatedAt),
    deletedAt = deletedAt?.let(::parseIsoInstant),
    userId = userId,
    syncState = SyncState.SYNCED,
)

fun RemoteExpense.toPayerEntities() = payers.map {
    ExpensePayerEntity(expenseId = id, memberId = it.memberId, amountPaid = it.amount)
}

fun RemoteExpense.toShareEntities() = shares.map {
    ExpenseShareEntity(expenseId = id, memberId = it.memberId, amountOwed = it.amount)
}

fun RemoteSettlement.toEntity() = SettlementEntity(
    id = id,
    groupId = groupId,
    fromMemberId = fromMemberId,
    toMemberId = toMemberId,
    amount = amount,
    note = note,
    createdAt = parseIsoInstant(createdAt),
    updatedAt = parseIsoInstant(updatedAt),
    deletedAt = deletedAt?.let(::parseIsoInstant),
    userId = userId,
    syncState = SyncState.SYNCED,
)

fun GroupEntity.toRemotePayload() = RemoteGroupPayload(
    id = id,
    name = name,
    createdAt = formatIsoInstant(createdAt),
    updatedAt = formatIsoInstant(updatedAt),
    deletedAt = deletedAt?.let(::formatIsoInstant),
)

fun MemberEntity.toRemotePayload() = RemoteMemberPayload(
    id = id,
    groupId = groupId,
    name = name,
    isArchived = isArchived,
    createdAt = formatIsoInstant(createdAt),
    updatedAt = formatIsoInstant(updatedAt),
    deletedAt = deletedAt?.let(::formatIsoInstant),
)

fun ExpenseEntity.toRemotePayload(
    payers: List<ExpensePayerEntity>,
    shares: List<ExpenseShareEntity>,
) = RemoteExpensePayload(
    id = id,
    groupId = groupId,
    title = title,
    note = note,
    totalAmount = totalAmount,
    splitType = splitType.name,
    payers = payers.map { RemoteExpenseParticipantAmount(memberId = it.memberId, amount = it.amountPaid) },
    shares = shares.map { RemoteExpenseParticipantAmount(memberId = it.memberId, amount = it.amountOwed) },
    createdAt = formatIsoInstant(createdAt),
    updatedAt = formatIsoInstant(updatedAt),
    deletedAt = deletedAt?.let(::formatIsoInstant),
)

fun SettlementEntity.toRemotePayload() = RemoteSettlementPayload(
    id = id,
    groupId = groupId,
    fromMemberId = fromMemberId,
    toMemberId = toMemberId,
    amount = amount,
    note = note,
    createdAt = formatIsoInstant(createdAt),
    updatedAt = formatIsoInstant(updatedAt),
    deletedAt = deletedAt?.let(::formatIsoInstant),
)
