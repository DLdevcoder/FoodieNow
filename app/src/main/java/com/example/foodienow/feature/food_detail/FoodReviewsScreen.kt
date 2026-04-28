package com.example.foodienow.feature.food_detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.foodienow.core.designsystem.theme.ColorBackground
import com.example.foodienow.core.designsystem.theme.ColorPrimary
import com.example.foodienow.domain.model.ReviewUiModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FoodReviewsScreen(
    reviews: List<ReviewUiModel>,
    onBackClick: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tất cả đánh giá", color = Color.White, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = ColorPrimary)
            )
        },
        containerColor = ColorBackground
    ) { paddingValues ->
        if (reviews.isEmpty()) {
            Text(
                text = "Chưa có đánh giá nào.",
                modifier = Modifier.padding(paddingValues).padding(16.dp),
                color = Color.Gray
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(paddingValues).background(Color.White),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(reviews) { review ->
                    ReviewItem(review = review)
                    Divider(color = Color.LightGray, modifier = Modifier.padding(top = 16.dp))
                }
            }
        }
    }
}