package com.example.foodienow.core.navigation

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.foodienow.R
import com.example.foodienow.domain.model.User
import com.example.foodienow.domain.model.UserRole
import com.example.foodienow.feature.activity.ActivityHistoryScreen
import com.example.foodienow.feature.auth.AuthViewModel
import com.example.foodienow.feature.auth.ForgotPasswordScreen
import com.example.foodienow.feature.auth.LoginScreen
import com.example.foodienow.feature.auth.RegisterScreen
import com.example.foodienow.feature.auth.VerifyAccountScreen
import com.example.foodienow.feature.cart.CartScreen
import com.example.foodienow.feature.cart.CartViewModel
import com.example.foodienow.feature.customer_home.CustomerHomeScreen
import com.example.foodienow.feature.food_detail.FoodDetailScreen
import com.example.foodienow.feature.food_detail.FoodDetailViewModel
import com.example.foodienow.feature.food_detail.FoodReviewsScreen
import com.example.foodienow.feature.merchant.AddEditFoodScreen
import com.example.foodienow.feature.merchant.MerchantHomeScreen
import com.example.foodienow.feature.notification.NotificationScreen
import com.example.foodienow.feature.order_history.OrderHistoryScreen
import com.example.foodienow.feature.payment.PaymentScreen
import com.example.foodienow.feature.profile.ProfileScreen
import com.example.foodienow.feature.main.CustomerMainScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Screen.AuthGate.route) {
        composable(route = Screen.AuthGate.route) {
            AuthGateScreen(
                onResolved = { user ->
                    navController.navigate(user?.homeRoute() ?: Screen.Login.route) {
                        popUpTo(Screen.AuthGate.route) { inclusive = true }
                    }
                }
            )
        }

        composable(route = Screen.Login.route) {
            LoginScreen(
                onNavigateRegister = { navController.navigate(Screen.Register.route) },
                onNavigateForgotPassword = { navController.navigate(Screen.ForgotPassword.route) },
                onLoginSuccess = { user ->
                    navController.navigate(user.homeRoute()) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        composable(route = Screen.ForgotPassword.route) {
            ForgotPasswordScreen(onBackToLogin = { navController.popBackStack() })
        }

        composable(route = Screen.Register.route) {
            RegisterScreen(
                onBackToLogin = { navController.popBackStack() },
                onRegisterSuccess = { email ->
                    navController.navigate(Screen.VerifyAccount.createRoute(Uri.encode(email)))
                }
            )
        }

        composable(
            route = Screen.VerifyAccount.route,
            arguments = listOf(navArgument("email") { type = NavType.StringType })
        ) { backStackEntry ->
            val email = backStackEntry.arguments?.getString("email").orEmpty()
            VerifyAccountScreen(
                email = Uri.decode(email),
                onBackToLogin = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        composable(route = Screen.CustomerHome.route) {
            CustomerMainScreen(
                rootNavController = navController,
                onNavigateToCart = { navController.navigate(Screen.Cart.route) },
                onNavigateToFoodDetail = { food ->
                    navController.navigate("food_detail/${food.id}")
                },
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.CustomerHome.route) { inclusive = true }
                    }
                }
            )
        }

        composable(route = Screen.Cart.route) {
            // Thay PlaceholderScreen bằng CartScreen thực tế
            CartScreen(
                onBackClick = { navController.popBackStack() },
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route)
                },
                onNavigateToCheckout = { userId ->
                    navController.navigate(Screen.Payment.route)
                }
            )
        }

        composable(route = Screen.Payment.route) {
            PaymentScreen(
                onBack = { navController.popBackStack() },
                onNavigateToOrderHistory = {
                    navController.navigate(Screen.OrderHistory.route) {
                        popUpTo(Screen.CustomerHome.route) { inclusive = false }
                    }
                }
            )
        }

        composable(route = Screen.Profile.route) {
            ProfileScreen(
                onBack = { navController.popBackStack() },
                onNavigateToOrderHistory = { navController.navigate(Screen.OrderHistory.route) },
                onNavigateToActivityHistory = { navController.navigate(Screen.ActivityHistory.route) },
                onLoggedOut = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.CustomerHome.route) { inclusive = true }
                    }
                },
                onNavigateToWallet = { navController.navigate(Screen.Wallet.route) }
            )
        }

        composable(route = Screen.Address.route) {
            com.example.foodienow.feature.profile.AddressScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable(route = Screen.PaymentSettings.route) {
            com.example.foodienow.feature.profile.PaymentSettingsScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable(route = Screen.MustTry.route) {
            com.example.foodienow.feature.profile.MustTryScreen(
                onBack = { navController.popBackStack() },
                onNavigateToFoodDetail = { food ->
                    navController.navigate("food_detail/${food.id}")
                }
            )
        }

        composable(route = Screen.Vouchers.route) {
            com.example.foodienow.feature.profile.VoucherScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable(route = Screen.RewardPoints.route) {
            com.example.foodienow.feature.profile.RewardPointsScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable(route = Screen.InviteFriends.route) {
            com.example.foodienow.feature.profile.InviteFriendsScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable(route = Screen.ShopOwner.route) {
            com.example.foodienow.feature.profile.ShopOwnerScreen(
                onBack = { navController.popBackStack() },
                onNavigateToMerchantLogin = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(route = Screen.HelpCentre.route) {
            com.example.foodienow.feature.profile.HelpCentreScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable(route = Screen.Settings.route) {
            com.example.foodienow.feature.profile.SettingsScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable(route = Screen.Wallet.route) {
            com.example.foodienow.feature.profile.WalletScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = "food_detail/{foodId}",
            arguments = listOf(navArgument("foodId") { type = NavType.StringType })
        ) {
            val viewModel: FoodDetailViewModel = hiltViewModel()
            val uiState by viewModel.uiState.collectAsState()

            val cartViewModel: CartViewModel = hiltViewModel()

            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (uiState.error != null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = "Lỗi: ${uiState.error}", color = MaterialTheme.colorScheme.error)
                }
            } else if (uiState.food != null && uiState.store != null) {
                FoodDetailScreen(
                    food = uiState.food!!,
                    store = uiState.store!!,
                    reviews = uiState.reviews,
                    onBackClick = { navController.popBackStack() },
                    onAddToCart = { food, quantity ->
                        cartViewModel.addToCart(food, quantity)
                        navController.navigate(Screen.Cart.route)
                    },
                    onNavigateToStore = { /* TODO */ },
                    onNavigateToAllReviews = {
                        navController.navigate("food_reviews/${uiState.food!!.id}")
                    },
                    onSubmitProductReview = { rating, comment ->
                        viewModel.submitReview(rating, comment)
                    }
                )
            }
        }

        composable(
            route = "food_reviews/{foodId}",
            arguments = listOf(navArgument("foodId") { type = NavType.StringType })
        ) {
            val viewModel: FoodDetailViewModel = hiltViewModel()
            val uiState by viewModel.uiState.collectAsState()

            FoodReviewsScreen(
                reviews = uiState.reviews,
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(route = Screen.ActivityHistory.route) {
            ActivityHistoryScreen(onBack = { navController.popBackStack() })
        }

        composable(route = Screen.OrderHistory.route) {
            OrderHistoryScreen(onBack = { navController.popBackStack() })
        }

        composable(route = Screen.Notifications.route) {
            NotificationScreen(onBack = { navController.popBackStack() })
        }

        composable(route = Screen.ShipperHome.route) {
            com.example.foodienow.feature.shipper.ShipperHomeScreen(
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(route = Screen.MerchantHome.route) {
            MerchantHomeScreen(
                onNavigateToAddFood = { storeId ->
                    navController.navigate("add_edit_food/new?storeId=$storeId")
                },
                onNavigateToEditFood = { foodId ->
                    navController.navigate("add_edit_food/$foodId")
                },
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onNavigateToHistory = {
                    navController.navigate(Screen.ActivityHistory.route)
                },
                onNavigateToOrderHistory = {
                    navController.navigate(Screen.OrderHistory.route)
                }
            )
        }

        composable(
            route = "add_edit_food/{foodId}?storeId={storeId}",
            arguments = listOf(
                navArgument("foodId") { type = NavType.StringType },
                navArgument("storeId") { type = NavType.StringType; defaultValue = "" }
            )
        ) { backStackEntry ->
            val storeId = backStackEntry.arguments?.getString("storeId").orEmpty()
            AddEditFoodScreen(
                storeId = storeId,
                onBack = { navController.popBackStack() }
            )
        }
    }
}

@Composable
private fun AuthGateScreen(
    onResolved: (User?) -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    var isResolving by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        val storedUser = viewModel.resolveStoredSession()
        onResolved(storedUser)
        isResolving = false
    }

    if (isResolving) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator()
            Text(text = stringResource(R.string.auth_checking_session))
        }
    }
}

private fun User.homeRoute(): String {
    return when (role) {
        UserRole.CUSTOMER -> Screen.CustomerHome.route
        UserRole.MERCHANT -> Screen.MerchantHome.route
        UserRole.SHIPPER -> Screen.ShipperHome.route
    }
}

@Composable
private fun PlaceholderScreen(title: String, onBack: (() -> Unit)? = null) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = title, style = MaterialTheme.typography.headlineMedium)
        if (onBack != null) {
            Button(onClick = onBack) {
                Text(stringResource(R.string.common_back))
            }
        }
    }
}