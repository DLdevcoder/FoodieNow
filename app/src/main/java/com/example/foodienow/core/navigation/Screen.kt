package com.example.foodienow.core.navigation

sealed class Screen(val route: String) {
    // Chung
    object AuthGate : Screen("auth_gate_screen")
    object Login : Screen("login_screen")
    object Register : Screen("register_screen")
    object ForgotPassword : Screen("forgot_password_screen")
    object VerifyAccount : Screen("verify_account_screen/{email}") {
        fun createRoute(email: String): String = "verify_account_screen/$email"
    }

    // Customer
    object CustomerHome : Screen("customer_home_screen")
    object Cart : Screen("cart_screen")
    object Payment : Screen("payment_screen")
    object Profile : Screen("profile_screen")
    object Notifications : Screen("notifications_screen")
    object ActivityHistory : Screen("activity_history_screen")
    object FoodDetail : Screen("food_detail/{foodId}") {
        fun createRoute(foodId: String) = "food_detail/$foodId"
    }

    // Merchant
    object MerchantHome : Screen("merchant_home_screen")
    object AddEditFood : Screen("add_edit_food/{foodId}") {
        fun createRoute(foodId: String = "new") = "add_edit_food/$foodId"
    }

    // Shipper
    object ShipperHome : Screen("shipper_home_screen")
}