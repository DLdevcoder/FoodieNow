package com.example.foodienow.feature.profile

import android.content.Context
import android.content.ContextWrapper
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import coil3.compose.AsyncImage
import com.example.foodienow.R
import com.example.foodienow.core.designsystem.components.FoodieCard
import com.example.foodienow.core.designsystem.components.FoodieErrorState
import com.example.foodienow.core.designsystem.components.FoodieLoadingState
import com.example.foodienow.core.designsystem.theme.AmberTertiary
import com.example.foodienow.core.designsystem.theme.ErrorRed
import com.example.foodienow.core.designsystem.theme.FoodieCream
import com.example.foodienow.core.designsystem.theme.FoodieNowTheme
import com.example.foodienow.core.designsystem.theme.InfoBlue
import com.example.foodienow.core.designsystem.theme.PromoGradientEnd
import com.example.foodienow.core.designsystem.theme.PromoGradientStart
import com.example.foodienow.core.designsystem.theme.SuccessGreen
import com.example.foodienow.domain.model.UserRole
import java.text.NumberFormat
import java.util.Locale

private data class ProfileMenuItem(
    val icon: ImageVector,
    val iconColor: Color,
    val title: String,
    val subtitle: String? = null,
    val trailingText: String? = null,
    val onClick: () -> Unit
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onBack: () -> Unit,
    onNavigateToOrderHistory: () -> Unit,
    onNavigateToActivityHistory: () -> Unit,
    onLoggedOut: () -> Unit,
    onNavigateToAddress: () -> Unit = {},
    onNavigateToPaymentSettings: () -> Unit = {},
    onNavigateToMustTry: () -> Unit = {},
    onNavigateToVouchers: () -> Unit = {},
    onNavigateToRewardPoints: () -> Unit = {},
    onNavigateToInviteFriends: () -> Unit = {},
    onNavigateToShopOwner: () -> Unit = {},
    onNavigateToHelpCentre: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    onNavigateToWallet: () -> Unit = {},
    onNavigateToEditProfile: () -> Unit = {},
    onNavigateToChangePassword: () -> Unit = {},
    profileViewModel: ProfileViewModel = hiltViewModel()
) {
    val profileUiState by profileViewModel.uiState.collectAsState()
    val context = LocalContext.current
    val lifecycleOwner = remember(context) { context.findLifecycleOwner() }
    val profile = profileUiState.profile
    val fullName = profile?.fullName ?: stringResource(R.string.profile_unnamed)
    val email = profile?.email ?: stringResource(R.string.profile_unnamed_email)
    val avatarUrl = profile?.avatarUrl
    val balance = profile?.balance ?: 0L
    val rewardPoints = profile?.rewardPoints ?: 0
    val formatter = remember { NumberFormat.getCurrencyInstance(Locale("vi", "VN")) }

    var isRefreshing by remember { mutableStateOf(false) }

    DisposableEffect(lifecycleOwner) {
        if (lifecycleOwner == null) {
            onDispose { }
        } else {
            val observer = LifecycleEventObserver { _, event ->
                if (event == Lifecycle.Event.ON_RESUME) {
                    profileViewModel.loadProfile()
                }
            }
            lifecycleOwner.lifecycle.addObserver(observer)
            onDispose {
                lifecycleOwner.lifecycle.removeObserver(observer)
            }
        }
    }

    LaunchedEffect(profileUiState.isLoading) {
        if (!profileUiState.isLoading) isRefreshing = false
    }

    LaunchedEffect(profileUiState.isLoggedOut) {
        if (profileUiState.isLoggedOut) onLoggedOut()
    }

    LaunchedEffect(profileUiState.errorMessage) {
        val message = profileUiState.errorMessage
        if (message != null && profile != null) {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            profileViewModel.clearMessages()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(FoodieCream)
    ) {
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = {
                isRefreshing = true
                profileViewModel.loadProfile()
            },
            modifier = Modifier.fillMaxSize()
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
            item {
                ProfileHeader(
                    fullName = fullName,
                    email = email,
                    avatarUrl = avatarUrl,
                    role = profile?.role,
                    onNavigateToEditProfile = onNavigateToEditProfile
                )
            }

            if (profileUiState.isLoading && profile == null) {
                item {
                    FoodieLoadingState(
                        label = "Đang tải hồ sơ...",
                        modifier = Modifier.fillParentMaxHeight(0.55f)
                    )
                }
            } else if (profileUiState.errorMessage != null && profile == null) {
                item {
                    FoodieErrorState(
                        title = "Không thể tải hồ sơ",
                        subtitle = profileUiState.errorMessage.orEmpty(),
                        actionLabel = "Thử lại",
                        onAction = profileViewModel::loadProfile,
                        modifier = Modifier.fillParentMaxHeight(0.55f)
                    )
                }
            } else {
                item {
                    QuickStatsRow(
                        balance = formatter.format(balance),
                        rewardPoints = rewardPoints.toString(),
                        voucherLabel = "Kho mã",
                        onNavigateToWallet = onNavigateToWallet,
                        onNavigateToRewardPoints = onNavigateToRewardPoints,
                        onNavigateToVouchers = onNavigateToVouchers
                    )
                }

                item {
                    ProfileMenuSection(
                        title = "Đơn hàng và ưu đãi",
                        items = listOf(
                            ProfileMenuItem(
                                icon = Icons.AutoMirrored.Filled.ReceiptLong,
                                iconColor = AmberTertiary,
                                title = stringResource(R.string.order_history_title),
                                subtitle = "Theo dõi đơn đã đặt và trạng thái giao hàng",
                                onClick = onNavigateToOrderHistory
                            ),
                            ProfileMenuItem(
                                icon = Icons.Default.History,
                                iconColor = InfoBlue,
                                title = stringResource(R.string.activity_history_title),
                                subtitle = "Xem thanh toán, điểm thưởng và hoạt động gần đây",
                                onClick = onNavigateToActivityHistory
                            ),
                            ProfileMenuItem(
                                icon = Icons.Default.ThumbUp,
                                iconColor = MaterialTheme.colorScheme.primary,
                                title = stringResource(R.string.me_must_try),
                                subtitle = "Các món phù hợp khẩu vị của bạn",
                                onClick = onNavigateToMustTry
                            )
                        )
                    )
                }

                item {
                    ProfileMenuSection(
                        title = "Tài khoản",
                        items = listOf(
                            ProfileMenuItem(
                                icon = Icons.Default.LocationOn,
                                iconColor = SuccessGreen,
                                title = stringResource(R.string.me_address),
                                subtitle = "Quản lý địa chỉ giao hàng mặc định",
                                onClick = onNavigateToAddress
                            ),
                            ProfileMenuItem(
                                icon = Icons.Default.Payment,
                                iconColor = InfoBlue,
                                title = stringResource(R.string.me_payment),
                                subtitle = "COD, FoodiePay và ví điện tử",
                                onClick = onNavigateToPaymentSettings
                            ),
                            ProfileMenuItem(
                                icon = Icons.Default.PersonAdd,
                                iconColor = MaterialTheme.colorScheme.primary,
                                title = stringResource(R.string.me_invite),
                                subtitle = "Nhận voucher khi bạn bè đặt đơn đầu tiên",
                                onClick = onNavigateToInviteFriends
                            )
                        )
                    )
                }

                item {
                    ProfileMenuSection(
                        title = "Hỗ trợ và cài đặt",
                        items = listOf(
                            ProfileMenuItem(
                                icon = Icons.Default.Storefront,
                                iconColor = AmberTertiary,
                                title = stringResource(R.string.me_shop_owners),
                                subtitle = "Mở gian hàng và quản lý đơn bán",
                                onClick = onNavigateToShopOwner
                            ),
                            ProfileMenuItem(
                                icon = Icons.AutoMirrored.Filled.HelpOutline,
                                iconColor = SuccessGreen,
                                title = stringResource(R.string.me_help),
                                subtitle = "Câu hỏi thường gặp và hỗ trợ khách hàng",
                                onClick = onNavigateToHelpCentre
                            ),
                            ProfileMenuItem(
                                icon = Icons.Default.Settings,
                                iconColor = InfoBlue,
                                title = stringResource(R.string.me_settings),
                                subtitle = "Ngôn ngữ, giao diện và thông báo",
                                onClick = onNavigateToSettings
                            ),
                            ProfileMenuItem(
                                icon = Icons.Default.Lock,
                                iconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                title = stringResource(R.string.profile_change_password),
                                subtitle = "Cập nhật mật khẩu đăng nhập",
                                onClick = onNavigateToChangePassword
                            )
                        )
                    )
                }

                item {
                    ProfileMenuSection(
                        title = null,
                        items = listOf(
                            ProfileMenuItem(
                                icon = Icons.AutoMirrored.Filled.ExitToApp,
                                iconColor = ErrorRed,
                                title = stringResource(R.string.profile_logout_btn),
                                subtitle = "Thoát khỏi tài khoản trên thiết bị này",
                                onClick = profileViewModel::logout
                            )
                        )
                    )
                }

                item {
                    Spacer(
                        modifier = Modifier
                            .height(24.dp)
                            .navigationBarsPadding()
                    )
                }
            }
            }
        }

        if (profileUiState.isLoggingOut) {
            LogoutLoadingScreen()
        }
    }
}

