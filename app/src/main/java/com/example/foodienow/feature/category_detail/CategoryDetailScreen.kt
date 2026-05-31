package com.example.foodienow.feature.category_detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.foodienow.R
import com.example.foodienow.core.designsystem.components.FoodieEmptyState
import com.example.foodienow.core.designsystem.components.FoodieErrorState
import com.example.foodienow.core.designsystem.components.FoodieLoadingState
import com.example.foodienow.core.designsystem.components.FoodieTopAppBar
import com.example.foodienow.core.designsystem.theme.FoodieCream
import com.example.foodienow.domain.model.Food
import com.example.foodienow.feature.customer_home.FoodCard

@Composable
fun CategoryDetailScreen(
    viewModel: CategoryDetailViewModel = hiltViewModel(),
    onBack: () -> Unit,
    onNavigateToFoodDetail: (Food) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            FoodieTopAppBar(
                title = uiState.categoryName,
                onBack = onBack
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(FoodieCream)
        ) {
            when {
                uiState.isLoading -> {
                    FoodieLoadingState(modifier = Modifier.align(Alignment.Center))
                }

                uiState.errorMessage != null -> {
                    FoodieErrorState(
                        title = stringResource(R.string.category_detail_error_title),
                        subtitle = uiState.errorMessage.orEmpty(),
                        actionLabel = stringResource(R.string.category_detail_error_retry),
                        onAction = viewModel::refresh,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                uiState.foods.isEmpty() -> {
                    FoodieEmptyState(
                        title = stringResource(R.string.category_detail_empty_title),
                        subtitle = stringResource(R.string.category_detail_empty_subtitle),
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                else -> {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        contentPadding = PaddingValues(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(uiState.foods, key = { it.id.ifBlank { it.name } }) { food ->
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
}