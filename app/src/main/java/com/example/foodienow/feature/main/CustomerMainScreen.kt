package com.example.foodienow.feature.main

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import com.example.foodienow.domain.model.Food
import com.example.foodienow.feature.customer_home.CustomerHomeScreen
import com.example.foodienow.feature.notification.NotificationScreen
import com.example.foodienow.feature.order_history.OrderHistoryScreen
import com.example.foodienow.feature.profile.ProfileScreen

@Composable
fun CustomerMainScreen(
    rootNavController: NavController,
    onNavigateToCart: () -> Unit,
    onNavigateToFoodDetail: (Food) -> Unit,
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
                    Triple(Icons.Default.Home, R.string.bottom_nav_home, 0),
                    Triple(Icons.Default.ListAlt, R.string.bottom_nav_orders, 1),
                    Triple(Icons.Default.Notifications, R.string.bottom_nav_notifications, 2),
                    Triple(Icons.Default.Person, R.string.bottom_nav_me, 3)
                )

                tabs.forEach { (icon, labelRes, index) ->
                    NavigationBarItem(
                        icon = { Icon(icon, contentDescription = stringResource(labelRes)) },
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        alwaysShowLabel = false,
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.surface, // No pill background
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
                0 -> CustomerHomeScreen(
                    onNavigateToFoodDetail = onNavigateToFoodDetail
                )
                1 -> OrderHistoryScreen(
                    onBack = { selectedTab = 0 } // Or handle back differently
                )
                2 -> NotificationScreen(
                    onBack = { selectedTab = 0 }
                )
                3 -> ProfileScreen(
                    onBack = { selectedTab = 0 },
                    onNavigateToOrderHistory = { selectedTab = 1 },
                    onNavigateToActivityHistory = { selectedTab = 1 },
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
                    onNavigateToChangePassword = { rootNavController.navigate("change_password_screen") }
                )
            }
        }
    }
}
