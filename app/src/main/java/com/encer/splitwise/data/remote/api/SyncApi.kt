package com.encer.splitwise.data.remote.api

import com.encer.splitwise.data.remote.model.SyncImportRequest
import com.encer.splitwise.data.remote.model.SyncRequestEnvelope
import com.encer.splitwise.data.remote.model.SyncResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface SyncApi {
    @POST("sync")
    suspend fun sync(@Body request: SyncRequestEnvelope): Response<SyncResponse>

    @POST("sync/import")
    suspend fun importData(@Body request: SyncImportRequest): Response<SyncResponse>
}
