package com.encer.splitwise.data.remote.api

import com.encer.splitwise.data.remote.model.ApiUser
import com.encer.splitwise.data.remote.model.AuthLoginRequest
import com.encer.splitwise.data.remote.model.AuthRegisterRequest
import com.encer.splitwise.data.remote.model.AuthResponse
import com.encer.splitwise.data.remote.model.TokenPair
import com.encer.splitwise.data.remote.model.TokenRefreshRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST

interface AuthApi {
    @Headers("No-Auth: true")
    @POST("auth/register")
    suspend fun register(@Body request: AuthRegisterRequest): Response<AuthResponse>

    @Headers("No-Auth: true")
    @POST("auth/login")
    suspend fun login(@Body request: AuthLoginRequest): Response<AuthResponse>

    @Headers("No-Auth: true")
    @POST("auth/refresh")
    suspend fun refresh(@Body request: TokenRefreshRequest): Response<TokenPair>

    @GET("auth/me")
    suspend fun me(): Response<ApiUser>
}
