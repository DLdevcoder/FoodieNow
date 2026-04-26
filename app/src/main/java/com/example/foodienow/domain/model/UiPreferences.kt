package com.example.foodienow.domain.model

enum class AppLanguage(val languageTag: String) {
    ENGLISH("en"),
    VIETNAMESE("vi");

    companion object {
        fun fromTag(tag: String?): AppLanguage {
            return entries.firstOrNull { it.languageTag.equals(tag, ignoreCase = true) } ?: ENGLISH
        }
    }
}

enum class ThemeMode {
    SYSTEM,
    LIGHT,
    DARK
}

data class UiPreferences(
    val appLanguage: AppLanguage = AppLanguage.ENGLISH,
    val themeMode: ThemeMode = ThemeMode.SYSTEM
)

