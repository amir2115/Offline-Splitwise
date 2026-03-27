package com.encer.splitwise.data.remote.datasource

import com.encer.splitwise.data.remote.api.SyncApi
import com.encer.splitwise.data.remote.model.*
import com.encer.splitwise.data.remote.network.requireBody
import javax.inject.Inject

class SyncRemoteDataSource @Inject constructor(
    private val syncApi: SyncApi,
) {
    suspend fun sync(request: SyncRequestEnvelope): SyncResponse =
        syncApi.sync(request).requireBody()

    suspend fun importData(request: SyncImportRequest): SyncResponse =
        syncApi.importData(request).requireBody()
}
