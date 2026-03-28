package com.encer.splitwise.core.di

import android.content.Context
import com.encer.splitwise.data.preferences.HealthStatusRepository
import com.encer.splitwise.data.preferences.SessionRepository
import com.encer.splitwise.data.preferences.SettingsRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object PreferencesModule {
    @Provides
    @Singleton
    fun provideSettingsRepository(@ApplicationContext context: Context): SettingsRepository = SettingsRepository(context)

    @Provides
    @Singleton
    fun provideSessionRepository(@ApplicationContext context: Context): SessionRepository = SessionRepository(context)

    @Provides
    @Singleton
    fun provideHealthStatusRepository(@ApplicationContext context: Context): HealthStatusRepository = HealthStatusRepository(context)
}
