package com.example.foodienow.feature.merchant

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.example.foodienow.core.designsystem.theme.ColorPrimaryDark
import com.example.foodienow.domain.model.Food
import com.example.foodienow.feature.customer_home.components.formatPrice

@Composable
fun MerchantMenuTab(
    uiState: MerchantUiState,
    onToggleAvailability: (Food) -> Unit,
    onAddFoodClick: () -> Unit,
    onEditFoodClick: (Food) -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        if (uiState.isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        } else if (uiState.error != null) {
            Text(
                text = "Lỗi: ${uiState.error}",
                color = Color.Red,
                modifier = Modifier.align(Alignment.Center)
            )
        } else if (uiState.menu.isEmpty()) {
            Text(
                text = "Chưa có món ăn nào.\nHãy bấm nút + để thêm món.",
                color = Color.Gray,
                modifier = Modifier.align(Alignment.Center),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(uiState.menu) { food ->
                    MerchantFoodItem(
                        food = food,
                        onToggle = { onToggleAvailability(food) },
                        onEdit = { onEditFoodClick(food) }
                    )
                }
                item { Spacer(modifier = Modifier.height(80.dp)) }
            }
        }

        FloatingActionButton(
            onClick = onAddFoodClick,
            containerColor = ColorPrimaryDark,
            contentColor = Color.White,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Thêm món")
        }
    }
}

@Composable
fun MerchantFoodItem(
    food: Food,
    onToggle: () -> Unit,
    onEdit: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = food.imageUrl,
                contentDescription = null,
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.LightGray),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(text = food.name, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Text(food.price.formatPrice(), color = ColorPrimaryDark, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (food.isAvailable) "Đang bán" else "Tạm hết",
                        fontSize = 11.sp,
                        color = if (food.isAvailable) Color(0xFF4CAF50) else Color.Red
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Switch(
                        checked = food.isAvailable,
                        onCheckedChange = { onToggle() },
                        modifier = Modifier.scale(0.6f)
                    )
                }
            }

            IconButton(onClick = onEdit, modifier = Modifier.size(36.dp)) {
                Icon(Icons.Default.Edit, contentDescription = "Sửa", tint = Color.Gray, modifier = Modifier.size(20.dp))
            }
        }
    }
}

private fun Modifier.scale(scale: Float) = this.then(Modifier.graphicsLayer(scaleX = scale, scaleY = scale))