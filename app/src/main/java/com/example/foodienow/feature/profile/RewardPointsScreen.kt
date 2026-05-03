package com.example.foodienow.feature.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class PointHistory(val id: String, val reason: String, val date: String, val amount: Int)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RewardPointsScreen(onBack: () -> Unit) {
    val history = listOf(
        PointHistory("1", "Hoàn thành đơn hàng #ORD001", "01/05/2026", +150),
        PointHistory("2", "Đổi mã giảm giá V2", "28/04/2026", -500),
        PointHistory("3", "Điểm danh hàng ngày", "28/04/2026", +10),
        PointHistory("4", "Hoàn thành đơn hàng #ORD002", "20/04/2026", +300)
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Foodie Xu của tôi") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Quay lại")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFFF5F5F5))
        ) {
            // Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.primary)
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.MonetizationOn, 
                        contentDescription = null, 
                        tint = Color(0xFFFFD700), 
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "12,500 Xu",
                        color = Color.White,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(text = "Tương đương 12,500đ", color = Color.White.copy(alpha = 0.8f))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            
            // Lịch sử
            Column(modifier = Modifier.background(Color.White).fillMaxSize()) {
                Text(
                    text = "Lịch sử Foodie Xu",
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(16.dp),
                    fontSize = 18.sp
                )
                HorizontalDivider()
                LazyColumn {
                    items(history) { item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = item.reason, fontWeight = FontWeight.Medium)
                                Text(text = item.date, color = Color.Gray, fontSize = 12.sp)
                            }
                            Text(
                                text = if (item.amount > 0) "+${item.amount}" else "${item.amount}",
                                color = if (item.amount > 0) Color(0xFF10B981) else Color.Red,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                        HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f))
                    }
                }
            }
        }
    }
}
