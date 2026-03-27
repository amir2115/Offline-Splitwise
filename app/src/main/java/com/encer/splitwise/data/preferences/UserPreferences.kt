package com.encer.splitwise.data.preferences

data class UserPreferences(
    val language: AppLanguage = AppLanguage.FA,
    val themeMode: AppThemeMode = AppThemeMode.LIGHT,
)
