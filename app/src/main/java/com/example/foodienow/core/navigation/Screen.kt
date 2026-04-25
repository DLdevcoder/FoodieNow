package com.example.foodienow.core.navigation

sealed class Screen(val route: String) {
    // Chung
    object AuthGate : Screen("auth_gate_screen")
    object Login : Screen("login_screen")
    object Register : Screen("register_screen")
    object VerifyAccount : Screen("verify_account_screen/{email}") {
        fun createRoute(email: String): String = "verify_account_screen/$email"
    }

    // Customer
    object CustomerHome : Screen("customer_home_screen")
    object Cart : Screen("cart_screen")

    // Merchant
    object MerchantHome : Screen("merchant_home_screen")

    // Shipper
    object ShipperHome : Screen("shipper_home_screen")
}