package com.example.foodienow.feature.customer_home.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.example.foodienow.core.designsystem.theme.ColorBackground
import com.example.foodienow.core.designsystem.theme.ColorPrimary
import com.example.foodienow.core.designsystem.theme.ColorPrimaryDark
import com.example.foodienow.core.designsystem.theme.ColorSurfaceLight
import com.example.foodienow.domain.model.Food


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FoodDetailBottomSheet(
    food: Food,
    onDismiss: () -> Unit,
    onAddToCart: (Food, Int) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var quantity by remember { mutableIntStateOf(1) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = ColorBackground,
        dragHandle = { BottomSheetDefaults.DragHandle(color = ColorPrimary) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 32.dp)
        ) {
            AsyncImage(
                model = food.imageUrl,
                contentDescription = "Ảnh món ${food.name}",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .clip(RoundedCornerShape(16.dp)),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = food.name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "${food.price} đ",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = ColorPrimaryDark
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = food.description ?: "Chưa có mô tả cho món ăn này.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.DarkGray
            )

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { if (quantity > 1) quantity-- },
                    colors = IconButtonDefaults.iconButtonColors(containerColor = ColorSurfaceLight)
                ) {
                    Icon(Icons.Default.Remove, contentDescription = "Giảm", tint = ColorPrimaryDark)
                }

                Text(
                    text = quantity.toString(),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 24.dp)
                )

                IconButton(
                    onClick = { quantity++ },
                    colors = IconButtonDefaults.iconButtonColors(containerColor = ColorSurfaceLight)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Tăng", tint = ColorPrimaryDark)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    onAddToCart(food, quantity)
                    onDismiss()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ColorPrimary),
                shape = RoundedCornerShape(12.dp)
            ) {
                // Tính tổng tiền tự động
                val totalPrice = food.price * quantity
                Text(
                    text = "Thêm $quantity vào giỏ - $totalPrice đ",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}