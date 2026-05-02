package com.example.foodienow.feature.notification

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.foodienow.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationScreen(
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
    ) {
        TopAppBar(
            title = {
                Text(
                    stringResource(R.string.notifications_tab_title),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Medium
                )
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {
            item {
                Column(modifier = Modifier.background(Color.White)) {
                    NotificationHeaderItem(
                        icon = Icons.Default.Campaign,
                        title = stringResource(R.string.notifications_news),
                        subtitle = stringResource(R.string.notifications_news_desc)
                    )
                    HorizontalDivider(modifier = Modifier.padding(start = 64.dp), color = Color.LightGray)
                    NotificationHeaderItem(
                        icon = Icons.Default.ReceiptLong,
                        title = stringResource(R.string.notifications_order_updates),
                        subtitle = stringResource(R.string.notifications_order_updates_desc)
                    )
                }
            }

            item {
                Text(
                    stringResource(R.string.notifications_promotions),
                    modifier = Modifier.padding(16.dp),
                    color = Color.Gray,
                    fontSize = 14.sp
                )
            }

            items(5) { index ->
                PromotionItem(index)
            }
        }
    }
}

@Composable
private fun NotificationHeaderItem(icon: ImageVector, title: String, subtitle: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(Color.White, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontSize = 16.sp, fontWeight = FontWeight.Medium)
            Text(subtitle, fontSize = 14.sp, color = Color.Gray)
        }
        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.Gray)
    }
}

@Composable
private fun PromotionItem(index: Int) {
    val titles = listOf(
        "Bao bạn FREESHIP, ăn sáng nha!",
        "GIẢM 99.000Đ, lễ to deal giảm to!",
        "Khao MÓN GIẢM 30.000Đ",
        "Ăn trưa ngày lễ GIẢM 50.000Đ",
        "Giảm 50% tất cả món ăn"
    )
    val descs = listOf(
        "⚡Khi nhập mã 55SPFFSOD 🍜Bánh canh cua, nui thịt bằm🍜Hủ tiếu nam vang... Đặt ngay!",
        "👉Khi nhập mã HOLIDAY99 🍗Gà Nướng & Gà Bó Xôi Thành Công 🍕Pizza & Spaghetti Pizza Huu🍲Tiệm Lẩu Nhà An... Mở tiệc thôi!",
        "👉Khi nhập mã SPFMOI30K 🍮Rau câu bánh flan 🍹Trà trái cây nhiệt đới 🥥Nước dừa tươi... Ăn xế thôi!",
        "👉Khi nhập mã T6GIAM18 🍜Nui xào bò, hamburger, bún Thái... 🎺Lễ giảm thả ga, đặt ngay nha!",
        "Nhập mã DEALSOCK để được giảm giá ngay hôm nay."
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFFFF5F5)) // Slight pinkish background
            .padding(16.dp)
    ) {
        Row {
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .background(Color.LightGray, RoundedCornerShape(8.dp))
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    titles.getOrElse(index) { "Promotion Title" },
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    descs.getOrElse(index) { "Promotion Description" },
                    fontSize = 14.sp,
                    color = Color.DarkGray,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "02/05/2026 08:56",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
        }
    }
}
