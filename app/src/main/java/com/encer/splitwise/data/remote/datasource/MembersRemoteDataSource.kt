package com.encer.splitwise.data.remote.datasource

import com.encer.splitwise.data.remote.api.MembersApi
import com.encer.splitwise.data.remote.model.RemoteAddMemberResponse
import com.encer.splitwise.data.remote.model.RemoteMemberCreateRequest
import com.encer.splitwise.data.remote.network.requireBody
import javax.inject.Inject

class MembersRemoteDataSource @Inject constructor(
    private val membersApi: MembersApi,
) {
    suspend fun create(request: RemoteMemberCreateRequest): RemoteAddMemberResponse =
        membersApi.create(request).requireBody()
}
