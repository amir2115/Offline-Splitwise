package com.encer.offlinesplitwise.data.remote.api

import com.encer.offlinesplitwise.data.remote.model.RemoteGroupInvite
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface GroupInvitesApi {
    @GET("group-invites")
    suspend fun list(@Query("status") status: String = "pending"): Response<List<RemoteGroupInvite>>

    @POST("group-invites/{inviteId}/accept")
    suspend fun accept(@Path("inviteId") inviteId: String): Response<RemoteGroupInvite>

    @POST("group-invites/{inviteId}/reject")
    suspend fun reject(@Path("inviteId") inviteId: String): Response<RemoteGroupInvite>
}
