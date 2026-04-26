package com.example.foodienow.feature.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material3.FilterChip
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.foodienow.R
import com.example.foodienow.domain.model.UserRole

@Composable
fun RegisterScreen(
    onBackToLogin: () -> Unit,
    onRegisterSuccess: (String) -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var selectedRole by remember { mutableStateOf(UserRole.CUSTOMER) }
    var localError by remember { mutableStateOf<String?>(null) }
    var isPasswordVisible by remember { mutableStateOf(false) }
    var isConfirmPasswordVisible by remember { mutableStateOf(false) }

    val isEmailValid = email.isNotBlank() && EMAIL_REGEX.matches(email.trim())
    val emptyEmailPasswordError = stringResource(R.string.auth_error_empty_email_password)
    val invalidEmailError = stringResource(R.string.auth_error_invalid_email)
    val minPasswordError = stringResource(R.string.auth_error_password_min_length)
    val passwordMismatchError = stringResource(R.string.auth_error_password_mismatch)

    LaunchedEffect(uiState.pendingVerificationEmail) {
        uiState.pendingVerificationEmail?.let { emailForVerify ->
            onRegisterSuccess(emailForVerify)
            viewModel.consumeVerificationTarget()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = stringResource(R.string.auth_register_title), style = MaterialTheme.typography.headlineMedium)

        Spacer(modifier = Modifier.height(20.dp))

        OutlinedTextField(
            value = email,
            onValueChange = {
                email = it
                localError = null
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
                localError = null
                viewModel.clearMessage()
            },
            label = { Text(stringResource(R.string.auth_password_label)) },
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
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = confirmPassword,
            onValueChange = {
                confirmPassword = it
                localError = null
                viewModel.clearMessage()
            },
            label = { Text(stringResource(R.string.auth_confirm_password_label)) },
            visualTransformation = if (isConfirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = { isConfirmPasswordVisible = !isConfirmPasswordVisible }) {
                    Icon(
                        imageVector = if (isConfirmPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = if (isConfirmPasswordVisible) {
                            stringResource(R.string.auth_hide_confirm_password)
                        } else {
                            stringResource(R.string.auth_show_confirm_password)
                        }
                    )
                }
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(text = stringResource(R.string.auth_role_label), modifier = Modifier.fillMaxWidth())
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            UserRole.entries.forEach { role ->
                FilterChip(
                    selected = selectedRole == role,
                    onClick = { selectedRole = role },
                    label = { Text(role.toDisplayName()) }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        localError?.let {
            Text(text = it, color = MaterialTheme.colorScheme.error)
        }

        uiState.errorMessage?.let {
            Text(text = it, color = MaterialTheme.colorScheme.error)
        }

        uiState.infoMessage?.let {
            Text(text = it, color = MaterialTheme.colorScheme.primary)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                when {
                    email.isBlank() || password.isBlank() -> {
                        localError = emptyEmailPasswordError
                    }
                    !isEmailValid -> {
                        localError = invalidEmailError
                    }
                    password.length < 6 -> {
                        localError = minPasswordError
                    }
                    password != confirmPassword -> {
                        localError = passwordMismatchError
                    }
                    else -> {
                        localError = null
                        viewModel.register(email = email, pass = password, role = selectedRole)
                    }
                }
            },
            enabled = !uiState.isLoading,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator()
            } else {
                Text(stringResource(R.string.auth_create_account_button))
            }
        }

        TextButton(
            onClick = onBackToLogin,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.auth_back_to_login_action))
        }
    }
}

@Composable
private fun UserRole.toDisplayName(): String {
    return when (this) {
        UserRole.CUSTOMER -> stringResource(R.string.role_customer)
        UserRole.MERCHANT -> stringResource(R.string.role_merchant)
        UserRole.SHIPPER -> stringResource(R.string.role_shipper)
    }
}

private val EMAIL_REGEX = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")

