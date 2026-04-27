package com.example.foodienow.feature.food_detail

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
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
    onAddToCart: (Food, Int) -> Unit, // Đã thêm callback thêm vào giỏ
    onNavigateToStore: (String) -> Unit,
    onNavigateToAllReviews: () -> Unit, // Callback mở trang Xem tất cả
    onSubmitProductReview: (Int, String) -> Unit // Callback gửi đánh giá món ăn
) {
    var showProductReviewDialog by remember { mutableStateOf(false) }

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
            Surface(
                color = Color.White,
                shadowElevation = 16.dp,
                modifier = Modifier.navigationBarsPadding()
            ) {
                // Chỉ hiển thị nút Thêm vào giỏ
                Button(
                    onClick = { onAddToCart(food, 1) }, // Mặc định thêm 1 phần
                    colors = ButtonDefaults.buttonColors(containerColor = ColorPrimaryDark),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .height(48.dp)
                ) {
                    Text("Thêm vào giỏ", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
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

            // Khối Thông tin món ăn
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

                Row(
                    modifier = Modifier.padding(top = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${if (food.rating > 0) String.format("%.1f", food.rating) else "0.0"} ☆",
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFFD700),
                        fontSize = 16.sp
                    )
                    Text(text = " | ", color = Color.LightGray, modifier = Modifier.padding(horizontal = 8.dp))
                    Text(
                        text = "Đã bán ${if (food.soldCount > 999) "999+" else food.soldCount}",
                        color = Color.Gray,
                        fontSize = 14.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Khối Thông tin Cửa hàng
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    AsyncImage(
                        model = store.imageUrl,
                        contentDescription = null,
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(ColorSurfaceLight)
                            .clickable { onNavigateToStore(store.id) },
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f).clickable { onNavigateToStore(store.id) }) {
                        Text(
                            text = store.name,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(text = "Xem cửa hàng", fontSize = 12.sp, color = Color.Gray)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Khối Chi tiết
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

            // Khối Đánh giá sản phẩm
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
                    Text(
                        text = "Xem tất cả >",
                        color = ColorPrimary,
                        fontSize = 14.sp,
                        modifier = Modifier.clickable { onNavigateToAllReviews() }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Nút Viết đánh giá món ăn
                OutlinedButton(
                    onClick = { showProductReviewDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Viết đánh giá cho món này", color = ColorPrimaryDark)
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (reviews.isEmpty()) {
                    Text("Chưa có đánh giá nào.", color = Color.Gray, fontSize = 14.sp)
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

    // Dialog đánh giá Món ăn
    if (showProductReviewDialog) {
        RatingDialog(
            title = "Đánh giá món ăn",
            showCommentField = true,
            onDismiss = { showProductReviewDialog = false },
            onSubmit = { rating, comment ->
                onSubmitProductReview(rating, comment)
                showProductReviewDialog = false
            }
        )
    }
}

@Composable
fun RatingDialog(
    title: String,
    showCommentField: Boolean,
    onDismiss: () -> Unit,
    onSubmit: (Int, String) -> Unit
) {
    var selectedRating by remember { mutableIntStateOf(0) }
    var comment by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = title, fontWeight = FontWeight.Bold, modifier = Modifier.fillMaxWidth()) },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Row(horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()) {
                    for (i in 1..5) {
                        Icon(
                            imageVector = if (i <= selectedRating) Icons.Filled.Star else Icons.Outlined.Star,
                            contentDescription = "Star $i",
                            tint = if (i <= selectedRating) Color(0xFFFFD700) else Color.LightGray,
                            modifier = Modifier
                                .size(40.dp)
                                .clickable { selectedRating = i }
                                .padding(4.dp)
                        )
                    }
                }

                if (showCommentField) {
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = comment,
                        onValueChange = { comment = it },
                        placeholder = { Text("Bạn thấy món này thế nào?") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSubmit(selectedRating, comment) },
                enabled = selectedRating > 0,
                colors = ButtonDefaults.buttonColors(containerColor = ColorPrimaryDark)
            ) {
                Text("Gửi", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Hủy", color = Color.Gray)
            }
        }
    )
}

@Composable
fun ReviewItem(review: ReviewUiModel) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (!review.userAvatarUrl.isNullOrEmpty()) {
                AsyncImage(
                    model = review.userAvatarUrl,
                    contentDescription = null,
                    modifier = Modifier.size(32.dp).clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier.size(32.dp).clip(CircleShape).background(ColorSurfaceLight),
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
                    Text(text = "${review.rating} ☆", color = Color(0xFFFFD700), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = review.date, fontSize = 12.sp, color = Color.Gray)
                }
            }
        }
        if (review.comment.isNotEmpty()) {
            Text(text = review.comment, modifier = Modifier.padding(top = 8.dp), fontSize = 14.sp, color = Color.DarkGray)
        }
    }
}