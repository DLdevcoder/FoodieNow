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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.foodienow.domain.model.User
import com.example.foodienow.domain.model.UserRole
import com.example.foodienow.feature.auth.AuthViewModel
import com.example.foodienow.feature.auth.LoginScreen
import com.example.foodienow.feature.auth.RegisterScreen
import com.example.foodienow.feature.auth.VerifyAccountScreen
import com.example.foodienow.feature.customer_home.CustomerHomeScreen

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
                onLoginSuccess = { user ->
                    navController.navigate(user.homeRoute()) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
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
            CustomerHomeScreen(
                onNavigateToCart = { navController.navigate(Screen.Cart.route) },
                onNavigateToFoodDetail = { }
            )
        }

        composable(route = Screen.Cart.route) {
            PlaceholderScreen(title = "Gio hang", onBack = { navController.popBackStack() })
        }

        composable(route = Screen.MerchantHome.route) {
            PlaceholderScreen(title = "Trang Merchant")
        }

        composable(route = Screen.ShipperHome.route) {
            PlaceholderScreen(title = "Trang Shipper")
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
            Text(text = "Dang kiem tra phien dang nhap...")
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
                Text("Quay lai")
            }
        }
    }
}