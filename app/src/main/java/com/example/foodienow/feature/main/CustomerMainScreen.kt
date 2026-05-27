package com.example.foodienow.feature.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Alignment
import androidx.navigation.NavController
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.foodienow.R
import com.example.foodienow.domain.model.Food
import com.example.foodienow.domain.model.OrderStatus
import com.example.foodienow.feature.customer_home.CustomerHomeScreen
import com.example.foodienow.feature.notification.NotificationScreen
import com.example.foodienow.feature.notification.NotificationViewModel
import com.example.foodienow.feature.order_history.OrderHistoryScreen
import com.example.foodienow.feature.order_history.OrderHistoryViewModel
import com.example.foodienow.feature.profile.ProfileScreen
import com.example.foodienow.core.navigation.Screen

@Composable
fun CustomerMainScreen(
    rootNavController: NavController,
    onNavigateToCart: () -> Unit,
    onNavigateToFoodDetail: (Food) -> Unit,
    onNavigateToCategory: (String, String) -> Unit,
    onNavigateToChatList: () -> Unit,
    onLogout: () -> Unit,
    notificationViewModel: NotificationViewModel = hiltViewModel(),
    orderHistoryViewModel: OrderHistoryViewModel = hiltViewModel()
) {
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    var orderHistoryInitialTab by rememberSaveable { mutableIntStateOf(0) }

    var hasViewedOrders by remember { mutableStateOf(selectedTab == 1) }
    var hasViewedNotifications by remember { mutableStateOf(selectedTab == 2) }

    val notificationState by notificationViewModel.uiState.collectAsState()
    val orderHistoryState by orderHistoryViewModel.uiState.collectAsState()

    val activeStatuses = setOf(
        OrderStatus.PENDING,
        OrderStatus.PREPARING,
        OrderStatus.DRIVER_ASSIGNED,
        OrderStatus.DELIVERING
    )

    var lastOrdersMap by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    val currentOrdersMap = orderHistoryState.orders
        .filter { it.status in activeStatuses }
        .associate { (it.id ?: "") to it.status.name }

    LaunchedEffect(currentOrdersMap) {
        val hasChanges = currentOrdersMap.any { (id, status) ->
            lastOrdersMap[id] != status
        }
        if (hasChanges) {
            hasViewedOrders = false
        }
        lastOrdersMap = currentOrdersMap
    }

    var lastUnreadCount by remember { mutableIntStateOf(0) }
    LaunchedEffect(notificationState.unreadCount) {
        if (notificationState.unreadCount > lastUnreadCount) {
            hasViewedNotifications = false
        }
        lastUnreadCount = notificationState.unreadCount
    }

    Scaffold(
        bottomBar = {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 8.dp,
                tonalElevation = 3.dp,
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
            ) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 0.dp,
                    modifier = Modifier.height(72.dp + WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding())
                ) {
                    val tabs = listOf(
                        Triple(Icons.Default.Home, R.string.bottom_nav_home, 0),
                        Triple(Icons.Default.ListAlt, R.string.bottom_nav_orders, 1),
                        Triple(Icons.Default.Notifications, R.string.bottom_nav_notifications, 2),
                        Triple(Icons.Default.Person, R.string.bottom_nav_me, 3)
                    )

                    tabs.forEach { (icon, labelRes, index) ->
                        val selected = selectedTab == index
                        val showBadge = when (index) {
                            1 -> currentOrdersMap.isNotEmpty() && !hasViewedOrders && selectedTab != 1
                            2 -> notificationState.unreadCount > 0 && !hasViewedNotifications && selectedTab != 2
                            else -> false
                        }
                        NavigationBarItem(
                            icon = {
                                Box {
                                    Icon(icon, contentDescription = stringResource(labelRes))
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
                                    text = stringResource(labelRes),
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                                )
                            },
                            selected = selected,
                            onClick = {
                                selectedTab = index
                                if (index == 1) {
                                    orderHistoryInitialTab = 0
                                    hasViewedOrders = true
                                }
                                if (index == 2) {
                                    hasViewedNotifications = true
                                }
                            },
                            alwaysShowLabel = true,
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = paddingValues.calculateBottomPadding())
        ) {
            when (selectedTab) {
                0 -> CustomerHomeScreen(
                    onNavigateToFoodDetail = onNavigateToFoodDetail,
                    onNavigateToSearch = { rootNavController.navigate("search_screen") },
                    onNavigateToCategory = onNavigateToCategory,
                    onNavigateToCart = onNavigateToCart,
                    onNavigateToChatList = onNavigateToChatList
                )
                1 -> OrderHistoryScreen(
                    onBack = { selectedTab = 0 },
                    onNavigateToOrderDetail = { orderId ->
                        rootNavController.navigate("order_detail/$orderId")
                    },
                    onNavigateToTracking = { orderId ->
                        rootNavController.navigate("customer_tracking/$orderId")
                    },
                    onNavigateToCart = { rootNavController.navigate(Screen.Payment.route) },
                    onNavigateToFoodDetail = onNavigateToFoodDetail,
                    initialTab = orderHistoryInitialTab,
                    viewModel = orderHistoryViewModel
                )
                2 -> NotificationScreen(
                    onBack = { selectedTab = 0 },
                    viewModel = notificationViewModel
                )
                3 -> ProfileScreen(
                    onBack = { selectedTab = 0 },
                    onNavigateToOrderHistory = {
                        orderHistoryInitialTab = 2
                        selectedTab = 1
                    },
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
