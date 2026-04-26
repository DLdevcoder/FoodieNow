@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.foodienow.feature.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.foodienow.R
import com.example.foodienow.domain.model.AppLanguage
import com.example.foodienow.domain.model.ThemeMode
import com.example.foodienow.feature.auth.AuthViewModel
import com.example.foodienow.feature.settings.UiPreferencesViewModel

@Composable
fun ProfileScreen(
    onBack: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onLoggedOut: () -> Unit,
    authViewModel: AuthViewModel = hiltViewModel(),
    profileViewModel: ProfileViewModel = hiltViewModel(),
    uiPreferencesViewModel: UiPreferencesViewModel = hiltViewModel()
) {
    val profileUiState by profileViewModel.uiState.collectAsState()
    val uiPreferences by uiPreferencesViewModel.uiPreferences.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.profile_title)) }
            )
        }
    ) { padding ->
        if (profileUiState.isLoading) {
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

            profileUiState.profile?.let { profile ->
                OutlinedTextField(
                    value = profile.fullName,
                    onValueChange = profileViewModel::onFullNameChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(stringResource(R.string.profile_name_label)) },
                    singleLine = true
                )
                OutlinedTextField(
                    value = profile.email,
                    onValueChange = { },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(stringResource(R.string.auth_email_label)) },
                    enabled = false,
                    singleLine = true
                )
                OutlinedTextField(
                    value = profile.phone.orEmpty(),
                    onValueChange = profileViewModel::onPhoneChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(stringResource(R.string.profile_phone_label)) },
                    singleLine = true
                )
                OutlinedTextField(
                    value = profile.address.orEmpty(),
                    onValueChange = profileViewModel::onAddressChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(stringResource(R.string.profile_address_label)) }
                )
                Text(stringResource(R.string.profile_role_value, profile.role.toDisplayName()))

                Button(
                    onClick = profileViewModel::saveProfile,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !profileUiState.isSaving
                ) {
                    if (profileUiState.isSaving) {
                        CircularProgressIndicator()
                    } else {
                        Text(stringResource(R.string.profile_save_button))
                    }
                }
            }

            profileUiState.errorMessage?.let {
                Text(text = it, color = MaterialTheme.colorScheme.error)
            }

            profileUiState.infoMessage?.let {
                Text(text = it, color = MaterialTheme.colorScheme.primary)
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = stringResource(R.string.settings_language_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Row(
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
            Row(
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
                onClick = onNavigateToHistory,
                modifier = Modifier.fillMaxWidth(),
                enabled = !profileUiState.isSaving
            ) {
                Text(stringResource(R.string.activity_history_title))
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    authViewModel.logout()
                    onLoggedOut()
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !profileUiState.isSaving
            ) {
                Text(stringResource(R.string.profile_logout))
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

