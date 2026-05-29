package com.example.foodienow.data.local

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.foodienow.domain.model.ThemeMode
import com.example.foodienow.domain.model.UiPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.uiPreferencesDataStore by preferencesDataStore(name = "ui_preferences")

@Singleton
class UiPreferencesDataStore @Inject constructor(
    @param:ApplicationContext private val context: Context
) {
    val uiPreferencesFlow: Flow<UiPreferences> = context.uiPreferencesDataStore.data.map { prefs ->
        prefs.toUiPreferences()
    }

    suspend fun setThemeMode(themeMode: ThemeMode) {
        context.uiPreferencesDataStore.edit { prefs ->
            prefs[Keys.THEME_MODE] = themeMode.name
        }
    }

    private fun Preferences.toUiPreferences(): UiPreferences {
        val themeMode = runCatching {
            ThemeMode.valueOf(this[Keys.THEME_MODE].orEmpty().uppercase())
        }.getOrDefault(ThemeMode.SYSTEM)

        return UiPreferences(
            themeMode = themeMode
        )
    }

    private object Keys {
        val THEME_MODE = stringPreferencesKey("theme_mode")
    }
}