@Composable
private fun LogoutLoadingScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(PromoGradientStart, MaterialTheme.colorScheme.primary, PromoGradientEnd)
                )
            )
            .clickable(onClick = {}),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 30.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Surface(
                modifier = Modifier.size(72.dp),
                shape = CircleShape,
                color = Color.White.copy(alpha = 0.2f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(42.dp),
                        strokeWidth = 3.dp
                    )
                }
            }
            Text(
                text = "FoodieNow",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
            )
            FoodieCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(horizontal = 22.dp, vertical = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Đang đăng xuất",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "FoodieNow đang kết thúc phiên đăng nhập và bảo vệ thông tin tài khoản trên thiết bị này.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(CircleShape),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.primaryContainer
                )
            }
            }
        }
    }
}

private tailrec fun Context.findLifecycleOwner(): LifecycleOwner? {
    return when (this) {
        is LifecycleOwner -> this
        is ContextWrapper -> baseContext.findLifecycleOwner()
        else -> null
    }
}

@Composable
private fun ProfileHeader(
    fullName: String,
    email: String,
    avatarUrl: String?,
    role: UserRole?,
    onNavigateToEditProfile: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    colors = listOf(PromoGradientStart, MaterialTheme.colorScheme.primary, PromoGradientEnd)
                )
            )
            .statusBarsPadding()
            .padding(start = 18.dp, top = 16.dp, end = 18.dp, bottom = 44.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(22.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.profile_header_title),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Quản lý tài khoản FoodieNow",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.82f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Surface(
                    shape = CircleShape,
                    color = Color.White.copy(alpha = 0.18f),
                    contentColor = Color.White
                ) {
                    IconButton(onClick = onNavigateToEditProfile, modifier = Modifier.size(44.dp)) {
                        Icon(Icons.Default.Edit, contentDescription = "Chỉnh sửa hồ sơ")
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ProfileAvatar(fullName = fullName, avatarUrl = avatarUrl)
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = fullName,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = email,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.84f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    RoleBadge(role = role)
                }
            }
        }
    }
}

