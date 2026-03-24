package com.encer.offlinesplitwise.data.remote.mapper

import com.encer.offlinesplitwise.data.remote.model.RemoteGroupInvite
import com.encer.offlinesplitwise.data.remote.network.parseIsoInstant
import com.encer.offlinesplitwise.domain.model.GroupInvite

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
