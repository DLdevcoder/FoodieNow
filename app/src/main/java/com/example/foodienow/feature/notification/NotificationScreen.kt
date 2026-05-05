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
import androidx.compose.material3.MaterialTheme
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
            colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    stringResource(R.string.notifications_promotions),
                    color = Color.Gray,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            items(5) { index ->
                NotificationCard(index)
            }
        }
    }
}

@Composable
private fun NotificationCard(index: Int) {
    val titles = listOf(
        "Đơn hàng giao thành công",
        "GIẢM 99.000Đ, lễ to deal giảm to!",
        "Khao MÓN GIẢM 30.000Đ",
        "Đơn hàng đang được chuẩn bị",
        "Giảm 50% tất cả món ăn"
    )
    val descs = listOf(
        "Đơn hàng #384729 của bạn đã được giao thành công. Chúc bạn ngon miệng!",
        "👉Khi nhập mã HOLIDAY99 🍗Gà Nướng & Gà Bó Xôi Thành Công 🍕Pizza & Spaghetti Pizza Huu🍲Tiệm Lẩu Nhà An... Mở tiệc thôi!",
        "👉Khi nhập mã SPFMOI30K 🍮Rau câu bánh flan 🍹Trà trái cây nhiệt đới 🥥Nước dừa tươi... Ăn xế thôi!",
        "Nhà hàng đang chuẩn bị món ăn cho đơn hàng #384730 của bạn.",
        "Nhập mã DEALSOCK để được giảm giá ngay hôm nay."
    )
    val isUnread = index < 2
    val iconBg = if (index % 3 == 0) Color(0xFF10B981) else MaterialTheme.colorScheme.primary
    val icon = if (index % 3 == 0) Icons.Default.ReceiptLong else Icons.Default.Campaign

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = if (isUnread) Color(0xFFFFF9F2) else Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(iconBg.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = iconBg, modifier = Modifier.size(24.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        titles.getOrElse(index) { "Notification" },
                        fontSize = 16.sp,
                        fontWeight = if (isUnread) FontWeight.Bold else FontWeight.Medium,
                        color = Color.Black,
                        modifier = Modifier.weight(1f)
                    )
                    if (isUnread) {
                        Box(
                            modifier = Modifier
                                .padding(start = 8.dp, top = 4.dp)
                                .size(8.dp)
                                .background(Color.Red, CircleShape)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    descs.getOrElse(index) { "Description" },
                    fontSize = 14.sp,
                    color = Color.Gray,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    if (index == 0) "Vừa xong" else "${index * 2} giờ trước",
                    fontSize = 12.sp,
                    color = Color.LightGray
                )
            }
        }
    }
}
