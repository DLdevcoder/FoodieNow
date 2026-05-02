package com.example.foodienow.feature.profile

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.foodienow.R

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
    profileViewModel: ProfileViewModel = hiltViewModel()
) {
    val profileUiState by profileViewModel.uiState.collectAsState()
    val fullName = profileUiState.profile?.fullName ?: "thanhhai107"

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
    ) {
        item {
            ProfileHeader(fullName = fullName)
        }
        
        item {
            Spacer(modifier = Modifier.height(12.dp))
            Column(modifier = Modifier.background(Color.White)) {
                MenuItem(
                    icon = Icons.Default.ThumbUp, // Approximate icon
                    iconColor = MaterialTheme.colorScheme.primary,
                    title = stringResource(R.string.me_must_try),
                    onClick = onNavigateToMustTry
                )
                HorizontalDivider(modifier = Modifier.padding(start = 56.dp), color = Color.LightGray, thickness = 0.5.dp)
                MenuItem(
                    icon = Icons.Default.ConfirmationNumber,
                    iconColor = MaterialTheme.colorScheme.primary,
                    title = stringResource(R.string.me_vouchers),
                    trailingText = stringResource(R.string.me_vouchers_count),
                    onClick = onNavigateToVouchers
                )
                HorizontalDivider(modifier = Modifier.padding(start = 56.dp), color = Color.LightGray, thickness = 0.5.dp)
                MenuItem(
                    icon = Icons.Default.MonetizationOn,
                    iconColor = Color(0xFFF59E0B), // Yellow/Amber
                    title = stringResource(R.string.me_coins),
                    trailingText = stringResource(R.string.me_coins_count),
                    onClick = onNavigateToRewardPoints
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(12.dp))
            Column(modifier = Modifier.background(Color.White)) {
                MenuItem(
                    icon = Icons.Default.AccountBalanceWallet,
                    iconColor = Color(0xFF3B82F6), // Blue
                    title = stringResource(R.string.me_payment),
                    onClick = onNavigateToPaymentSettings
                )
                HorizontalDivider(modifier = Modifier.padding(start = 56.dp), color = Color.LightGray, thickness = 0.5.dp)
                MenuItem(
                    icon = Icons.Default.LocationOn,
                    iconColor = Color(0xFF14B8A6), // Teal
                    title = stringResource(R.string.me_address),
                    onClick = onNavigateToAddress
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(12.dp))
            Column(modifier = Modifier.background(Color.White)) {
                MenuItem(
                    icon = Icons.Default.Email,
                    iconColor = Color(0xFF3B82F6), // Blue
                    title = stringResource(R.string.me_invite),
                    onClick = onNavigateToInviteFriends
                )
                HorizontalDivider(modifier = Modifier.padding(start = 56.dp), color = Color.LightGray, thickness = 0.5.dp)
                MenuItem(
                    icon = Icons.Default.Storefront,
                    iconColor = Color(0xFFF59E0B), // Yellow/Amber
                    title = stringResource(R.string.me_shop_owners),
                    onClick = onNavigateToShopOwner
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(12.dp))
            Column(modifier = Modifier.background(Color.White)) {
                MenuItem(
                    icon = Icons.Default.HelpOutline,
                    iconColor = Color(0xFF10B981), // Green
                    title = stringResource(R.string.me_help),
                    onClick = onNavigateToHelpCentre
                )
                HorizontalDivider(modifier = Modifier.padding(start = 56.dp), color = Color.LightGray, thickness = 0.5.dp)
                MenuItem(
                    icon = Icons.Default.Settings,
                    iconColor = Color(0xFF3B82F6), // Blue
                    title = stringResource(R.string.me_settings),
                    onClick = onNavigateToSettings
                )
            }
        }
        
        item {
            Spacer(modifier = Modifier.height(12.dp))
            Column(modifier = Modifier.background(Color.White)) {
                MenuItem(
                    icon = Icons.Default.ExitToApp,
                    iconColor = Color.Red,
                    title = "Đăng xuất",
                    onClick = onLoggedOut
                )
            }
        }
        
        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun ProfileHeader(fullName: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.primary)
            .padding(top = 40.dp, bottom = 24.dp, start = 16.dp, end = 16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
                    .background(Color.LightGray),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Person, contentDescription = null, tint = Color.White, modifier = Modifier.size(40.dp))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = fullName,
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium
            )
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
