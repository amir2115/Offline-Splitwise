package com.encer.splitwise.data.remote.mapper

import com.encer.splitwise.data.local.entity.ExpenseEntity
import com.encer.splitwise.data.local.entity.ExpensePayerEntity
import com.encer.splitwise.data.local.entity.ExpenseShareEntity
import com.encer.splitwise.data.local.entity.GroupEntity
import com.encer.splitwise.data.local.entity.MemberEntity
import com.encer.splitwise.data.local.entity.SettlementEntity
import com.encer.splitwise.data.local.entity.SyncState
import com.encer.splitwise.data.remote.model.*
import com.encer.splitwise.data.remote.network.* 
import com.encer.splitwise.domain.model.MembershipStatus
import com.encer.splitwise.domain.model.SplitType

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
    username = username,
    createdAt = parseIsoInstant(createdAt),
    updatedAt = parseIsoInstant(updatedAt),
    deletedAt = deletedAt?.let(::parseIsoInstant),
    isArchived = isArchived,
    userId = userId,
    membershipStatus = runCatching { MembershipStatus.valueOf(membershipStatus) }.getOrDefault(MembershipStatus.ACTIVE),
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
    username = username,
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
