@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.foodienow.feature.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.foodienow.R
import com.example.foodienow.domain.model.AppLanguage
import com.example.foodienow.domain.model.ThemeMode
import com.example.foodienow.domain.model.User
import com.example.foodienow.feature.auth.AuthViewModel
import com.example.foodienow.feature.settings.UiPreferencesViewModel

@Composable
fun ProfileScreen(
    onBack: () -> Unit,
    onLoggedOut: () -> Unit,
    authViewModel: AuthViewModel = hiltViewModel(),
    uiPreferencesViewModel: UiPreferencesViewModel = hiltViewModel()
) {
    var user by remember { mutableStateOf<User?>(null) }
    var isLoadingProfile by remember { mutableStateOf(true) }
    var isLoggingOut by remember { mutableStateOf(false) }
    val uiPreferences by uiPreferencesViewModel.uiPreferences.collectAsState()

    LaunchedEffect(Unit) {
        user = authViewModel.resolveStoredSession()
        isLoadingProfile = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.profile_title)) }
            )
        }
    ) { padding ->
        if (isLoadingProfile) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                verticalArrangement = Arrangement.Center
            ) {
                CircularProgressIndicator(modifier = Modifier.padding(start = 16.dp))
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = stringResource(R.string.profile_account_information),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(stringResource(R.string.profile_name_value, user?.name.orEmpty()))
            Text(stringResource(R.string.profile_email_value, user?.email.orEmpty()))
            Text(stringResource(R.string.profile_role_value, user?.role?.toDisplayName() ?: "N/A"))

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = stringResource(R.string.settings_language_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            androidx.compose.foundation.layout.Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AppLanguage.entries.forEach { language ->
                    FilterChip(
                        selected = uiPreferences.appLanguage == language,
                        onClick = { uiPreferencesViewModel.setAppLanguage(language) },
                        label = {
                            Text(
                                text = if (language == AppLanguage.ENGLISH) {
                                    stringResource(R.string.settings_language_english)
                                } else {
                                    stringResource(R.string.settings_language_vietnamese)
                                }
                            )
                        }
                    )
                }
            }

            Text(
                text = stringResource(R.string.settings_theme_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            androidx.compose.foundation.layout.Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ThemeMode.entries.forEach { themeMode ->
                    FilterChip(
                        selected = uiPreferences.themeMode == themeMode,
                        onClick = { uiPreferencesViewModel.setThemeMode(themeMode) },
                        label = { Text(themeMode.toDisplayName()) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = {
                    isLoggingOut = true
                    authViewModel.logout()
                    onLoggedOut()
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoggingOut
            ) {
                if (isLoggingOut) {
                    CircularProgressIndicator()
                } else {
                    Text(stringResource(R.string.profile_logout))
                }
            }

            TextButton(
                onClick = onBack,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.common_back))
            }
        }
    }
}

@Composable
private fun ThemeMode.toDisplayName(): String {
    return when (this) {
        ThemeMode.SYSTEM -> stringResource(R.string.settings_theme_system)
        ThemeMode.LIGHT -> stringResource(R.string.settings_theme_light)
        ThemeMode.DARK -> stringResource(R.string.settings_theme_dark)
    }
}

@Composable
private fun com.example.foodienow.domain.model.UserRole.toDisplayName(): String {
    return when (this) {
        com.example.foodienow.domain.model.UserRole.CUSTOMER -> stringResource(R.string.role_customer)
        com.example.foodienow.domain.model.UserRole.MERCHANT -> stringResource(R.string.role_merchant)
        com.example.foodienow.domain.model.UserRole.SHIPPER -> stringResource(R.string.role_shipper)
    }
}

