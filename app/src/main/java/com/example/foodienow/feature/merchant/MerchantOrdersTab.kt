package com.example.foodienow.feature.merchant

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.foodienow.R
import com.example.foodienow.core.designsystem.components.FoodieCard
import com.example.foodienow.core.designsystem.theme.PromoGradientEnd
import com.example.foodienow.core.designsystem.theme.PromoGradientStart
import com.example.foodienow.domain.model.Order
import com.example.foodienow.domain.model.OrderStatus
import java.text.NumberFormat
import java.util.Locale

@Composable
fun MerchantOrdersTab(
    onNavigateToChatList: () -> Unit,
    unreadMessageCount: Int,
    viewModel: MerchantOrdersViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedTabIndex by remember { mutableIntStateOf(0) }

    val tabs = listOf(
        OrderStatus.WAITING_STORE_CONFIRMATION to "Chờ xác nhận",
        OrderStatus.PREPARING to "Đang chuẩn bị",
        OrderStatus.WAITING_SHIPPER to "Chờ nhận đơn",
        OrderStatus.PICKING_UP to "Chờ shipper lấy",
        OrderStatus.DELIVERING to "Đang giao",
        null to "Lịch sử"
    )

    val cancelReason = stringResource(R.string.merchant_orders_cancel_reason_store)

    Column(modifier = Modifier.fillMaxSize()) {
        Surface(
            color = Color.Transparent,
            contentColor = Color.White
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                PromoGradientStart,
                                MaterialTheme.colorScheme.primary,
                                PromoGradientEnd
                            )
                        )
                    )
                    .statusBarsPadding()
                    .padding(start = 18.dp, top = 16.dp, end = 18.dp, bottom = 18.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(MaterialTheme.shapes.large)
                            .background(Color.White.copy(alpha = 0.18f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Assignment,
                            contentDescription = null,
                            modifier = Modifier.size(27.dp),
                            tint = Color.White
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.merchant_orders_manage_title),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = stringResource(R.string.merchant_orders_manage_subtitle),
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.84f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    val chatBadgePlus = stringResource(R.string.merchant_orders_chat_badge_plus)
                    Surface(
                        shape = CircleShape,
                        color = Color.White.copy(alpha = 0.18f),
                        contentColor = Color.White
                    ) {
                        IconButton(
                            onClick = onNavigateToChatList,
                            modifier = Modifier.size(44.dp)
                        ) {
                            BadgedBox(
                                badge = {
                                    if (unreadMessageCount > 0) {
                                        Badge(
                                            containerColor = MaterialTheme.colorScheme.error,
                                            contentColor = Color.White
                                        ) {
                                            Text(
                                                text = if (unreadMessageCount > 99) chatBadgePlus
                                                else unreadMessageCount.toString()
                                            )
                                        }
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ChatBubbleOutline,
                                    contentDescription = stringResource(R.string.chat_list_title),
                                    tint = Color.White,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // 2. CHUYỂN SANG ScrollableTabRow VÌ ĐÃ CÓ 6 TAB, KHÔNG ĐỦ CHỖ NẾU CHIA ĐỀU
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
                    // Sửa lại cách gọi text vì tôi dùng String trực tiếp thay vì Resource ID ở trên
                    // Nếu bạn có file strings.xml, hãy tạo thẻ string và đổi lại thành stringResource(tab.second as Int)
                    text = { Text(tab.second.toString()) }
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

        val currentTab = tabs[selectedTabIndex]
        val filteredOrders = uiState.orders
            .filter {
                if (currentTab.first == null) {
                    it.status == OrderStatus.COMPLETED ||
                            it.status == OrderStatus.CANCELLED_BY_CUSTOMER ||
                            it.status == OrderStatus.CANCELLED_BY_STORE ||
                            it.status == OrderStatus.NO_SHIPPER_FOUND ||
                            it.status == OrderStatus.PAYMENT_FAILED ||
                            it.status == OrderStatus.DELIVERY_TIMEOUT
                } else {
                    it.status == currentTab.first
                }
            }
            .sortedByDescending { it.createdAt }

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
                        onAccept = { order.id?.let { viewModel.acceptOrder(it) } },
                        onCancel = { order.id?.let { viewModel.rejectOrder(it, cancelReason) } },
                        onMarkReady = { order.id?.let { viewModel.markOrderReady(it) } }
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
    onCancel: () -> Unit,
    onMarkReady: () -> Unit
) {
    val formatter = NumberFormat.getInstance(Locale("vi", "VN"))
    val formattedPrice = "${formatter.format(order.totalPrice)} VND"

    val displayTime = order.createdAt?.take(16)?.replace("T", " ") ?: "-"

    FoodieCard(
        modifier = Modifier.fillMaxWidth()
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
                text = stringResource(R.string.merchant_orders_customer_name, order.customerId.take(8)),
                style = MaterialTheme.typography.bodyMedium
            )

            val statusDisplay = order.status.displayNameVi

            val statusColor = when (order.status) {
                OrderStatus.COMPLETED -> Color(0xFF4CAF50)
                OrderStatus.CANCELLED_BY_CUSTOMER,
                OrderStatus.CANCELLED_BY_STORE,
                OrderStatus.NO_SHIPPER_FOUND,
                OrderStatus.PAYMENT_FAILED,
                OrderStatus.DELIVERY_TIMEOUT -> MaterialTheme.colorScheme.error
                else -> MaterialTheme.colorScheme.primary
            }

            Text(
                text = "$statusDisplay - $formattedPrice",
                style = MaterialTheme.typography.bodyMedium,
                color = statusColor,
                fontWeight = FontWeight.SemiBold
            )

            if (!order.note.isNullOrBlank()) {
                Text(
                    text = stringResource(R.string.merchant_orders_note_prefix, order.note),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFFF57C00),
                    fontWeight = FontWeight.Medium
                )
            }

            if (order.status == OrderStatus.WAITING_STORE_CONFIRMATION) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(onClick = onCancel) {
                        Text(stringResource(R.string.merchant_orders_action_cancel))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = onAccept) {
                        Text(stringResource(R.string.merchant_orders_action_accept))
                    }
                }
            } else if (order.status == OrderStatus.PREPARING) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(onClick = onMarkReady) {
                        Text(stringResource(R.string.merchant_orders_action_prepared))
                    }
                }
            }
        }
    }
}