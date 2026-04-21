package com.example.foodienow.feature.customer_home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.foodienow.core.designsystem.theme.ColorBackground
import com.example.foodienow.core.designsystem.theme.ColorPrimary
import com.example.foodienow.core.designsystem.theme.ColorPrimaryDark
import com.example.foodienow.core.designsystem.theme.ColorSurfaceLight
import com.example.foodienow.feature.customer_home.components.FoodItemCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerHomeScreen(
    viewModel: CustomerHomeViewModel = hiltViewModel(),
    onNavigateToCart: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Hôm nay ăn gì?",
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = ColorPrimary
                )
            )
        },
        containerColor = ColorBackground
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Thanh tìm kiếm
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = viewModel::onSearchQueryChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White, RoundedCornerShape(12.dp)),
                placeholder = { Text("Tìm kiếm món ăn, quán ăn...", color = Color.Gray) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = ColorPrimary) },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ColorPrimaryDark,
                    unfocusedBorderColor = ColorSurfaceLight
                )
            )

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Gợi ý",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = ColorPrimaryDark,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // Danh sách món ăn
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.fillMaxWidth().wrapContentWidth(),
                    color = ColorPrimaryDark
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(uiState.recommendedFoods) { food ->
                        FoodItemCard(
                            food = food,
                            onAddToCartClick = { selectedFood ->
                                println("Đã thêm ${selectedFood.name} vào giỏ")
                            }
                        )
                    }
                    item { Spacer(modifier = Modifier.height(16.dp)) }
                }
            }
        }
    }
}