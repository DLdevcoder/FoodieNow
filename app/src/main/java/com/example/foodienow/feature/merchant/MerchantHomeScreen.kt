package com.example.foodienow.feature.merchant

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.foodienow.core.designsystem.theme.ColorBackground
import com.example.foodienow.core.designsystem.theme.ColorPrimary
import com.example.foodienow.core.designsystem.theme.ColorPrimaryDark
import com.example.foodienow.feature.notification.NotificationScreen
import com.example.foodienow.feature.profile.ProfileScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MerchantHomeScreen(
    viewModel: MerchantViewModel = hiltViewModel(),
    onNavigateToAddFood: (String) -> Unit,
    onNavigateToEditFood: (String) -> Unit,
    onLogout: () -> Unit,
    onNavigateToHistory: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) }

    Scaffold(
        topBar = {
            // Ẩn TopAppBar mặc định đi nếu tab đó (như Profile, Notification) đã tự có TopAppBar riêng
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
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = ColorPrimary)
                )
            }
        },
        bottomBar = {
            NavigationBar(containerColor = Color.White) {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.List, contentDescription = "Đơn hàng") },
                    label = { Text("Đơn hàng", maxLines = 1) },
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = ColorPrimaryDark,
                        selectedTextColor = ColorPrimaryDark
                    )
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.MenuBook, contentDescription = "Thực đơn") },
                    label = { Text("Thực đơn", maxLines = 1) },
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = ColorPrimaryDark,
                        selectedTextColor = ColorPrimaryDark
                    )
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Store, contentDescription = "Cửa hàng") },
                    label = { Text("Cửa hàng", maxLines = 1) },
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = ColorPrimaryDark,
                        selectedTextColor = ColorPrimaryDark
                    )
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Notifications, contentDescription = "Thông báo") },
                    label = { Text("Thông báo", maxLines = 1) },
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = ColorPrimaryDark,
                        selectedTextColor = ColorPrimaryDark
                    )
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Person, contentDescription = "Hồ sơ") },
                    label = { Text("Hồ sơ", maxLines = 1) },
                    selected = selectedTab == 4,
                    onClick = { selectedTab = 4 },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = ColorPrimaryDark,
                        selectedTextColor = ColorPrimaryDark
                    )
                )
            }
        },
        containerColor = ColorBackground
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    // Nếu là tab Thông báo hoặc Hồ sơ, không dùng padding trên vì chúng đã có TopAppBar riêng
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
                        viewModel.updateStoreInfo(newName, newAddress, newOpeningTime, newClosingTime, newIsActive, imageBytes)
                    }
                )
                3 -> NotificationScreen(
                    onBack = { selectedTab = 0 } // Bấm back sẽ về lại màn hình Đơn hàng
                )
                4 -> ProfileScreen(
                    onBack = { selectedTab = 0 }, // Bấm back sẽ về lại màn hình Đơn hàng
                    onNavigateToHistory = onNavigateToHistory,
                    onLoggedOut = onLogout
                )
            }
        }
    }
}

@Composable
fun MerchantOrdersTab() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Giao diện danh sách đơn hàng")
    }
}