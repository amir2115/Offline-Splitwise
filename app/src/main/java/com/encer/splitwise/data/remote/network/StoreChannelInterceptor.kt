package com.encer.splitwise.data.remote.network

import com.encer.splitwise.BuildConfig
import javax.inject.Inject
import okhttp3.Interceptor
import okhttp3.Response

class StoreChannelInterceptor @Inject constructor() : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
            .newBuilder()
            .header("X-App-Store", BuildConfig.STORE_CHANNEL)
            .build()
        return chain.proceed(request)
    }
}
