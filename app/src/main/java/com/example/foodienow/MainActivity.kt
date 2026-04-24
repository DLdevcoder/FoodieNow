package com.example.foodienow

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.foodienow.domain.model.Food
import com.example.foodienow.feature.customer_home.CustomerHomeScreen
import com.example.foodienow.feature.customer_home.FoodDetailScreen
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation()
                }
            }
        }
    }
}

@Composable
fun AppNavigation() {
    var currentScreen by remember { mutableStateOf("home") }
    var foodToDetail by remember { mutableStateOf<Food?>(null) }

    Crossfade(targetState = currentScreen, label = "MainNavigation") { screen ->
        when (screen) {
            "home" -> {
                CustomerHomeScreen(
                    onNavigateToCart = {
                        // TODO: Chuyển sang màn hình giỏ hàng tổng
                    },
                    onNavigateToFoodDetail = { food ->
                        foodToDetail = food
                        currentScreen = "detail"
                    }
                )
            }
            "detail" -> {
                foodToDetail?.let { food ->
                    FoodDetailScreen(
                        food = food,
                        onBackClick = {
                            // Quay lại màn hình chính
                            currentScreen = "home"
                        },
                        onNavigateToCart = {
                            // TODO: Mở giỏ hàng từ trang chi tiết
                        }
                    )
                }
            }
        }
    }
}