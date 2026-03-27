package com.encer.splitwise.data.remote.mapper

import com.encer.splitwise.data.remote.model.RemoteGroupInvite
import com.encer.splitwise.data.remote.network.parseIsoInstant
import com.encer.splitwise.domain.model.GroupInvite

fun RemoteGroupInvite.toDomain() = GroupInvite(
    id = id,
    groupId = groupId,
    memberId = memberId,
    username = username,
    inviterUserId = inviterUserId,
    inviteeUserId = inviteeUserId,
    status = status,
    groupName = groupName,
    inviterUsername = inviterUsername,
    inviteeUsername = inviteeUsername,
    respondedAt = respondedAt?.let(::parseIsoInstant),
    createdAt = parseIsoInstant(createdAt),
    updatedAt = parseIsoInstant(updatedAt),
)
