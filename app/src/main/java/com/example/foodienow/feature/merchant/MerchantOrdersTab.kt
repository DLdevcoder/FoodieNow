package com.example.foodienow.feature.merchant

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.foodienow.R
import com.example.foodienow.domain.model.Order
import com.example.foodienow.domain.model.OrderStatus
import java.text.NumberFormat
import java.util.Locale

@Composable
fun MerchantOrdersTab(
    viewModel: MerchantOrdersViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedTabIndex by remember { mutableIntStateOf(0) }

    val tabs = listOf(
        OrderStatus.PENDING to R.string.merchant_orders_status_pending,
        OrderStatus.PREPARING to R.string.merchant_orders_status_preparing,
        OrderStatus.DELIVERING to R.string.merchant_orders_status_delivering,
        OrderStatus.COMPLETED to R.string.merchant_orders_status_completed,
        OrderStatus.CANCELLED to R.string.merchant_orders_status_cancelled
    )

    Column(modifier = Modifier.fillMaxSize()) {
        ScrollableTabRow(
            selectedTabIndex = selectedTabIndex,
            edgePadding = 8.dp,
            indicator = { tabPositions ->
                if (selectedTabIndex < tabPositions.size) {
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex])
                    )
                }
            }
        ) {
            tabs.forEachIndexed { index, tab ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index },
                    text = { Text(stringResource(tab.second)) }
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

        val currentStatusFilter = tabs[selectedTabIndex].first
        val filteredOrders = uiState.orders.filter { it.status == currentStatusFilter }

        if (filteredOrders.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.merchant_orders_empty),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredOrders, key = { it.id ?: it.hashCode() }) { order ->
                    MerchantOrderCard(
                        order = order,
                        onAccept = { order.id?.let { viewModel.updateOrderStatus(it, OrderStatus.PREPARING) } },
                        onReady = { order.id?.let { viewModel.updateOrderStatus(it, OrderStatus.DELIVERING) } },
                        onCancel = { order.id?.let { viewModel.updateOrderStatus(it, OrderStatus.CANCELLED) } }
                    )
                }
            }
        }
    }
}

@Composable
private fun MerchantOrderCard(
    order: Order,
    onAccept: () -> Unit,
    onReady: () -> Unit,
    onCancel: () -> Unit
) {
    val formatter = NumberFormat.getInstance(Locale("vi", "VN"))
    val formattedPrice = "${formatter.format(order.totalPrice)} VND"

    val displayTime = order.createdAt?.take(16)?.replace("T", " ") ?: "-"

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
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
                    text = stringResource(R.string.activity_history_order_title, order.id?.take(8) ?: "N/A"),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = displayTime,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Text(
                // Do chưa join bảng users nên tạm thời hiển thị ID rút gọn của khách hàng
                text = stringResource(R.string.merchant_orders_customer_name, order.customerId.take(8)),
                style = MaterialTheme.typography.bodyMedium
            )

            Text(
                text = stringResource(R.string.activity_history_item_subtitle, order.status.name, formattedPrice),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold
            )

            if (order.status == OrderStatus.PENDING || order.status == OrderStatus.PREPARING) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (order.status == OrderStatus.PENDING) {
                        OutlinedButton(onClick = onCancel) {
                            Text(stringResource(R.string.merchant_orders_action_cancel))
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(onClick = onAccept) {
                            Text(stringResource(R.string.merchant_orders_action_accept))
                        }
                    } else {
                        Button(onClick = onReady) {
                            Text(stringResource(R.string.merchant_orders_action_ready))
                        }
                    }
                }
            }
        }
    }
}