@Composable
private fun ProfileAvatar(fullName: String, avatarUrl: String?) {
    val initials = remember(fullName) {
        fullName
            .trim()
            .split(Regex("\\s+"))
            .filter { it.isNotBlank() }
            .take(2)
            .mapNotNull { it.firstOrNull()?.uppercaseChar() }
            .joinToString("")
            .ifBlank { "F" }
    }

    Box(
        modifier = Modifier
            .size(72.dp)
            .clip(CircleShape)
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        if (!avatarUrl.isNullOrBlank()) {
            AsyncImage(
                model = avatarUrl,
                contentDescription = "Ảnh đại diện",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            Text(
                text = initials,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun RoleBadge(role: UserRole?) {
    Surface(
        shape = CircleShape,
        color = Color.White.copy(alpha = 0.18f),
        contentColor = Color.White
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.VerifiedUser, contentDescription = null, modifier = Modifier.size(15.dp))
            Text(
                text = when (role) {
                    UserRole.CUSTOMER -> stringResource(R.string.role_customer)
                    UserRole.MERCHANT -> stringResource(R.string.role_merchant)
                    UserRole.SHIPPER -> stringResource(R.string.role_shipper)
                    null -> "FoodieNow"
                },
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun QuickStatsRow(
    balance: String,
    rewardPoints: String,
    voucherLabel: String,
    onNavigateToWallet: () -> Unit,
    onNavigateToRewardPoints: () -> Unit,
    onNavigateToVouchers: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp)
            .offset(y = (-28).dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        QuickStatCard(
            modifier = Modifier.weight(1f),
            title = stringResource(R.string.profile_wallet_foodiepay),
            value = balance,
            icon = Icons.Default.AccountBalanceWallet,
            iconColor = InfoBlue,
            onClick = onNavigateToWallet
        )
        QuickStatCard(
            modifier = Modifier.weight(1f),
            title = stringResource(R.string.profile_foodiecoins),
            value = rewardPoints,
            icon = Icons.Default.MonetizationOn,
            iconColor = AmberTertiary,
            onClick = onNavigateToRewardPoints
        )
        QuickStatCard(
            modifier = Modifier.weight(1f),
            title = stringResource(R.string.profile_vouchers),
            value = voucherLabel,
            icon = Icons.Default.ConfirmationNumber,
            iconColor = SuccessGreen,
            onClick = onNavigateToVouchers
        )
    }
}

@Composable
private fun QuickStatCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    icon: ImageVector,
    iconColor: Color,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier
            .height(108.dp)
            .clickable(onClick = onClick),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = FoodieNowTheme.elevation.card,
        shadowElevation = 3.dp
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(26.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.ExtraBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun ProfileMenuSection(
    title: String?,
    items: List<ProfileMenuItem>
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        if (title != null) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(horizontal = 2.dp)
            )
        }

        FoodieCard(modifier = Modifier.fillMaxWidth()) {
            Column {
                items.forEachIndexed { index, item ->
                    ProfileMenuRow(item = item)
                    if (index < items.lastIndex) {
                        HorizontalDivider(
                            modifier = Modifier.padding(start = 64.dp),
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.22f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfileMenuRow(item: ProfileMenuItem) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = item.onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(MaterialTheme.shapes.medium)
                .background(item.iconColor.copy(alpha = 0.14f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = item.icon,
                contentDescription = null,
                tint = item.iconColor,
                modifier = Modifier.size(21.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (item.subtitle != null) {
                Text(
                    text = item.subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        if (item.trailingText != null) {
            Text(
                text = item.trailingText,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(8.dp))
        }
        Icon(
            Icons.Default.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
        )
    }
}
