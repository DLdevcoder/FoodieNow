package com.example.foodienow.domain.model

enum class ThemeMode {
    SYSTEM,
    LIGHT,
    DARK
}

data class UiPreferences(
    val themeMode: ThemeMode = ThemeMode.SYSTEM
)

