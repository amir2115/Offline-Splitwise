package com.encer.splitwise.data.remote.api

import com.encer.splitwise.data.remote.model.HealthCheckResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Headers

interface HealthApi {
    @Headers("No-Auth: true")
    @GET("health")
    suspend fun health(): Response<HealthCheckResponse>
}
