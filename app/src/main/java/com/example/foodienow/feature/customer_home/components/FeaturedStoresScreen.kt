package com.example.foodienow.feature.customer_home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.foodienow.R
import com.example.foodienow.core.designsystem.components.FoodImage
import com.example.foodienow.core.designsystem.components.FoodieCard
import com.example.foodienow.core.designsystem.components.FoodieEmptyState
import com.example.foodienow.core.designsystem.components.FoodieErrorState
import com.example.foodienow.core.designsystem.components.FoodieLoadingState
import com.example.foodienow.core.designsystem.components.FoodieTopAppBar
import com.example.foodienow.core.designsystem.components.RatingBadge
import com.example.foodienow.core.designsystem.theme.FoodieCream
import com.example.foodienow.core.designsystem.theme.FoodieRating
import com.example.foodienow.domain.model.Store
import com.example.foodienow.feature.customer_home.CustomerHomeViewModel

@Composable
fun FeaturedStoresScreen(
    onBack: () -> Unit,
    onStoreClick: (Store) -> Unit,
    viewModel: CustomerHomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            FoodieTopAppBar(
                title = "Quán nổi bật gần bạn",
                onBack = onBack
            )
        }
    ) { padding ->
        when {
            uiState.isLoading && uiState.featuredStores.isEmpty() -> {
                FoodieLoadingState(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    label = "Đang tìm quán nổi bật..."
                )
            }

            uiState.errorMessage != null && uiState.featuredStores.isEmpty() -> {
                FoodieErrorState(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    title = "Không thể tải danh sách quán",
                    subtitle = uiState.errorMessage.orEmpty(),
                    actionLabel = "Thử lại",
                    onAction = viewModel::refresh
                )
            }

            uiState.featuredStores.isEmpty() -> {
                FoodieEmptyState(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    title = "Chưa có quán nổi bật nào gần đây",
                    subtitle = "Quán ăn quanh vị trí của bạn sẽ xuất hiện tại đây."
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
                        uiState.featuredStores,
                        key = { it.id.orEmpty() }
                    ) { store ->
                        StoreItemCard(
                            store = store,
                            onClick = { onStoreClick(store) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StoreItemCard(
    store: Store,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    FoodieCard(
        modifier = modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Box {
                FoodImage(
                    imageUrl = store.imageUrl,
                    contentDescription = store.name,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                )

                if (store.rating != null && store.rating > 0) {
                    RatingBadge(
                        rating = store.rating,
                        reviewCount = store.reviewCount,
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(8.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = store.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            if (!store.address.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = store.address,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AccessTime,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "20-30 phút",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = FoodieRating,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = String.format("%.1f", store.rating ?: 0.0),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
