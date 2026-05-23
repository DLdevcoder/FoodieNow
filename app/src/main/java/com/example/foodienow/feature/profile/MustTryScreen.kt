package com.example.foodienow.feature.profile

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
fun MustTryScreen(
    onBack: () -> Unit,
    onNavigateToFoodDetail: (Food) -> Unit,
    viewModel: CustomerHomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            FoodieTopAppBar(
                title = "Gợi ý món ngon",
                onBack = onBack
            )
        }
    ) { padding ->
        when {
            uiState.isLoading && uiState.recommendedFoods.isEmpty() -> {
                FoodieLoadingState(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    label = "Đang tìm món hợp khẩu vị..."
                )
            }

            uiState.errorMessage != null && uiState.recommendedFoods.isEmpty() -> {
                FoodieErrorState(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    title = "Không thể tải gợi ý",
                    subtitle = uiState.errorMessage.orEmpty(),
                    actionLabel = "Thử lại",
                    onAction = viewModel::refresh
                )
            }

            uiState.recommendedFoods.isEmpty() -> {
                FoodieEmptyState(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    title = "Chưa có gợi ý món ngon",
                    subtitle = "Các món được đánh giá cao sẽ xuất hiện tại đây."
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
                        uiState.recommendedFoods.sortedWith(
                            compareByDescending<Food> { it.rating }.thenByDescending { it.soldCount }
                        ),
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
