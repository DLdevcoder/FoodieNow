package com.example.foodienow.feature.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.foodienow.R
import com.example.foodienow.domain.model.User
import kotlinx.coroutines.delay

@Composable
fun LoginScreen(
    onNavigateRegister: () -> Unit,
    onNavigateForgotPassword: () -> Unit,
    onLoginSuccess: (User) -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }

    val isEmailValid = email.isNotBlank() && EMAIL_REGEX.matches(email.trim())
    val isPasswordValid = password.length >= 6
    val canSubmit = !uiState.isLoading && uiState.remainingCooldownSeconds == 0 && isEmailValid && isPasswordValid

    LaunchedEffect(uiState.currentUser?.id) {
        uiState.currentUser?.let { user ->
            onLoginSuccess(user)
            viewModel.consumeLoginState()
        }
    }

    LaunchedEffect(uiState.cooldownUntilMillis) {
        while (uiState.remainingCooldownSeconds > 0) {
            delay(1000)
            viewModel.refreshCooldownState()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = stringResource(R.string.auth_login_title), style = MaterialTheme.typography.headlineMedium)

        Spacer(modifier = Modifier.height(20.dp))

        OutlinedTextField(
            value = email,
            onValueChange = {
                email = it
                viewModel.clearMessage()
            },
            label = { Text(stringResource(R.string.auth_email_label)) },
            modifier = Modifier.fillMaxWidth(),
            isError = email.isNotBlank() && !isEmailValid,
            singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = password,
            onValueChange = {
                password = it
                viewModel.clearMessage()
            },
            label = { Text(stringResource(R.string.auth_password_label)) },
            modifier = Modifier.fillMaxWidth(),
            isError = password.isNotBlank() && !isPasswordValid,
            visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                    Icon(
                        imageVector = if (isPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = if (isPasswordVisible) {
                            stringResource(R.string.auth_hide_password)
                        } else {
                            stringResource(R.string.auth_show_password)
                        }
                    )
                }
            },
            singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        uiState.errorMessage?.let {
            Text(text = it, color = MaterialTheme.colorScheme.error)
        }

        uiState.infoMessage?.let {
            Text(text = it, color = MaterialTheme.colorScheme.primary)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { viewModel.login(email = email, pass = password) },
            modifier = Modifier.fillMaxWidth(),
            enabled = canSubmit
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator()
            } else {
                val title = if (uiState.remainingCooldownSeconds > 0) {
                    stringResource(R.string.auth_try_again_in_seconds, uiState.remainingCooldownSeconds)
                } else {
                    stringResource(R.string.auth_login_button)
                }
                Text(title)
            }
        }

        TextButton(
            onClick = onNavigateForgotPassword,
            modifier = Modifier.fillMaxWidth(),
            enabled = !uiState.isLoading
        ) {
            Text(stringResource(R.string.auth_forgot_password_action))
        }

        TextButton(
            onClick = onNavigateRegister,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.auth_register_action))
        }
    }
}

private val EMAIL_REGEX = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")

