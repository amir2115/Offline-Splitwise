package com.encer.splitwise.data.remote.api

import com.encer.splitwise.data.remote.model.RemoteAddMemberResponse
import com.encer.splitwise.data.remote.model.RemoteMemberCreateRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface MembersApi {
    @POST("members")
    suspend fun create(@Body request: RemoteMemberCreateRequest): Response<RemoteAddMemberResponse>
}
