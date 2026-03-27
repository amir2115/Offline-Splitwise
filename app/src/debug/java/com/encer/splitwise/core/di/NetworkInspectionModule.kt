package com.encer.splitwise.core.di

import android.content.Context
import com.chuckerteam.chucker.api.ChuckerCollector
import com.chuckerteam.chucker.api.ChuckerInterceptor
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import okhttp3.Interceptor

@Module
@InstallIn(SingletonComponent::class)
object NetworkInspectionModule {
    @Provides
    @Singleton
    @NetworkInspectionInterceptor
    fun provideNetworkInspectionInterceptor(
        @ApplicationContext context: Context,
    ): Interceptor {
        val collector = ChuckerCollector(
            context = context,
            showNotification = true,
        )
        return ChuckerInterceptor.Builder(context)
            .collector(collector)
            .alwaysReadResponseBody(true)
            .build()
    }
}
