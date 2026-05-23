package com.example.foodienow.feature.profile

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil3.compose.AsyncImage
import com.example.foodienow.core.designsystem.components.FoodieCard
import com.example.foodienow.core.designsystem.components.FoodieErrorState
import com.example.foodienow.core.designsystem.components.FoodieLoadingState
import com.example.foodienow.core.designsystem.components.FoodieTopAppBar
import com.example.foodienow.core.designsystem.theme.FoodieCream
import com.example.foodienow.core.designsystem.theme.FoodieCreamSurface
import com.example.foodienow.core.designsystem.theme.InfoBlue
import com.example.foodienow.core.designsystem.theme.PromoGradientEnd
import com.example.foodienow.core.designsystem.theme.PromoGradientStart
import com.example.foodienow.core.designsystem.theme.SuccessGreen
import com.example.foodienow.domain.model.Profile

@Composable
fun EditProfileScreen(
    onBack: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val profile = uiState.profile

    LaunchedEffect(uiState.infoMessage, uiState.errorMessage) {
        uiState.infoMessage?.let {
            val shouldClose = uiState.closeEditAfterInfo
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearMessages()
            if (shouldClose) onBack()
        }
        uiState.errorMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearMessages()
        }
    }

    Scaffold(
        topBar = {
            FoodieTopAppBar(
                title = "Chỉnh sửa hồ sơ",
                onBack = onBack
            )
        },
        containerColor = FoodieCream
    ) { paddingValues ->
        when {
            uiState.isLoading && profile == null -> {
                FoodieLoadingState(
                    label = "Đang tải hồ sơ...",
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .background(FoodieCream)
                )
            }

            profile == null -> {
                FoodieErrorState(
                    title = "Không thể tải hồ sơ",
                    subtitle = uiState.errorMessage ?: "Vui lòng thử lại sau.",
                    actionLabel = "Thử lại",
                    onAction = viewModel::loadProfile,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .background(FoodieCream)
                )
            }

            else -> {
                EditProfileContent(
                    profile = profile,
                    isSaving = uiState.isSaving,
                    isAvatarSaving = uiState.isAvatarSaving,
                    onFullNameChange = viewModel::onFullNameChange,
                    onPhoneChange = viewModel::onPhoneChange,
                    onAvatarSelected = viewModel::uploadAvatar,
                    onDeleteAvatar = viewModel::deleteAvatar,
                    onSave = viewModel::saveProfile,
                    modifier = Modifier.padding(paddingValues)
                )
            }
        }
    }
}

