package com.example.foodienow.feature.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Alignment
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.foodienow.core.designsystem.theme.ColorBackground
import com.example.foodienow.domain.model.OrderStatus
import com.example.foodienow.feature.merchant.MerchantMenuTab
import com.example.foodienow.feature.merchant.MerchantOrdersTab
import com.example.foodienow.feature.merchant.MerchantOrdersViewModel
import com.example.foodienow.feature.merchant.MerchantEarningsTab
import com.example.foodienow.feature.merchant.MerchantViewModel
import com.example.foodienow.feature.notification.NotificationScreen
import com.example.foodienow.feature.notification.NotificationViewModel
import com.example.foodienow.feature.profile.ProfileScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MerchantMainScreen(
    rootNavController: NavController,
    viewModel: MerchantViewModel = hiltViewModel(),
    merchantOrdersViewModel: MerchantOrdersViewModel = hiltViewModel(),
    notificationViewModel: NotificationViewModel = hiltViewModel(),
    onNavigateToAddFood: (String) -> Unit,
    onNavigateToEditFood: (String) -> Unit,
    onNavigateToChatList: () -> Unit,
    onLogout: () -> Unit
) {
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    val uiState by viewModel.uiState.collectAsState()

    val ordersState by merchantOrdersViewModel.uiState.collectAsState()
    val notificationState by notificationViewModel.uiState.collectAsState()

    var hasViewedOrders by remember { mutableStateOf(selectedTab == 0) }
    var hasViewedNotifications by remember { mutableStateOf(selectedTab == 3) }

    var lastPendingOrdersMap by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    val currentPendingOrdersMap = ordersState.orders
        .filter { it.status == OrderStatus.WAITING_STORE_CONFIRMATION }
        .associate { (it.id ?: "") to it.status.name }

    LaunchedEffect(currentPendingOrdersMap) {
        val hasChanges = currentPendingOrdersMap.any { (id, status) ->
            lastPendingOrdersMap[id] != status
        }
        if (hasChanges) {
            hasViewedOrders = false
        }
        lastPendingOrdersMap = currentPendingOrdersMap
    }

    var lastUnreadCount by remember { mutableIntStateOf(0) }
    LaunchedEffect(notificationState.unreadCount) {
        if (notificationState.unreadCount > lastUnreadCount) {
            hasViewedNotifications = false
        }
        lastUnreadCount = notificationState.unreadCount
    }

    LaunchedEffect(Unit) {
        viewModel.loadUnreadMessageCount()
    }

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                val tabs = listOf(
                    Triple(Icons.Default.List, "Đơn hàng", 0),
                    Triple(Icons.Default.MenuBook, "Thực đơn", 1),
                    Triple(Icons.Default.AccountBalanceWallet, "Thu nhập", 2),
                    Triple(Icons.Default.Notifications, "Thông báo", 3),
                    Triple(Icons.Default.Person, "Hồ sơ", 4)
                )

                tabs.forEach { (icon, label, index) ->
                    val showBadge = when (index) {
                        0 -> currentPendingOrdersMap.isNotEmpty() && !hasViewedOrders && selectedTab != 0
                        3 -> notificationState.unreadCount > 0 && !hasViewedNotifications && selectedTab != 3
                        else -> false
                    }
                    NavigationBarItem(
                        icon = {
                            Box {
                                Icon(icon, contentDescription = label)
                                if (showBadge) {
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .offset(x = 5.dp, y = (-3).dp)
                                            .size(7.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.error)
                                    )
                                }
                            }
                        },
                        label = {
                            Text(
                                text = label,
                                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                                maxLines = 1
                            )
                        },
                        selected = selectedTab == index,
                        onClick = {
                            selectedTab = index
                            if (index == 0) {
                                hasViewedOrders = true
                            }
                            if (index == 3) {
                                hasViewedNotifications = true
                            }
                        },
                        alwaysShowLabel = false,
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.surface,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            }
        },
        containerColor = ColorBackground
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    top = 0.dp,
                    bottom = paddingValues.calculateBottomPadding()
                )
        ) {
            when (selectedTab) {
                0 -> MerchantOrdersTab(
                    onNavigateToChatList = onNavigateToChatList,
                    unreadMessageCount = uiState.unreadMessageCount,
                    viewModel = merchantOrdersViewModel
                )
                1 -> MerchantMenuTab(
                    uiState = uiState,
                    onToggleAvailability = { viewModel.toggleFoodAvailability(it) },
                    onAddFoodClick = { uiState.store?.let { onNavigateToAddFood(it.id) } },
                    onEditFoodClick = { food -> onNavigateToEditFood(food.id) },
                    onCreateVoucher = { code, percent, amount, minVal, maxDis, active, expiry ->
                        viewModel.createVoucher(code, percent, amount, minVal, maxDis, active, expiry)
                    },
                    onUpdateVoucher = { id, code, percent, amount, minVal, maxDis, active, expiry ->
                        viewModel.updateVoucher(id, code, percent, amount, minVal, maxDis, active, expiry)
                    },
                    onDeleteVoucher = { id ->
                        viewModel.deleteVoucher(id)
                    }
                )
                2 -> MerchantEarningsTab(
                    onNavigateToPaymentSettings = { rootNavController.navigate("payment_settings_screen") }
                )
                3 -> NotificationScreen(
                    onBack = { selectedTab = 0 },
                    viewModel = notificationViewModel
                )
                4 -> ProfileScreen(
                    onBack = { selectedTab = 0 },
                    onNavigateToOrderHistory = { rootNavController.navigate("order_history_screen?tab=2") },
                    onNavigateToActivityHistory = { rootNavController.navigate("activity_history_screen") },
                    onLoggedOut = onLogout,
                    onNavigateToAddress = { rootNavController.navigate("address_screen") },
                    onNavigateToPaymentSettings = { rootNavController.navigate("payment_settings_screen") },
                    onNavigateToMustTry = { rootNavController.navigate("must_try_screen") },
                    onNavigateToVouchers = { rootNavController.navigate("vouchers_screen") },
                    onNavigateToRewardPoints = { rootNavController.navigate("reward_points_screen") },
                    onNavigateToInviteFriends = { rootNavController.navigate("invite_friends_screen") },
                    onNavigateToShopOwner = { rootNavController.navigate("shop_owner_screen") },
                    onNavigateToHelpCentre = { rootNavController.navigate("help_centre_screen") },
                    onNavigateToSettings = { rootNavController.navigate("settings_screen") },
                    onNavigateToEditProfile = { rootNavController.navigate("edit_profile_screen") },
                    onNavigateToChangePassword = { rootNavController.navigate("change_password_screen") },
                    onNavigateToWallet = { rootNavController.navigate("wallet_screen") }
                )
            }
        }
    }
}