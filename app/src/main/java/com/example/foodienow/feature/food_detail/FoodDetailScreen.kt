package com.example.foodienow.feature.food_detail

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.example.foodienow.R
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
    onAddToCart: (Food, Int) -> Unit,
    onNavigateToStore: (String) -> Unit,
    onNavigateToAllReviews: () -> Unit,
    onNavigateToChat: (storeId: String, receiverId: String, storeName: String) -> Unit
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    var quantity by remember { mutableStateOf(1) }
    var isFavorite by remember { mutableStateOf(false) }

    val showCollapsedTitle = scrollState.value > 360

    Scaffold(
        topBar = {
            Surface(
                color = if (showCollapsedTitle) MaterialTheme.colorScheme.surface else Color.Transparent,
                tonalElevation = if (showCollapsedTitle) 4.dp else 0.dp,
                modifier = Modifier.statusBarsPadding()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onBackClick,
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = if (showCollapsedTitle) Color.Transparent else Color.Black.copy(alpha = 0.4f),
                            contentColor = if (showCollapsedTitle) MaterialTheme.colorScheme.onSurface else Color.White
                        ),
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null)
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    AnimatedVisibility(
                        visible = showCollapsedTitle,
                        enter = fadeIn(),
                        exit = fadeOut(),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = food.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    if (!showCollapsedTitle) {
                        Spacer(modifier = Modifier.weight(1f))
                    }

                    IconButton(
                        onClick = { isFavorite = !isFavorite },
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = if (showCollapsedTitle) Color.Transparent else Color.Black.copy(alpha = 0.4f),
                            contentColor = if (isFavorite) Color(0xFFFF5252) else (if (showCollapsedTitle) MaterialTheme.colorScheme.onSurface else Color.White)
                        ),
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = null
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    IconButton(
                        onClick = {
                            Toast.makeText(context, "Đã chia sẻ liên kết món ăn", Toast.LENGTH_SHORT).show()
                        },
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = if (showCollapsedTitle) Color.Transparent else Color.Black.copy(alpha = 0.4f),
                            contentColor = if (showCollapsedTitle) MaterialTheme.colorScheme.onSurface else Color.White
                        ),
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null)
                    }
                }
            }
        },
        bottomBar = {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp,
                modifier = Modifier
                    .shadow(elevation = 16.dp, spotColor = Color.Black.copy(alpha = 0.12f))
                    .navigationBarsPadding()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .background(Color(0xFFF3F4F6), RoundedCornerShape(24.dp))
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    ) {
                        IconButton(
                            onClick = { if (quantity > 1) quantity-- },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Text("-", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)
                        }
                        Text(
                            text = quantity.toString(),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 12.dp)
                        )
                        IconButton(
                            onClick = { quantity++ },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Text("+", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)
                        }
                    }

                    Button(
                        onClick = {
                            onAddToCart(food, quantity)
                            Toast.makeText(context, "Đã thêm vào giỏ hàng", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                        contentPadding = PaddingValues(),
                        shape = RoundedCornerShape(28.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp)
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(Color(0xFFFF7E36), Color(0xFFFF5252))
                                ),
                                shape = RoundedCornerShape(28.dp)
                            )
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Icon(Icons.Default.ShoppingBag, contentDescription = null, tint = Color.White)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Thêm vào giỏ - ${(food.price * quantity).formatPrice()}",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                    }
                }
            }
        },
        containerColor = Color(0xFFF9FAFB)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(320.dp)
            ) {
                AsyncImage(
                    model = food.imageUrl,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Black.copy(alpha = 0.3f), Color.Transparent, Color.Black.copy(alpha = 0.1f))
                            )
                        )
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .offset(y = (-20).dp)
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (food.soldCount >= 1000) {
                                Box(
                                    modifier = Modifier
                                        .background(Color(0xFFFFF1E8), RoundedCornerShape(4.dp))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "Bán chạy nhất",
                                        color = MaterialTheme.colorScheme.primary,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            Box(
                                modifier = Modifier
                                    .background(Color(0xFFE8F5E9), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                Text(
                                    text = "Đảm bảo chất lượng",
                                    color = Color(0xFF2E7D32),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = food.name,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = food.price.formatPrice(),
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.primary
                            )

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .background(Color(0xFFFFF9C4), RoundedCornerShape(12.dp))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = null,
                                        tint = Color(0xFFFBC02D),
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (food.rating > 0) String.format("%.1f", food.rating) else "Mới",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF5D4037)
                                    )
                                }

                                Text(
                                    text = "Đã bán ${food.soldCount}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.Gray,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AsyncImage(
                            model = store.imageUrl,
                            contentDescription = null,
                            modifier = Modifier
                                .size(54.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFF3F4F6))
                                .border(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f), CircleShape),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = store.name,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(top = 2.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = null,
                                    tint = Color(0xFFFFD700),
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(2.dp))
                                Text(
                                    text = "${store.rating} • Quán nổi bật",
                                    fontSize = 12.sp,
                                    color = Color.Gray
                                )
                            }
                        }
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedButton(
                                onClick = { onNavigateToStore(store.id) },
                                shape = RoundedCornerShape(18.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                modifier = Modifier.height(34.dp)
                            ) {
                                Text("Ghé quán", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }

                            IconButton(
                                onClick = {
                                    onNavigateToChat(store.id, store.ownerId, store.name)
                                },
                                modifier = Modifier
                                    .size(34.dp)
                                    .background(Color(0xFFFFF1E8), CircleShape)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Chat,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Mô tả món ăn",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = food.description ?: "Chủ quán chưa cập nhật mô tả cụ thể cho món ăn thơm ngon này.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF4B5563),
                            lineHeight = 22.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Đánh giá từ khách hàng",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            if (reviews.isNotEmpty()) {
                                Text(
                                    text = "Xem tất cả (${reviews.size})",
                                    color = MaterialTheme.colorScheme.primary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.clickable { onNavigateToAllReviews() }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        if (reviews.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Món ăn chưa có đánh giá nào. Hãy là người đầu tiên thưởng thức và chia sẻ cảm nhận nhé!",
                                    fontSize = 13.sp,
                                    color = Color.Gray,
                                    lineHeight = 18.sp
                                )
                            }
                        } else {
                            val averageRating = reviews.map { it.rating }.average()
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFFF9FAFB), RoundedCornerShape(12.dp))
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.padding(end = 24.dp)
                                ) {
                                    Text(
                                        text = String.format("%.1f", averageRating),
                                        fontSize = 32.sp,
                                        fontWeight = FontWeight.Black,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                                    ) {
                                        repeat(5) {
                                            Icon(
                                                imageVector = Icons.Default.Star,
                                                contentDescription = null,
                                                tint = Color(0xFFFFD700),
                                                modifier = Modifier.size(12.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Độ hài lòng cao",
                                        fontSize = 10.sp,
                                        color = Color.Gray
                                    )
                                }

                                Column(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    repeat(5) { index ->
                                        val starCount = 5 - index
                                        val countOfStar = reviews.count { it.rating == starCount }
                                        val progress = if (reviews.isNotEmpty()) countOfStar.toFloat() / reviews.size else 0f
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Text(
                                                text = "$starCount sao",
                                                fontSize = 11.sp,
                                                color = Color.Gray,
                                                modifier = Modifier.width(36.dp)
                                            )
                                            LinearProgressIndicator(
                                                progress = { progress },
                                                color = MaterialTheme.colorScheme.primary,
                                                trackColor = Color(0xFFE5E7EB),
                                                strokeCap = StrokeCap.Round,
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .height(6.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            reviews.take(3).forEach { review ->
                                Column {
                                    ReviewItem(review = review)
                                    if (review != reviews.take(3).last()) {
                                        HorizontalDivider(
                                            color = Color(0xFFF3F4F6),
                                            modifier = Modifier.padding(vertical = 12.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ReviewItem(
    review: ReviewUiModel,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (!review.userAvatarUrl.isNullOrEmpty()) {
                AsyncImage(
                    model = review.userAvatarUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFFF1E8)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = review.userName.firstOrNull()?.toString()?.uppercase() ?: "U",
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = review.userName,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Box(
                        modifier = Modifier
                            .background(Color(0xFFE8F5E9), RoundedCornerShape(2.dp))
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "Đã mua",
                            color = Color(0xFF2E7D32),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(1.dp)
                    ) {
                        repeat(review.rating) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = Color(0xFFFFD700),
                                modifier = Modifier.size(10.dp)
                            )
                        }
                    }
                    Text(
                        text = review.date,
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                }
            }
        }

        if (review.comment.isNotEmpty()) {
            Text(
                text = review.comment,
                modifier = Modifier.padding(top = 8.dp, start = 46.dp),
                fontSize = 13.sp,
                color = Color(0xFF374151),
                lineHeight = 18.sp
            )
        }
    }
}