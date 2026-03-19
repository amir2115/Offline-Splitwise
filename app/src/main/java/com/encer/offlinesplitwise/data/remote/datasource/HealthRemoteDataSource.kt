package com.encer.offlinesplitwise.data.remote.datasource

import com.encer.offlinesplitwise.data.remote.api.HealthApi
import com.encer.offlinesplitwise.data.remote.model.HealthCheckResponse
import com.encer.offlinesplitwise.data.remote.network.requireBody
import javax.inject.Inject

class HealthRemoteDataSource @Inject constructor(
    private val healthApi: HealthApi,
) {
    suspend fun health(): HealthCheckResponse = healthApi.health().requireBody()
}
