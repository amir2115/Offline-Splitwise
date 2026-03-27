package com.encer.splitwise.core.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import okhttp3.Interceptor

@Module
@InstallIn(SingletonComponent::class)
object NetworkInspectionModule {
    @Provides
    @Singleton
    @NetworkInspectionInterceptor
    fun provideNetworkInspectionInterceptor(): Interceptor = Interceptor { chain ->
        chain.proceed(chain.request())
    }
}
