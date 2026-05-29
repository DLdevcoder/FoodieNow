package com.example.foodienow.feature.profile

import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.util.Locale
import androidx.core.os.LocaleListCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.foodienow.R
import com.example.foodienow.domain.model.ThemeMode
import com.example.foodienow.feature.settings.UiPreferencesViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    viewModel: UiPreferencesViewModel = hiltViewModel()
) {
    val uiPreferences by viewModel.uiPreferences.collectAsState()
    val notificationsEnabled = uiPreferences.notificationsEnabled

    val darkModeEnabled = uiPreferences.themeMode == ThemeMode.DARK
    val currentLocales = remember { AppCompatDelegate.getApplicationLocales() }
    val englishLanguage = if (currentLocales.isEmpty) {
        Locale.getDefault().language == "en"
    } else {
        currentLocales.get(0)?.language == "en"
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.settings_back_content_desc)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Column(modifier = Modifier.background(MaterialTheme.colorScheme.surface)) {
                SettingSwitchItem(
                    icon = Icons.Default.NotificationsActive,
                    iconColor = Color(0xFFF59E0B),
                    title = stringResource(R.string.settings_push_notifications),
                    checked = notificationsEnabled,
                    onCheckedChange = { viewModel.setNotificationsEnabled(it) }
                )
                HorizontalDivider(modifier = Modifier.padding(start = 56.dp))

                SettingSwitchItem(
                    icon = Icons.Default.DarkMode,
                    iconColor = if (darkModeEnabled) Color(0xFF9CA3AF) else Color(0xFF374151),
                    title = stringResource(R.string.settings_dark_mode),
                    checked = darkModeEnabled,
                    onCheckedChange = {
                        viewModel.setThemeMode(if (it) ThemeMode.DARK else ThemeMode.LIGHT)
                    }
                )
                HorizontalDivider(modifier = Modifier.padding(start = 56.dp))

                SettingSwitchItem(
                    icon = Icons.Default.Language,
                    iconColor = Color(0xFF3B82F6),
                    title = stringResource(R.string.settings_use_english),
                    checked = englishLanguage,
                    onCheckedChange = {
                        val localeTag = if (it) "en" else "vi"
                        AppCompatDelegate.setApplicationLocales(
                            LocaleListCompat.forLanguageTags(localeTag)
                        )
                    }
                )
            }
        }
    }
}

@Composable
fun SettingSwitchItem(
    icon: ImageVector,
    iconColor: Color,
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = iconColor)
        Spacer(modifier = Modifier.width(16.dp))
        Text(text = title, modifier = Modifier.weight(1f), fontWeight = FontWeight.Medium)
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}
