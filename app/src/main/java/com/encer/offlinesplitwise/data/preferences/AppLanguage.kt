package com.encer.offlinesplitwise.data.preferences

enum class AppLanguage(val tag: String) {
    FA("fa"),
    EN("en");

    companion object {
        fun fromTag(value: String?): AppLanguage = entries.firstOrNull { it.tag == value } ?: FA
    }
}
