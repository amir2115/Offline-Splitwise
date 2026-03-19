package com.encer.offlinesplitwise.data.remote.datasource

import com.encer.offlinesplitwise.data.remote.api.SyncApi
import com.encer.offlinesplitwise.data.remote.model.*
import com.encer.offlinesplitwise.data.remote.network.requireBody
import javax.inject.Inject

class SyncRemoteDataSource @Inject constructor(
    private val syncApi: SyncApi,
) {
    suspend fun sync(request: SyncRequestEnvelope): SyncResponse =
        syncApi.sync(request).requireBody()

    suspend fun importData(request: SyncImportRequest): SyncResponse =
        syncApi.importData(request).requireBody()
}
