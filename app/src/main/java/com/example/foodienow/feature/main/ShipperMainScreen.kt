package com.example.foodienow.feature.main

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.DeliveryDining
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.foodienow.R
import com.example.foodienow.feature.order_history.OrderHistoryScreen
import com.example.foodienow.feature.shipper.ShipperEarningsScreen
import com.example.foodienow.feature.shipper.ShipperHomeScreen

@Composable
fun ShipperMainScreen(
    rootNavController: NavController,
    onLogout: () -> Unit
) {
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                val tabs = listOf(
                    Triple(Icons.Default.Home, R.string.shipper_nav_home, 0),
                    Triple(Icons.Default.ListAlt, R.string.shipper_nav_orders, 1),
                    Triple(Icons.Default.AccountBalanceWallet, R.string.shipper_nav_earnings, 2),
                    Triple(Icons.Default.Person, R.string.shipper_nav_profile, 3)
                )

                tabs.forEach { (icon, labelRes, index) ->
                    NavigationBarItem(
                        icon = { Icon(icon, contentDescription = stringResource(labelRes)) },
                        label = {
                            Text(
                                text = stringResource(labelRes),
                                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal
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
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (selectedTab) {
                0 -> ShipperHomeScreen(onLogout = onLogout)
                1 -> OrderHistoryScreen(onBack = { selectedTab = 0 })
                2 -> ShipperEarningsScreen(onBack = { selectedTab = 0 })
                3 -> {
                    Text("Profile Screen - Coming Soon")
                }
            }
        }
    }
}