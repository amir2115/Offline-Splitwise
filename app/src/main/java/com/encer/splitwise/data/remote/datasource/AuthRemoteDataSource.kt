package com.encer.splitwise.data.remote.datasource

import com.encer.splitwise.data.remote.api.AuthApi
import com.encer.splitwise.data.remote.model.*
import com.encer.splitwise.data.remote.network.requireBody
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
