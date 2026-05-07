package com.example.foodienow.feature.main

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.foodienow.core.designsystem.theme.ColorBackground
import com.example.foodienow.feature.merchant.MerchantMenuTab
import com.example.foodienow.feature.merchant.MerchantOrdersTab
import com.example.foodienow.feature.merchant.MerchantStoreTab
import com.example.foodienow.feature.merchant.MerchantViewModel
import com.example.foodienow.feature.notification.NotificationScreen
import com.example.foodienow.feature.profile.ProfileScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MerchantMainScreen(
    rootNavController: NavController,
    viewModel: MerchantViewModel = hiltViewModel(),
    onNavigateToAddFood: (String) -> Unit,
    onNavigateToEditFood: (String) -> Unit,
    onLogout: () -> Unit
) {
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            if (selectedTab in 0..2) {
                TopAppBar(
                    title = {
                        Text(
                            text = when (selectedTab) {
                                0 -> "Quản lý đơn hàng"
                                1 -> "Quản lý thực đơn"
                                else -> "Cửa hàng của tôi"
                            },
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        titleContentColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
            }
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                val tabs = listOf(
                    Triple(Icons.Default.List, "Đơn hàng", 0),
                    Triple(Icons.Default.MenuBook, "Thực đơn", 1),
                    Triple(Icons.Default.Store, "Cửa hàng", 2),
                    Triple(Icons.Default.Notifications, "Thông báo", 3),
                    Triple(Icons.Default.Person, "Hồ sơ", 4)
                )

                tabs.forEach { (icon, label, index) ->
                    NavigationBarItem(
                        icon = { Icon(icon, contentDescription = label) },
                        label = {
                            Text(
                                text = label,
                                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                                maxLines = 1
                            )
                        },
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
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
                    top = if (selectedTab in 3..4) 0.dp else paddingValues.calculateTopPadding(),
                    bottom = paddingValues.calculateBottomPadding()
                )
        ) {
            when (selectedTab) {
                0 -> MerchantOrdersTab()
                1 -> MerchantMenuTab(
                    uiState = uiState,
                    onToggleAvailability = { viewModel.toggleFoodAvailability(it) },
                    onAddFoodClick = { uiState.store?.let { onNavigateToAddFood(it.id) } },
                    onEditFoodClick = { food -> onNavigateToEditFood(food.id) }
                )
                2 -> MerchantStoreTab(
                    uiState = uiState,
                    onUpdateStore = { newName, newAddress, newOpeningTime, newClosingTime, newIsActive, imageBytes ->
                        viewModel.updateStoreInfo(
                            newName,
                            newAddress,
                            newOpeningTime,
                            newClosingTime,
                            newIsActive,
                            imageBytes
                        )
                    }
                )
                3 -> NotificationScreen(onBack = { selectedTab = 0 })
                4 -> ProfileScreen(
                    onBack = { selectedTab = 0 },
                    onNavigateToOrderHistory = { rootNavController.navigate("order_history_screen") },
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