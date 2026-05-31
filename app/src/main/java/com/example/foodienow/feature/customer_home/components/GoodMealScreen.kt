package com.example.foodienow.feature.customer_home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.foodienow.core.designsystem.components.FoodieEmptyState
import com.example.foodienow.core.designsystem.components.FoodieErrorState
import com.example.foodienow.core.designsystem.components.FoodieLoadingState
import com.example.foodienow.core.designsystem.components.FoodieTopAppBar
import com.example.foodienow.core.designsystem.theme.FoodieCream
import com.example.foodienow.domain.model.Food
import com.example.foodienow.feature.customer_home.CustomerHomeViewModel
import com.example.foodienow.feature.customer_home.FoodCard

@Composable
fun GoodMealScreen(
    onBack: () -> Unit,
    onNavigateToFoodDetail: (Food) -> Unit,
    viewModel: CustomerHomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            FoodieTopAppBar(
                title = "Bữa ngon giá tốt",
                onBack = onBack
            )
        }
    ) { padding ->
        when {
            uiState.isLoading && uiState.nearbyFoods.isEmpty() -> {
                FoodieLoadingState(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    label = "Đang tìm món ngon giá tốt..."
                )
            }

            uiState.errorMessage != null && uiState.nearbyFoods.isEmpty() -> {
                FoodieErrorState(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    title = "Không thể tải món ngon",
                    subtitle = uiState.errorMessage.orEmpty(),
                    actionLabel = "Thử lại",
                    onAction = viewModel::refresh
                )
            }

            uiState.nearbyFoods.isEmpty() -> {
                FoodieEmptyState(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    title = "Chưa có món ngon giá tốt",
                    subtitle = "Các món bán chạy sẽ xuất hiện tại đây."
                )
            }

            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .background(FoodieCream),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(
                        uiState.nearbyFoods.sortedByDescending { it.soldCount },
                        key = { it.id.ifBlank { it.name } }
                    ) { food ->
                        FoodCard(
                            food = food,
                            onClick = onNavigateToFoodDetail
                        )
                    }
                }
            }
        }
    }
}
