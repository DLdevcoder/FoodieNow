package com.example.foodienow.feature.order_history

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.foodienow.R
import coil3.compose.AsyncImage
import com.example.foodienow.domain.model.Order
import com.example.foodienow.domain.model.OrderStatus
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.draw.clip

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderHistoryScreen(
    onBack: () -> Unit,
    onNavigateToOrderDetail: (String) -> Unit,
    viewModel: OrderHistoryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf(
        R.string.my_orders_tab_ongoing,
        R.string.my_orders_tab_history,
        R.string.my_orders_tab_cancelled
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
    ) {
        TopAppBar(
            title = {
                Text(
                    stringResource(R.string.my_orders_title),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Medium
                )
            },
            actions = {
                IconButton(onClick = { }) {
                    Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.primary,
                titleContentColor = MaterialTheme.colorScheme.onPrimary,
                navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                actionIconContentColor = MaterialTheme.colorScheme.onPrimary
            )
        )

        TabRow(
            selectedTabIndex = selectedTabIndex,
            containerColor = Color.White,
            contentColor = MaterialTheme.colorScheme.primary,
            indicator = { tabPositions ->
                TabRowDefaults.Indicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                    color = MaterialTheme.colorScheme.primary
                )
            },
            divider = { HorizontalDivider(color = Color.LightGray) }
        ) {
            tabs.forEachIndexed { index, titleRes ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index },
                    text = {
                        Text(
                            stringResource(titleRes),
                            color = if (selectedTabIndex == index) MaterialTheme.colorScheme.primary else Color.Gray,
                            fontWeight = if (selectedTabIndex == index) FontWeight.SemiBold else FontWeight.Normal
                        )
                    }
                )
            }
        }

        val ongoingOrders = uiState.orders.filter { it.status in listOf(OrderStatus.PENDING, OrderStatus.PREPARING, OrderStatus.DELIVERING) }
        val historyOrders = uiState.orders.filter { it.status == OrderStatus.COMPLETED }
        val cancelledOrders = uiState.orders.filter { it.status == OrderStatus.CANCELLED }

        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        } else {
            when (selectedTabIndex) {
                0 -> OrdersTabContent(orders = ongoingOrders, isEmptyState = ongoingOrders.isEmpty(), onNavigateToOrderDetail = onNavigateToOrderDetail)
                1 -> OrdersTabContent(orders = historyOrders, isEmptyState = historyOrders.isEmpty(), onNavigateToOrderDetail = onNavigateToOrderDetail)
                2 -> OrdersTabContent(orders = cancelledOrders, isEmptyState = cancelledOrders.isEmpty(), onNavigateToOrderDetail = onNavigateToOrderDetail)
            }
        }
    }
}

@Composable
private fun OrdersTabContent(orders: List<Order>, isEmptyState: Boolean, onNavigateToOrderDetail: (String) -> Unit) {
    if (isEmptyState) {
        EmptyOngoingState()
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            items(orders.size) { index ->
                OrderCardItem(order = orders[index], onNavigateToOrderDetail = onNavigateToOrderDetail)
            }
        }
    }
}

@Composable
private fun EmptyOngoingState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            Icons.Default.Assignment,
            contentDescription = null,
            modifier = Modifier.size(100.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            stringResource(R.string.my_orders_empty_title),
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            stringResource(R.string.my_orders_empty_desc),
            color = Color.Gray,
            textAlign = TextAlign.Center,
            fontSize = 14.sp
        )
    }
}

@Composable
private fun OrderCardItem(order: Order, onNavigateToOrderDetail: (String) -> Unit) {
    val statusColor = when (order.status) {
        OrderStatus.COMPLETED -> Color(0xFF10B981) // Green
        OrderStatus.CANCELLED -> Color(0xFFEF4444) // Red
        else -> Color(0xFFF59E0B) // Orange
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    stringResource(
                        R.string.order_history_order_title,
                        order.id?.substring(0, 8) ?: "Unknown"
                    ),
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color.Black
                )
                Box(
                    modifier = Modifier
                        .background(statusColor.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        order.status.name,
                        fontSize = 12.sp,
                        color = statusColor,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = Color(0xFFF3F4F6))
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AsyncImage(
                    model = order.previewImageUrl
                        ?: "https://images.unsplash.com/photo-1546069901-ba9599a7e63c?w=500&q=80",
                    contentDescription = null,
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.LightGray),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        order.previewFoodName ?: "Đơn hàng từ FoodieNow",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        order.deliveryAddress,
                        fontSize = 13.sp,
                        color = Color.Gray,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = Color(0xFFF3F4F6))
            Spacer(modifier = Modifier.height(12.dp))

            val formattedDate = order.createdAt?.let {
                if (it.length >= 16) it.substring(0, 16).replace("T", " ") else it
            } ?: ""

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    formattedDate,
                    fontSize = 12.sp,
                    color = Color.Gray,
                    modifier = Modifier.weight(1f)
                )
                Column(horizontalAlignment = Alignment.End) {
                    Text("Tổng cộng", fontSize = 12.sp, color = Color.Gray)
                    val formattedPrice =
                        java.text.NumberFormat.getCurrencyInstance(java.util.Locale("vi", "VN"))
                            .format(order.totalPrice)
                    Text(
                        formattedPrice,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            if (order.status == OrderStatus.COMPLETED) {
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { order.id?.let { onNavigateToOrderDetail(it) } },
                        modifier = Modifier.weight(1f).height(40.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text("Chi tiết", fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = { /* TODO: Reorder */ },
                        modifier = Modifier.weight(1f).height(40.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary.copy(
                                alpha = 0.1f
                            ), contentColor = MaterialTheme.colorScheme.primary
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Đặt lại đơn này", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}