package com.example.foodienow.feature.auth

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.DeliveryDining
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.foodienow.R
import com.example.foodienow.core.designsystem.components.FoodieCard
import com.example.foodienow.core.designsystem.components.FoodieLoadingOverlay
import com.example.foodienow.core.designsystem.theme.FoodieCream
import com.example.foodienow.core.designsystem.theme.FoodieCreamSurface
import com.example.foodienow.core.designsystem.theme.InfoBlue
import com.example.foodienow.core.designsystem.theme.PromoGradientEnd
import com.example.foodienow.core.designsystem.theme.PromoGradientStart
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
    val snackbarHostState = remember { SnackbarHostState() }

    val isEmailValid = email.isNotBlank() && EMAIL_REGEX.matches(email.trim())
    val isEmailError = email.isNotBlank() && !isEmailValid
    val isPasswordError = password.isNotBlank() && password.length < 6
    val isConfirmPasswordError = confirmPassword.isNotBlank() && password != confirmPassword
    val canSubmit = !uiState.isLoading &&
        isEmailValid &&
        password.length >= 6 &&
        confirmPassword == password

    val emptyEmailPasswordError = "Email và mật khẩu không được để trống."
    val invalidEmailError = "Email chưa đúng định dạng."
    val minPasswordError = "Mật khẩu tối thiểu 6 ký tự."
    val passwordMismatchError = "Mật khẩu xác nhận không khớp."

    LaunchedEffect(uiState.pendingVerificationEmail) {
        uiState.pendingVerificationEmail?.let { emailForVerify ->
            onRegisterSuccess(emailForVerify)
            viewModel.consumeVerificationTarget()
        }
    }

    LaunchedEffect(uiState.errorMessage, uiState.infoMessage) {
        val message = uiState.errorMessage ?: uiState.infoMessage
        if (message != null) {
            snackbarHostState.showSnackbar(message)
            viewModel.clearMessage()
        }
    }

    LaunchedEffect(localError) {
        localError?.let { message ->
            snackbarHostState.showSnackbar(message)
            localError = null
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize().imePadding(),
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        containerColor = FoodieCream
    ) { paddingValues ->
        FoodieLoadingOverlay(isLoading = uiState.isLoading)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(FoodieCream)
                .verticalScroll(rememberScrollState())
                .padding(paddingValues)
                .padding(horizontal = 18.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            AuthBrandPanel(
                eyebrow = "Tham gia FoodieNow",
                title = "Tạo tài khoản",
                subtitle = "Lưu địa chỉ, nhận voucher riêng và theo dõi mọi đơn hàng trong một nơi.",
                benefits = listOf(
                    AuthFeatureBenefit(Icons.Default.LocalOffer, "Ưu đãi mới"),
                    AuthFeatureBenefit(Icons.Default.DeliveryDining, "Theo dõi đơn"),
                    AuthFeatureBenefit(Icons.Default.Fastfood, "Đặt lại nhanh")
                )
            )

            FoodieCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "Thông tin tài khoản",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Sau khi tạo tài khoản, bạn sẽ xác thực email để kích hoạt đăng nhập.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    OutlinedTextField(
                        value = email,
                        onValueChange = {
                            email = it
                            localError = null
                            viewModel.clearMessage()
                        },
                        label = { Text("Email") },
                        modifier = Modifier.fillMaxWidth(),
                        isError = isEmailError,
                        supportingText = if (isEmailError) { { Text("Email chưa đúng định dạng.") } } else null,
                        singleLine = true,
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Next
                        ),
                        colors = authFieldColors()
                    )

                    PasswordInput(
                        value = password,
                        onValueChange = {
                            password = it
                            localError = null
                            viewModel.clearMessage()
                        },
                        label = "Mật khẩu",
                        isError = isPasswordError,
                        errorText = "Mật khẩu tối thiểu 6 ký tự.",
                        isVisible = isPasswordVisible,
                        onToggleVisible = { isPasswordVisible = !isPasswordVisible },
                        imeAction = ImeAction.Next
                    )

                    PasswordInput(
                        value = confirmPassword,
                        onValueChange = {
                            confirmPassword = it
                            localError = null
                            viewModel.clearMessage()
                        },
                        label = "Nhập lại mật khẩu",
                        isError = isConfirmPasswordError,
                        errorText = "Mật khẩu xác nhận không khớp.",
                        isVisible = isConfirmPasswordVisible,
                        onToggleVisible = { isConfirmPasswordVisible = !isConfirmPasswordVisible },
                        imeAction = ImeAction.Done
                    )

                    AccountTypeSelector(
                        selectedRole = selectedRole,
                        onRoleSelected = { selectedRole = it }
                    )

                    Button(
                        onClick = {
                            when {
                                email.isBlank() || password.isBlank() -> localError = emptyEmailPasswordError
                                !isEmailValid -> localError = invalidEmailError
                                password.length < 6 -> localError = minPasswordError
                                password != confirmPassword -> localError = passwordMismatchError
                                else -> {
                                    localError = null
                                    viewModel.register(email = email, pass = password, role = selectedRole)
                                }
                            }
                        },
                        enabled = canSubmit,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = MaterialTheme.shapes.large,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                    ) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(22.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Tạo tài khoản", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            AuthSwitchPanel(
                title = "Đã có tài khoản?",
                action = "Đăng nhập",
                onClick = onBackToLogin,
                enabled = !uiState.isLoading
            )

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun PasswordInput(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    isError: Boolean,
    errorText: String,
    isVisible: Boolean,
    onToggleVisible: () -> Unit,
    imeAction: ImeAction
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        visualTransformation = if (isVisible) VisualTransformation.None else PasswordVisualTransformation(),
        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
        trailingIcon = {
            IconButton(onClick = onToggleVisible) {
                Icon(
                    imageVector = if (isVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                    contentDescription = if (isVisible) {
                        stringResource(R.string.auth_hide_password)
                    } else {
                        stringResource(R.string.auth_show_password)
                    }
                )
            }
        },
        modifier = Modifier.fillMaxWidth(),
        isError = isError,
        supportingText = if (isError) { { Text(errorText) } } else null,
        singleLine = true,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Password,
            imeAction = imeAction
        ),
        colors = authFieldColors()
    )
}

@Composable
private fun AccountTypeSelector(
    selectedRole: UserRole,
    onRoleSelected: (UserRole) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "Loại tài khoản",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            UserRole.entries.filter { it != UserRole.ADMIN }.forEach { role ->
                FilterChip(
                    selected = selectedRole == role,
                    onClick = { onRoleSelected(role) },
                    label = { Text(role.toDisplayName()) },
                    leadingIcon = if (selectedRole == role) {
                        {
                            Icon(
                                Icons.Default.Badge,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    } else {
                        null
                    },
                    shape = MaterialTheme.shapes.large,
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        selectedLeadingIconColor = MaterialTheme.colorScheme.primary
                    )
                )
            }
        }
    }
}

