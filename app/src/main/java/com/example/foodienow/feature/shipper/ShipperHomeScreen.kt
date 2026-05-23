package com.example.foodienow.feature.shipper

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeliveryDining
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.foodienow.R
import com.example.foodienow.domain.model.Order
import com.example.foodienow.domain.model.OrderStatus
import java.text.NumberFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun ShipperHomeScreen(
    viewModel: ShipperViewModel = hiltViewModel(),
    onNavigateToTracking: (String) -> Unit,
    onLogout: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedTabIndex by remember { mutableIntStateOf(0) }

    val tabs = listOf(
        R.string.shipper_tab_available,
        R.string.shipper_tab_active,
        R.string.shipper_tab_completed
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
    ) {
        ShipperTopSection(
            activeOrderCount = uiState.activeOrders.size,
            onLogout = onLogout
        )

        TabRow(
            selectedTabIndex = selectedTabIndex,
            containerColor = Color.White,
            indicator = { tabPositions ->
                if (selectedTabIndex < tabPositions.size) {
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex])
                    )
                }
            }
        ) {
            tabs.forEachIndexed { index, titleRes ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index },
                    text = {
                        Text(
                            text = stringResource(titleRes),
                            fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                )
            }
        }

        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return
        }

        uiState.error?.let { errorMsg ->
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = errorMsg, color = MaterialTheme.colorScheme.error)
            }
            return
        }

        when (selectedTabIndex) {
            0 -> OrderList(
                orders = uiState.availableOrders,
                emptyMessageRes = R.string.shipper_empty_available,
                onNavigateToMapClick = { }, // Không mở bản đồ khi chưa nhận đơn
                viewModel = viewModel,
                isHistoryTab = false
            )
            1 -> OrderList(
                orders = uiState.activeOrders,
                emptyMessageRes = R.string.shipper_empty_active,
                onNavigateToMapClick = onNavigateToTracking,
                viewModel = viewModel,
                isHistoryTab = false
            )
            2 -> OrderList(
                orders = uiState.completedOrders,
                emptyMessageRes = R.string.shipper_empty_completed,
                onNavigateToMapClick = { },
                viewModel = viewModel,
                isHistoryTab = true
            )
        }
    }
}

@Composable
private fun ShipperTopSection(activeOrderCount: Int, onLogout: () -> Unit) {
    val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    val greeting = when (currentHour) {
        in 0..11 -> "Chào buổi sáng ☀️"
        in 12..17 -> "Chào buổi chiều ⛅"
        else -> "Chào buổi tối 🌙"
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary,
                        Color(0xFFF97316)
                    )
                )
            )
            .padding(top = 40.dp, bottom = 16.dp, start = 16.dp, end = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = greeting,
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Khu vực hoạt động",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                if (activeOrderCount > 0) {
                    Icon(
                        imageVector = Icons.Default.DeliveryDining,
                        contentDescription = null,
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "$activeOrderCount đơn",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                }
                TextButton(onClick = onLogout) {
                    Text("Đăng xuất", color = Color.White)
                }
            }
        }
    }
}

@Composable
private fun OrderList(
    orders: List<Order>,
    emptyMessageRes: Int,
    onNavigateToMapClick: (String) -> Unit,
    viewModel: ShipperViewModel,
    isHistoryTab: Boolean
) {
    if (orders.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                text = stringResource(emptyMessageRes),
                color = Color.Gray
            )
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(orders, key = { it.id ?: it.hashCode() }) { order ->
                ShipperOrderCard(
                    order = order,
                    onNavigateToMap = { order.id?.let { onNavigateToMapClick(it) } },
                    viewModel = viewModel,
                    isHistoryTab = isHistoryTab
                )
            }
        }
    }
}

@Composable
private fun ShipperOrderCard(
    order: Order,
    onNavigateToMap: () -> Unit,
    viewModel: ShipperViewModel,
    isHistoryTab: Boolean
) {
    val formatter = NumberFormat.getInstance(Locale("vi", "VN"))
    val formattedPrice = "${formatter.format(order.totalPrice)} VND"

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Đơn #${order.id?.take(8)}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = formattedPrice,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }

            Text(
                text = "Giao đến: ${order.deliveryAddress}",
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            if (!order.note.isNullOrBlank()) {
                Text(
                    text = "Ghi chú: ${order.note}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFFD32F2F)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Xử lý hiển thị nút bấm theo logic trạng thái mới
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isHistoryTab || order.status == OrderStatus.COMPLETED) {
                    Text(
                        text = "Đã giao thành công",
                        color = Color(0xFF4CAF50),
                        fontWeight = FontWeight.Bold
                    )
                } else {
                    when (order.status) {
                        OrderStatus.PREPARING -> {
                            // Đơn đang chờ Shipper nhận (Tab Chờ nhận)
                            Button(
                                onClick = { order.id?.let { viewModel.acceptOrder(it) } },
                                shape = MaterialTheme.shapes.medium
                            ) {
                                Text(stringResource(R.string.shipper_action_accept))
                            }
                        }
                        OrderStatus.DRIVER_ASSIGNED -> {
                            // Shipper đã nhận, đang trên đường đến lấy hàng (Tab Đang giao)
                            OutlinedButton(
                                onClick = onNavigateToMap,
                                modifier = Modifier.padding(end = 8.dp),
                                shape = MaterialTheme.shapes.medium
                            ) {
                                Text("Xem Bản đồ")
                            }
                            Button(
                                onClick = { order.id?.let { viewModel.markAsDelivering(it) } },
                                shape = MaterialTheme.shapes.medium
                            ) {
                                Text("Đã lấy hàng")
                            }
                        }
                        OrderStatus.DELIVERING -> {
                            // Shipper đã lấy hàng, đang đi giao (Tab Đang giao)
                            OutlinedButton(
                                onClick = onNavigateToMap,
                                modifier = Modifier.padding(end = 8.dp),
                                shape = MaterialTheme.shapes.medium
                            ) {
                                Text("Xem Bản đồ")
                            }
                            Button(
                                onClick = { order.id?.let { viewModel.completeOrder(it) } },
                                shape = MaterialTheme.shapes.medium
                            ) {
                                Text("Hoàn thành")
                            }
                        }
                        else -> {}
                    }
                }
            }
        }
    }
}