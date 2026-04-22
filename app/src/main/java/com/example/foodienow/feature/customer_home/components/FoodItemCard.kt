package com.example.foodienow.feature.customer_home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddShoppingCart
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.example.foodienow.core.designsystem.theme.ColorPrimaryDark
import com.example.foodienow.core.designsystem.theme.ColorSurfaceLight
import com.example.foodienow.domain.model.Food

@Composable
fun FoodItemCard(
    food: Food,
    onCardClick: (Food) -> Unit,
    // Tạm thời dùng biến trạng thái nội bộ để xử lý giao diện tăng giảm
    // Trong thực tế, số lượng này nên được lấy từ ViewModel quản lý giỏ hàng
) {
    var quantity by remember { mutableIntStateOf(0) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCardClick(food) }, // Bấm vào toàn bộ thẻ để xem chi tiết
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = food.imageUrl,
                contentDescription = null,
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(text = food.name, fontWeight = FontWeight.Bold, color = Color.Black)
                Spacer(modifier = Modifier.height(4.dp))
                // Đã xóa phần mô tả món ăn ở đây
                Text(text = "${food.price} VNĐ", color = ColorPrimaryDark, fontWeight = FontWeight.Bold)
            }

            // Giao diện nút Thêm vào giỏ / Chỉnh số lượng
            if (quantity == 0) {
                IconButton(
                    onClick = { quantity = 1 },
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = ColorPrimaryDark, // Dùng nền đậm để nút rõ nét hơn
                        contentColor = Color.White
                    )
                ) {
                    Icon(Icons.Default.AddShoppingCart, contentDescription = "Thêm vào giỏ")
                }
            } else {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .background(ColorSurfaceLight, RoundedCornerShape(50))
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    IconButton(
                        onClick = { quantity-- },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Default.Remove, contentDescription = "Giảm", tint = ColorPrimaryDark)
                    }

                    Text(
                        text = quantity.toString(),
                        fontWeight = FontWeight.Bold,
                        color = ColorPrimaryDark,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )

                    IconButton(
                        onClick = { quantity++ },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Tăng", tint = ColorPrimaryDark)
                    }
                }
            }
        }
    }
}