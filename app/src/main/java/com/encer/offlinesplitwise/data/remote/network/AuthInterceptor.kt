package com.encer.offlinesplitwise.data.remote.network

import com.encer.offlinesplitwise.data.preferences.SessionRepository
import javax.inject.Inject
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor @Inject constructor(
    private val sessionRepository: SessionRepository,
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val skipAuth = original.header("No-Auth") != null
        val builder = original.newBuilder().removeHeader("No-Auth")
        if (!skipAuth) {
            sessionRepository.currentAccessToken()?.let { token ->
                builder.header("Authorization", "Bearer $token")
            }
        }
        return chain.proceed(builder.build())
    }
}
