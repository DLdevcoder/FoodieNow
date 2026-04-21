package com.example.foodienow

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.foodienow.feature.customer_home.CustomerHomeScreen
import dagger.hilt.android.AndroidEntryPoint

// BẮT BUỘC PHẢI CÓ DÒNG NÀY ĐỂ HILT HOẠT ĐỘNG
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // Bao bọc ứng dụng trong Theme mặc định
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    CustomerHomeScreen(
                        onNavigateToCart = {
                            // Tạm thời để trống, sau này xử lý chuyển trang sau
                        }
                    )
                }
            }
        }
    }
}