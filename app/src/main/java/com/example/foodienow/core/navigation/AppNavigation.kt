package com.example.foodienow.core.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    // màn hình Login
    NavHost(navController = navController, startDestination = Screen.Login.route) {

        // 1. Màn hình Auth
        composable(route = Screen.Login.route) {
            // LoginScreen()
            // Khi login xong, kiểm tra Role để chuyển: navController.navigate(Screen.CustomerHome.route)
        }

        // 2. Màn hình Customer
        composable(route = Screen.CustomerHome.route) {
            // CustomerHomeScreen()
        }

        composable(route = Screen.Cart.route) {
            // CartScreen()
        }

        // 3. Màn hình Merchant
        composable(route = Screen.MerchantHome.route) {
            // MerchantHomeScreen()
        }
    }
}