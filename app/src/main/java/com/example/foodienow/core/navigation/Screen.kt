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
    object PaymentResult : Screen("payment_result_screen/{orderId}/{amount}/{methodLabel}") {
        fun createRoute(orderId: String, amount: Long, methodLabel: String) =
            "payment_result_screen/$orderId/$amount/${android.net.Uri.encode(methodLabel)}"
    }
    object Profile : Screen("profile_screen")
    object Notifications : Screen("notifications_screen")
    object ActivityHistory : Screen("activity_history_screen")
    object OrderHistory : Screen("order_history_screen")
    object Address : Screen("address_screen")
    object PaymentSettings : Screen("payment_settings_screen")
    object Wallet : Screen("wallet_screen")
    object MustTry : Screen("must_try_screen")
    object Vouchers : Screen("vouchers_screen")
    object RewardPoints : Screen("reward_points_screen")
    object InviteFriends : Screen("invite_friends_screen")
    object ShopOwner : Screen("shop_owner_screen")
    object HelpCentre : Screen("help_centre_screen")
    object Settings : Screen("settings_screen")
    object FoodDetail : Screen("food_detail/{foodId}") {
        fun createRoute(foodId: String) = "food_detail/$foodId"
    }

    // Merchant
    object MerchantHome : Screen("merchant_home_screen")
    object AddEditFood : Screen("add_edit_food/{foodId}") {
        fun createRoute(foodId: String = "new") = "add_edit_food/$foodId"
    }

    // Shipper
    object ShipperHome : Screen("shipper_home_screen?tab={tab}") {
        fun createRoute(tab: Int = 0): String = "shipper_home_screen?tab=$tab"
    }
    object ShipperEarnings : Screen("shipper_earnings_screen")

    // Admin
    object AdminDashboard : Screen("admin_dashboard_screen")
}
