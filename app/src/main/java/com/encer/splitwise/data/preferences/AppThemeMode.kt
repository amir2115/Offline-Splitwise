package com.encer.splitwise.data.preferences

enum class AppThemeMode {
    LIGHT,
    DARK;

    companion object {
        fun fromName(value: String?): AppThemeMode = entries.firstOrNull { it.name == value } ?: LIGHT
    }
}
