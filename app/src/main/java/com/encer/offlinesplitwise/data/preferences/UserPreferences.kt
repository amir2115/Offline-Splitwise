package com.encer.offlinesplitwise.data.preferences

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

enum class AppLanguage(val tag: String) {
    FA("fa"),
    EN("en");

    companion object {
        fun fromTag(value: String?): AppLanguage = entries.firstOrNull { it.tag == value } ?: FA
    }
}

enum class AppThemeMode {
    LIGHT,
    DARK;

    companion object {
        fun fromName(value: String?): AppThemeMode = entries.firstOrNull { it.name == value } ?: LIGHT
    }
}

data class UserPreferences(
    val language: AppLanguage = AppLanguage.FA,
    val themeMode: AppThemeMode = AppThemeMode.LIGHT,
)

class SettingsRepository(context: Context) {
    private val preferences = context.getSharedPreferences("ui_settings", Context.MODE_PRIVATE)
    private val _settings = MutableStateFlow(loadSettings())

    fun observeSettings(): StateFlow<UserPreferences> = _settings

    fun updateLanguage(language: AppLanguage) {
        preferences.edit().putString(KEY_LANGUAGE, language.tag).apply()
        _settings.value = loadSettings()
    }

    fun updateThemeMode(themeMode: AppThemeMode) {
        preferences.edit().putString(KEY_THEME_MODE, themeMode.name).apply()
        _settings.value = loadSettings()
    }

    private fun loadSettings(): UserPreferences {
        return UserPreferences(
            language = AppLanguage.fromTag(preferences.getString(KEY_LANGUAGE, AppLanguage.FA.tag)),
            themeMode = AppThemeMode.fromName(preferences.getString(KEY_THEME_MODE, AppThemeMode.LIGHT.name))
        )
    }

    private companion object {
        const val KEY_LANGUAGE = "language"
        const val KEY_THEME_MODE = "theme_mode"
    }
}
