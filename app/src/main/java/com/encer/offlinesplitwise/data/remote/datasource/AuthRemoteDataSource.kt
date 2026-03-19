package com.encer.offlinesplitwise.data.remote.datasource

import com.encer.offlinesplitwise.data.remote.api.AuthApi
import com.encer.offlinesplitwise.data.remote.model.*
import com.encer.offlinesplitwise.data.remote.network.requireBody
import javax.inject.Inject

class AuthRemoteDataSource @Inject constructor(
    private val authApi: AuthApi,
) {
    suspend fun register(request: AuthRegisterRequest): AuthResponse =
        authApi.register(request).requireBody()

    suspend fun login(request: AuthLoginRequest): AuthResponse =
        authApi.login(request).requireBody()

    suspend fun refresh(request: TokenRefreshRequest): TokenPair =
        authApi.refresh(request).requireBody()

    suspend fun me(): ApiUser = authApi.me().requireBody()
}
