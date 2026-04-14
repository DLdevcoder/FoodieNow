package com.example.foodienow.core.navigation

sealed class Screen(val route: String) {
    // Chung
    object Login : Screen("login_screen")

    // Customer
    object CustomerHome : Screen("customer_home_screen")
    object Cart : Screen("cart_screen")

    // Merchant
    object MerchantHome : Screen("merchant_home_screen")

    // Shipper
    object ShipperHome : Screen("shipper_home_screen")
}