private data class AuthFeatureBenefit(
    val icon: ImageVector,
    val label: String
)

@Composable
private fun AuthBrandPanel(
    eyebrow: String,
    title: String,
    subtitle: String,
    benefits: List<AuthFeatureBenefit>
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .clip(MaterialTheme.shapes.extraLarge)
            .background(
                Brush.linearGradient(
                    listOf(PromoGradientStart, MaterialTheme.colorScheme.primary, PromoGradientEnd)
                )
            )
            .padding(18.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier.size(56.dp),
                    shape = CircleShape,
                    color = Color.White.copy(alpha = 0.2f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Default.Fastfood,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(30.dp)
                        )
                    }
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = eyebrow,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White.copy(alpha = 0.84f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = title,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.9f),
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                benefits.forEach { benefit ->
                    AuthBenefitChip(
                        icon = benefit.icon,
                        label = benefit.label,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun AuthBenefitChip(
    icon: ImageVector,
    label: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.height(38.dp),
        shape = MaterialTheme.shapes.large,
        color = Color.White.copy(alpha = 0.18f),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.size(6.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun AuthSwitchPanel(
    title: String,
    action: String,
    onClick: () -> Unit,
    enabled: Boolean
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = FoodieCreamSurface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.18f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            TextButton(onClick = onClick, enabled = enabled) {
                Text(action, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun authFieldColors(): TextFieldColors {
    return OutlinedTextFieldDefaults.colors(
        focusedContainerColor = MaterialTheme.colorScheme.surface,
        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
        focusedBorderColor = MaterialTheme.colorScheme.primary,
        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.55f),
        focusedLeadingIconColor = MaterialTheme.colorScheme.primary,
        unfocusedLeadingIconColor = InfoBlue,
        errorLeadingIconColor = MaterialTheme.colorScheme.error,
        focusedLabelColor = MaterialTheme.colorScheme.primary
    )
}

@Composable
private fun UserRole.toDisplayName(): String {
    return when (this) {
        UserRole.CUSTOMER -> stringResource(R.string.role_customer)
        UserRole.MERCHANT -> stringResource(R.string.role_merchant)
        UserRole.SHIPPER -> stringResource(R.string.role_shipper)
        UserRole.ADMIN -> "Admin"
    }
}

private val EMAIL_REGEX = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")
