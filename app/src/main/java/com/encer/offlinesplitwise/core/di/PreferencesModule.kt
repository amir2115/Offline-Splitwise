package com.encer.offlinesplitwise.core.di

import android.content.Context
import com.encer.offlinesplitwise.data.preferences.SessionRepository
import com.encer.offlinesplitwise.data.preferences.SettingsRepository
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
}