@Composable
private fun EditProfileContent(
    profile: Profile,
    isSaving: Boolean,
    isAvatarSaving: Boolean,
    onFullNameChange: (String) -> Unit,
    onPhoneChange: (String) -> Unit,
    onAvatarSelected: (ByteArray) -> Unit,
    onDeleteAvatar: () -> Unit,
    onSave: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val avatarPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri ?: return@rememberLauncherForActivityResult
        val imageBytes = runCatching {
            context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
        }.getOrNull()

        if (imageBytes == null) {
            Toast.makeText(context, "Không đọc được ảnh đã chọn.", Toast.LENGTH_SHORT).show()
        } else {
            onAvatarSelected(imageBytes)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(FoodieCream)
            .verticalScroll(rememberScrollState())
            .imePadding()
            .navigationBarsPadding()
            .padding(horizontal = 18.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        EditProfileHeader(profile = profile)

        FoodieCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                SectionTitle(
                    icon = Icons.Default.AddAPhoto,
                    title = "Ảnh đại diện",
                    subtitle = "Ảnh này hiển thị trên hồ sơ và trong các cuộc trò chuyện."
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ProfileAvatarPreview(
                        profile = profile,
                        modifier = Modifier.size(76.dp),
                        backgroundColor = FoodieCreamSurface,
                        initialsColor = MaterialTheme.colorScheme.primary
                    )

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                avatarPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            enabled = !isAvatarSaving,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.AddAPhoto, contentDescription = null)
                            Text(
                                text = if (profile.avatarUrl.isNullOrBlank()) "Tải ảnh lên" else "Đổi ảnh",
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }

                        if (!profile.avatarUrl.isNullOrBlank()) {
                            TextButton(
                                onClick = onDeleteAvatar,
                                enabled = !isAvatarSaving,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = null)
                                Text(
                                    text = "Xóa ảnh đại diện",
                                    modifier = Modifier.padding(start = 8.dp)
                                )
                            }
                        }

                        if (isAvatarSaving) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    strokeWidth = 2.dp
                                )
                                Text(
                                    text = "Đang cập nhật ảnh...",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }

        FoodieCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                SectionTitle(
                    icon = Icons.Default.VerifiedUser,
                    title = "Tài khoản bảo mật",
                    subtitle = "Email dùng để đăng nhập và nhận mã xác thực."
                )

                OutlinedTextField(
                    value = profile.email,
                    onValueChange = {},
                    label = { Text("Email đăng ký") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = false,
                    singleLine = true,
                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                    colors = editProfileFieldColors()
                )

                Surface(
                    shape = MaterialTheme.shapes.large,
                    color = FoodieCreamSurface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.14f))
                ) {
                    Text(
                        text = "Email không thể thay đổi trong ứng dụng. FoodieNow dùng email này để gửi mã bảo mật khi đổi mật khẩu.",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        FoodieCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                SectionTitle(
                    icon = Icons.Default.Person,
                    title = "Thông tin người dùng",
                    subtitle = "Tên và số điện thoại dùng để liên hệ khi cần."
                )

                OutlinedTextField(
                    value = profile.fullName,
                    onValueChange = onFullNameChange,
                    label = { Text("Họ và tên") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Words,
                        imeAction = ImeAction.Next
                    ),
                    colors = editProfileFieldColors()
                )

                OutlinedTextField(
                    value = profile.phone.orEmpty(),
                    onValueChange = onPhoneChange,
                    label = { Text("Số điện thoại") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Phone,
                        imeAction = ImeAction.Done
                    ),
                    colors = editProfileFieldColors()
                )
            }
        }

        Button(
            onClick = onSave,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            enabled = !isSaving && profile.fullName.isNotBlank(),
            shape = MaterialTheme.shapes.large,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
        ) {
            if (isSaving) {
                CircularProgressIndicator(
                    color = Color.White,
                    modifier = Modifier.size(22.dp),
                    strokeWidth = 2.dp
                )
            } else {
                Text(
                    text = "Lưu thay đổi",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
private fun EditProfileHeader(profile: Profile) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.extraLarge)
            .background(
                Brush.linearGradient(
                    listOf(PromoGradientStart, MaterialTheme.colorScheme.primary, PromoGradientEnd)
                )
            )
            .padding(18.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ProfileAvatarPreview(
                profile = profile,
                modifier = Modifier.size(58.dp),
                backgroundColor = Color.White.copy(alpha = 0.22f),
                initialsColor = Color.White
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Hồ sơ giao món",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White.copy(alpha = 0.82f)
                )
                Text(
                    text = profile.fullName.ifBlank { "Người dùng FoodieNow" },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = profile.email,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.86f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun ProfileAvatarPreview(
    profile: Profile,
    modifier: Modifier = Modifier,
    backgroundColor: Color = MaterialTheme.colorScheme.primaryContainer,
    initialsColor: Color = MaterialTheme.colorScheme.primary
) {
    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        if (!profile.avatarUrl.isNullOrBlank()) {
            AsyncImage(
                model = profile.avatarUrl,
                contentDescription = "Ảnh đại diện",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            Text(
                text = profile.fullName.trim().firstOrNull()?.uppercase() ?: "F",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold,
                color = initialsColor
            )
        }
    }
}

@Composable
private fun SectionTitle(
    icon: ImageVector,
    title: String,
    subtitle: String
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.Top
    ) {
        Surface(
            modifier = Modifier.size(34.dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer,
            contentColor = if (icon == Icons.Default.VerifiedUser) SuccessGreen else MaterialTheme.colorScheme.primary
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp))
            }
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun editProfileFieldColors(): TextFieldColors {
    return OutlinedTextFieldDefaults.colors(
        focusedContainerColor = MaterialTheme.colorScheme.surface,
        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
        disabledContainerColor = FoodieCreamSurface,
        focusedBorderColor = MaterialTheme.colorScheme.primary,
        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.55f),
        focusedLeadingIconColor = MaterialTheme.colorScheme.primary,
        unfocusedLeadingIconColor = InfoBlue,
        disabledLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
        disabledTextColor = MaterialTheme.colorScheme.onSurface,
        disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
    )
}
