package com.encer.splitwise.data.remote.datasource

import com.encer.splitwise.data.remote.api.GroupInvitesApi
import com.encer.splitwise.data.remote.model.RemoteGroupInvite
import com.encer.splitwise.data.remote.network.requireBody
import javax.inject.Inject

class GroupInvitesRemoteDataSource @Inject constructor(
    private val groupInvitesApi: GroupInvitesApi,
) {
    suspend fun list(status: String = "pending"): List<RemoteGroupInvite> = groupInvitesApi.list(status).requireBody()
    suspend fun accept(inviteId: String): RemoteGroupInvite = groupInvitesApi.accept(inviteId).requireBody()
    suspend fun reject(inviteId: String): RemoteGroupInvite = groupInvitesApi.reject(inviteId).requireBody()
}
