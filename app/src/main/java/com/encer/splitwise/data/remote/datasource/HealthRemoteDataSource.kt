package com.encer.splitwise.data.remote.datasource

import com.encer.splitwise.data.remote.api.HealthApi
import com.encer.splitwise.data.remote.model.HealthCheckResponse
import com.encer.splitwise.data.remote.network.requireBody
import javax.inject.Inject

class HealthRemoteDataSource @Inject constructor(
    private val healthApi: HealthApi,
) {
    suspend fun health(): HealthCheckResponse = healthApi.health().requireBody()
}
