package com.example.foodienow.feature.customer_home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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
import com.example.foodienow.domain.model.ReviewUiModel
import com.example.foodienow.domain.model.Store
import com.example.foodienow.feature.customer_home.components.formatPrice

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FoodDetailScreen(
    food: Food,
    store: Store,
    reviews: List<ReviewUiModel>,
    onBackClick: () -> Unit,
    onNavigateToCart: () -> Unit,
    onNavigateToStore: (String) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Chi tiết món ăn", color = Color.White, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = ColorPrimary)
            )
        },
        containerColor = ColorBackground,
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .background(Color.White)
                    .navigationBarsPadding(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onNavigateToCart,
                    modifier = Modifier.weight(0.2f)
                ) {
                    Icon(Icons.Default.ShoppingCart, contentDescription = "Giỏ hàng", tint = ColorPrimaryDark)
                }

                // Nút Thêm vào giỏ
                Button(
                    onClick = { /* TODO */ },
                    colors = ButtonDefaults.buttonColors(containerColor = ColorSurfaceLight),
                    shape = RoundedCornerShape(0.dp),
                    modifier = Modifier.weight(0.4f).fillMaxHeight()
                ) {
                    Text("Thêm vào giỏ", color = ColorPrimaryDark, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = { /* TODO: Mở màn hình thanh toán luôn */ },
                    colors = ButtonDefaults.buttonColors(containerColor = ColorPrimaryDark),
                    shape = RoundedCornerShape(0.dp),
                    modifier = Modifier.weight(0.4f).fillMaxHeight()
                ) {
                    Text("Mua ngay", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            AsyncImage(
                model = food.imageUrl,
                contentDescription = null,
                modifier = Modifier.fillMaxWidth().height(280.dp),
                contentScale = ContentScale.Crop
            )

            // Khối Thông tin chính
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(16.dp)
            ) {
                Text(
                    text = food.price.formatPrice(),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = ColorPrimaryDark
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = food.name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )

                // Rating & Đã bán (Dữ liệu tĩnh tạm thời theo mẫu)
                Row(
                    modifier = Modifier.padding(top = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Star, contentDescription = "Sao", tint = Color(0xFFFFD700), modifier = Modifier.size(20.dp))
                    Text(text = " 4.8", fontWeight = FontWeight.Bold, modifier = Modifier.padding(end = 8.dp))
                    Text(text = "|", color = Color.LightGray, modifier = Modifier.padding(end = 8.dp))
                    Text(text = "Đã bán 1.2k", color = Color.Gray, fontSize = 14.sp)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Khối Thông tin Cửa hàng
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .clickable { onNavigateToStore(store.id) }
                    .padding(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    AsyncImage(
                        model = store.imageUrl,
                        contentDescription = null,
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(ColorSurfaceLight),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = store.name,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(top = 4.dp)
                        ) {
                            Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFD700), modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            if (store.reviewCount > 0) {
                                Text(
                                    text = "${String.format("%.1f", store.rating)} (${if (store.reviewCount > 999) "999+" else store.reviewCount} đánh giá)",
                                    fontSize = 12.sp,
                                    color = Color.DarkGray
                                )
                            } else {
                                Text(text = "Chưa có đánh giá", fontSize = 12.sp, color = Color.Gray)
                            }
                        }
                    }
                    Icon(Icons.Default.ChevronRight, contentDescription = "Xem quán", tint = Color.Gray)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Khối Chi tiết món ăn
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(16.dp)
            ) {
                Text(text = "Chi tiết món ăn", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = food.description ?: "Chủ quán chưa cập nhật mô tả cho món ăn này.",
                    color = Color.DarkGray,
                    lineHeight = 22.sp
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Khối Đánh giá
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Đánh giá sản phẩm", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    Text(text = "Xem tất cả >", color = ColorPrimary, fontSize = 14.sp)
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (reviews.isEmpty()) {
                    Text("Chưa có đánh giá nào cho món ăn này.", color = Color.Gray, fontSize = 14.sp)
                } else {
                    reviews.take(3).forEach { review ->
                        ReviewItem(review = review)
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun ReviewItem(review: ReviewUiModel) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (!review.userAvatarUrl.isNullOrEmpty()) {
                AsyncImage(
                    model = review.userAvatarUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(ColorSurfaceLight),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = review.userName.firstOrNull()?.toString()?.uppercase() ?: "U",
                        color = ColorPrimaryDark,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Column {
                Text(text = review.userName, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    repeat(5) { index ->
                        Icon(
                            Icons.Default.Star,
                            contentDescription = null,
                            tint = if (index < review.rating) Color(0xFFFFD700) else Color.LightGray,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = review.date, fontSize = 12.sp, color = Color.Gray)
                }
            }
        }
        Text(text = review.comment, modifier = Modifier.padding(top = 8.dp), fontSize = 14.sp, color = Color.DarkGray)
    }
}