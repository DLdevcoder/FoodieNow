package com.example.foodienow.feature.profile

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil3.compose.AsyncImage
import com.example.foodienow.R
import java.text.NumberFormat
import java.util.Locale

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
    val user = profileUiState.user
    val profile = profileUiState.profile
    val fullName = profile?.fullName ?: "Chưa cập nhật tên"
    val email = profile?.email ?: "Chưa cập nhật email"
    val balance = profile?.balance ?: 0.0
    val rewardPoints = profile?.rewardPoints ?: 0
    val formatter = remember { NumberFormat.getCurrencyInstance(Locale("vi", "VN")) }

    var isRefreshing by remember { mutableStateOf(false) }

    LaunchedEffect(profileUiState.isLoading) {
        if (!profileUiState.isLoading) {
            isRefreshing = false
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF3F4F6))
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
                modifier = Modifier.fillMaxSize()
            ) {
            item {
                ProfileHeader(fullName = fullName, email = email, onNavigateToEditProfile = onNavigateToEditProfile)
            }

            item {
                QuickStatsRow(
                    balance = formatter.format(balance),
                    rewardPoints = rewardPoints.toString(),
                    onNavigateToWallet = onNavigateToWallet,
                    onNavigateToRewardPoints = onNavigateToRewardPoints,
                    onNavigateToVouchers = onNavigateToVouchers
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            item {
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column {
                            MenuItem(
                                icon = Icons.Default.History,
                                iconColor = Color(0xFF10B981), // Green
                                title = stringResource(R.string.activity_history_title),
                                onClick = onNavigateToActivityHistory
                            )
                            HorizontalDivider(modifier = Modifier.padding(start = 56.dp), color = Color(0xFFF3F4F6), thickness = 1.dp)
                            MenuItem(
                                icon = Icons.Default.ReceiptLong,
                                iconColor = Color(0xFFF59E0B), // Yellow/Amber
                                title = stringResource(R.string.order_history_title),
                                onClick = onNavigateToOrderHistory
                            )
                            HorizontalDivider(modifier = Modifier.padding(start = 56.dp), color = Color(0xFFF3F4F6), thickness = 1.dp)
                            MenuItem(
                                icon = Icons.Default.ThumbUp,
                                iconColor = MaterialTheme.colorScheme.primary,
                                title = stringResource(R.string.me_must_try),
                                onClick = onNavigateToMustTry
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            item {
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column {
                            MenuItem(
                                icon = Icons.Default.ConfirmationNumber,
                                iconColor = MaterialTheme.colorScheme.primary,
                                title = stringResource(R.string.me_vouchers),
                                trailingText = stringResource(R.string.me_vouchers_count),
                                onClick = onNavigateToVouchers
                            )
                            HorizontalDivider(modifier = Modifier.padding(start = 56.dp), color = Color(0xFFF3F4F6), thickness = 1.dp)
                            MenuItem(
                                icon = Icons.Default.MonetizationOn,
                                iconColor = Color(0xFFF59E0B), // Yellow/Amber
                                title = stringResource(R.string.me_coins),
                                trailingText = stringResource(R.string.me_coins_count),
                                onClick = onNavigateToRewardPoints
                            )
                            HorizontalDivider(modifier = Modifier.padding(start = 56.dp), color = Color(0xFFF3F4F6), thickness = 1.dp)
                            MenuItem(
                                icon = Icons.Default.AccountBalanceWallet,
                                iconColor = Color(0xFF3B82F6), // Blue
                                title = "Ví FoodiePay",
                                trailingText = "Nạp tiền/Lịch sử",
                                onClick = onNavigateToWallet
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
            
            item {
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column {
                            MenuItem(
                                icon = Icons.Default.LocationOn,
                                iconColor = Color(0xFF10B981), // Green
                                title = stringResource(R.string.me_address),
                                onClick = onNavigateToAddress
                            )
                            HorizontalDivider(modifier = Modifier.padding(start = 56.dp), color = Color(0xFFF3F4F6), thickness = 1.dp)
                            MenuItem(
                                icon = Icons.Default.PersonAdd,
                                iconColor = MaterialTheme.colorScheme.primary,
                                title = stringResource(R.string.me_invite),
                                onClick = onNavigateToInviteFriends
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
            
            item {
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column {
                            MenuItem(
                                icon = Icons.Default.Storefront,
                                iconColor = Color(0xFFF59E0B), // Yellow/Amber
                                title = stringResource(R.string.me_shop_owners),
                                onClick = onNavigateToShopOwner
                            )
                            HorizontalDivider(modifier = Modifier.padding(start = 56.dp), color = Color(0xFFF3F4F6), thickness = 1.dp)
                            MenuItem(
                                icon = Icons.Default.HelpOutline,
                                iconColor = Color(0xFF10B981), // Green
                                title = stringResource(R.string.me_help),
                                onClick = onNavigateToHelpCentre
                            )
                            HorizontalDivider(modifier = Modifier.padding(start = 56.dp), color = Color(0xFFF3F4F6), thickness = 1.dp)
                            MenuItem(
                                icon = Icons.Default.Settings,
                                iconColor = Color(0xFF3B82F6), // Blue
                                title = stringResource(R.string.me_settings),
                                onClick = onNavigateToSettings
                            )
                            HorizontalDivider(modifier = Modifier.padding(start = 56.dp), color = Color(0xFFF3F4F6), thickness = 1.dp)
                            MenuItem(
                                icon = Icons.Default.Lock,
                                iconColor = Color.Gray,
                                title = "Đổi mật khẩu",
                                onClick = onNavigateToChangePassword
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
            
            item {
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        MenuItem(
                            icon = Icons.AutoMirrored.Filled.ExitToApp,
                            iconColor = Color.Red,
                            title = "Đăng xuất",
                            onClick = onLoggedOut
                        )
                    }
                }
            }
            
            item {
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}
}

@Composable
private fun QuickStatsRow(
    balance: String,
    rewardPoints: String,
    onNavigateToWallet: () -> Unit,
    onNavigateToRewardPoints: () -> Unit,
    onNavigateToVouchers: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .offset(y = (-20).dp), // Kéo Row lên đè lên header một chút
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        QuickStatCard(
            modifier = Modifier.weight(1f),
            title = "Ví FoodiePay",
            value = balance,
            icon = Icons.Default.AccountBalanceWallet,
            iconColor = Color(0xFF3B82F6), // Blue
            onClick = onNavigateToWallet
        )
        QuickStatCard(
            modifier = Modifier.weight(1f),
            title = "FoodieCoins",
            value = rewardPoints,
            icon = Icons.Default.MonetizationOn,
            iconColor = Color(0xFFF59E0B), // Yellow/Amber
            onClick = onNavigateToRewardPoints
        )
        QuickStatCard(
            modifier = Modifier.weight(1f),
            title = "Voucher",
            value = "3 mã", // Hardcode tạm
            icon = Icons.Default.ConfirmationNumber,
            iconColor = Color(0xFF10B981), // Green
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
    Card(
        modifier = modifier.clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(28.dp))
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Text(title, color = Color.Gray, fontSize = 11.sp)
        }
    }
}

@Composable
private fun ProfileHeader(fullName: String, email: String, onNavigateToEditProfile: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.primary)
            .padding(top = 40.dp, bottom = 44.dp, start = 16.dp, end = 16.dp) // padding bottom nhiều hơn để chừa chỗ cho QuickStatsRow
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            AsyncImage(
                model = "https://ui-avatars.com/api/?name=${fullName.replace(" ", "+")}&background=random&size=200",
                contentDescription = "Avatar",
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(Color.White),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = fullName,
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = email,
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 14.sp
                )
            }
            IconButton(onClick = onNavigateToEditProfile) {
                Icon(Icons.Default.Edit, contentDescription = "Edit Profile", tint = Color.White)
            }
        }
    }
}

@Composable
private fun MenuItem(
    icon: ImageVector,
    iconColor: Color,
    title: String,
    trailingText: String? = null,
    onClick: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val actualOnClick = onClick ?: {
        Toast.makeText(context, "Tính năng đang được phát triển", Toast.LENGTH_SHORT).show()
    }
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = actualOnClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Text(title, fontSize = 16.sp, modifier = Modifier.weight(1f))
        if (trailingText != null) {
            Text(trailingText, color = Color.Gray, fontSize = 14.sp)
            Spacer(modifier = Modifier.width(8.dp))
        }
        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.LightGray)
    }
}
