package com.example.foodienow.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.foodienow.data.local.UiPreferencesDataStore
import com.example.foodienow.domain.model.ThemeMode
import com.example.foodienow.domain.model.UiPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UiPreferencesViewModel @Inject constructor(
    private val uiPreferencesDataStore: UiPreferencesDataStore
) : ViewModel() {

    val uiPreferences: StateFlow<UiPreferences> = uiPreferencesDataStore.uiPreferencesFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = UiPreferences()
    )

    fun setThemeMode(themeMode: ThemeMode) {
        viewModelScope.launch {
            uiPreferencesDataStore.setThemeMode(themeMode)
        }
    }
}

