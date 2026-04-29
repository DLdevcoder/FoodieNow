package com.example.foodienow.feature.merchant

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.foodienow.core.designsystem.theme.ColorBackground
import com.example.foodienow.core.designsystem.theme.ColorPrimary
import com.example.foodienow.core.designsystem.theme.ColorPrimaryDark

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MerchantHomeScreen(
    viewModel: MerchantViewModel = hiltViewModel(),
    onNavigateToAddFood: (String) -> Unit,
    onNavigateToEditFood: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) }


    Scaffold(
        topBar = {
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
        },
        bottomBar = {
            NavigationBar(containerColor = Color.White) {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.List, contentDescription = "Đơn hàng") },
                    label = { Text("Đơn hàng") },
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = ColorPrimaryDark,
                        selectedTextColor = ColorPrimaryDark
                    )
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.MenuBook, contentDescription = "Thực đơn") },
                    label = { Text("Thực đơn") },
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = ColorPrimaryDark,
                        selectedTextColor = ColorPrimaryDark
                    )
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Store, contentDescription = "Cửa hàng") },
                    label = { Text("Cửa hàng") },
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
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
                .padding(paddingValues)
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
