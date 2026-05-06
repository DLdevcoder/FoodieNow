package com.example.foodienow.feature.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.foodienow.R
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person

@Composable
fun ForgotPasswordScreen(
    onBackToLogin: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var email by remember { mutableStateOf("") }
    val snackbarHostState = remember { SnackbarHostState() }

    val isEmailValid = email.isNotBlank() && EMAIL_REGEX.matches(email.trim())
    val isEmailError = email.isNotBlank() && !isEmailValid

    LaunchedEffect(Unit) {
        viewModel.clearMessage()
    }

    LaunchedEffect(uiState.errorMessage, uiState.infoMessage) {
        val message = uiState.errorMessage ?: uiState.infoMessage
        if (message != null) {
            snackbarHostState.showSnackbar(message)
            viewModel.clearMessage()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .imePadding()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Surface(
                shape = MaterialTheme.shapes.large,
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(12.dp)
                )
            }
            Text(text = stringResource(R.string.auth_forgot_password_title), style = MaterialTheme.typography.headlineMedium)
            Text(
                text = stringResource(R.string.auth_forgot_password_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            OutlinedTextField(
                value = email,
                onValueChange = {
                    email = it
                    viewModel.clearMessage()
                },
                label = { Text(stringResource(R.string.auth_email_label)) },
                modifier = Modifier.fillMaxWidth(),
                isError = isEmailError,
                supportingText = {
                    if (isEmailError) {
                        Text(stringResource(R.string.auth_error_invalid_email))
                    }
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done)
            )

            Button(
                onClick = { viewModel.forgotPassword(email) },
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isLoading && isEmailValid
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp))
                } else {
                    Text(stringResource(R.string.auth_forgot_password_send))
                }
            }


            TextButton(
                onClick = onBackToLogin,
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isLoading
            ) {
                Text(stringResource(R.string.auth_back_to_login))
            }
        }
    }
}

private val EMAIL_REGEX = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")
