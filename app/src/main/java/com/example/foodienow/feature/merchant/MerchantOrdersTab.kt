package com.example.foodienow.feature.merchant

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeliveryDining
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.foodienow.R
import com.example.foodienow.core.designsystem.components.VoucherBadge
import com.example.foodienow.core.designsystem.theme.AmberTertiary
import com.example.foodienow.core.designsystem.theme.ErrorRed
import com.example.foodienow.core.designsystem.theme.FoodieCream
import com.example.foodienow.core.designsystem.theme.FoodieNowTheme
import com.example.foodienow.core.designsystem.theme.InfoBlue
import com.example.foodienow.core.designsystem.theme.SuccessGreen
import com.example.foodienow.core.designsystem.theme.TealSecondary
import com.example.foodienow.core.designsystem.theme.PromoGradientEnd
import com.example.foodienow.core.designsystem.theme.PromoGradientStart
import com.example.foodienow.domain.model.Order
import com.example.foodienow.domain.model.OrderStatus
import java.text.NumberFormat
import java.util.Locale

private data class MerchantOrderTab(
    val status: OrderStatus?,
    val label: String,
    val icon: ImageVector
)

private val merchantTabs = listOf(
    MerchantOrderTab(OrderStatus.WAITING_STORE_CONFIRMATION, "Chờ xác nhận", Icons.Default.HourglassEmpty),
    MerchantOrderTab(OrderStatus.PREPARING, "Đang chuẩn bị", Icons.Default.Inventory2),
    MerchantOrderTab(OrderStatus.WAITING_SHIPPER, "Chờ nhận đơn", Icons.Default.DeliveryDining),
    MerchantOrderTab(OrderStatus.PICKING_UP, "Đang lấy đơn", Icons.Default.DeliveryDining),
    MerchantOrderTab(OrderStatus.DELIVERING, "Đang giao", Icons.Default.DeliveryDining),
    MerchantOrderTab(null, "Lịch sử", Icons.Default.History)
)

@Composable
fun MerchantOrdersTab(
    onNavigateToChatList: () -> Unit,
    unreadMessageCount: Int,
    viewModel: MerchantOrdersViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val cancelReason = stringResource(R.string.merchant_orders_cancel_reason_store)
    val chatBadgePlus = stringResource(R.string.merchant_orders_chat_badge_plus)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FoodieCream)
    ) {
        MerchantOrdersHeader(
            unreadMessageCount = unreadMessageCount,
            onNavigateToChatList = onNavigateToChatList,
            chatBadgePlus = chatBadgePlus
        )

        MerchantOrdersTabRow(
            orders = uiState.orders,
            selectedIndex = selectedTabIndex,
            onSelected = { selectedTabIndex = it }
        )

        when {
            uiState.isLoading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            }

            uiState.error != null -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = uiState.error ?: "",
                        color = ErrorRed,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            else -> {
                val currentTab = merchantTabs[selectedTabIndex]
                val filteredOrders = uiState.orders
                    .filter {
                        if (currentTab.status == null) {
                            it.status == OrderStatus.COMPLETED ||
                                    it.status == OrderStatus.CANCELLED_BY_CUSTOMER ||
                                    it.status == OrderStatus.CANCELLED_BY_STORE ||
                                    it.status == OrderStatus.NO_SHIPPER_FOUND ||
                                    it.status == OrderStatus.PAYMENT_FAILED ||
                                    it.status == OrderStatus.DELIVERY_TIMEOUT
                        } else {
                            it.status == currentTab.status
                        }
                    }
                    .sortedByDescending { it.createdAt }

                if (filteredOrders.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = currentTab.icon,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                            )
                            Text(
                                text = stringResource(R.string.merchant_orders_empty),
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(start = 18.dp, top = 8.dp, end = 18.dp, bottom = 24.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        itemsIndexed(filteredOrders, key = { _, o -> o.id ?: o.hashCode() }) { index, order ->
                            AnimatedVisibility(
                                visible = true,
                                enter = fadeIn(tween(200, delayMillis = index.coerceAtMost(8) * 18)) +
                                        slideInVertically(tween(200, delayMillis = index.coerceAtMost(8) * 18)) { it / 5 }
                            ) {
                                MerchantOrderCard(
                                    order = order,
                                    onAccept = { order.id?.let { viewModel.acceptOrder(it) } },
                                    onCancel = { order.id?.let { viewModel.rejectOrder(it, cancelReason) } },
                                    onMarkReady = { order.id?.let { viewModel.markOrderReady(it) } }
                                )
                            }
                        }
                        item { Spacer(modifier = Modifier.navigationBarsPadding()) }
                    }
                }
            }
        }
    }
}

@Composable
private fun MerchantOrdersHeader(
    unreadMessageCount: Int,
    onNavigateToChatList: () -> Unit,
    chatBadgePlus: String
) {
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
                        imageVector = Icons.AutoMirrored.Filled.Assignment,
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

@Composable
private fun MerchantOrdersTabRow(
    orders: List<Order>,
    selectedIndex: Int,
    onSelected: (Int) -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp)
            .padding(top = 16.dp),
        shape = MaterialTheme.shapes.extraLarge,
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = FoodieNowTheme.elevation.card
    ) {
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            itemsIndexed(merchantTabs) { index, tab ->
                val isSelected = selectedIndex == index
                val pendingCount = orders.count {
                    if (tab.status == null) {
                        it.status == OrderStatus.COMPLETED ||
                                it.status == OrderStatus.CANCELLED_BY_CUSTOMER ||
                                it.status == OrderStatus.CANCELLED_BY_STORE ||
                                it.status == OrderStatus.NO_SHIPPER_FOUND ||
                                it.status == OrderStatus.PAYMENT_FAILED ||
                                it.status == OrderStatus.DELIVERY_TIMEOUT
                    } else {
                        it.status == tab.status
                    }
                }

                val bgColor by animateColorAsState(
                    targetValue = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                    label = "tab_bg"
                )
                val textColor by animateColorAsState(
                    targetValue = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                    label = "tab_text"
                )

                Surface(
                    modifier = Modifier
                        .height(44.dp)
                        .clip(MaterialTheme.shapes.large)
                        .clickable { onSelected(index) },
                    shape = MaterialTheme.shapes.large,
                    color = bgColor,
                    contentColor = textColor
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = tab.label,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                            maxLines = 1
                        )
                        if (pendingCount > 0) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .defaultMinSize(minWidth = 22.dp)
                                    .height(20.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (isSelected) Color.White.copy(alpha = 0.22f)
                                        else MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                                    )
                                    .padding(horizontal = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = pendingCount.toString(),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.primary,
                                    maxLines = 1
                                )
                            }
                        }
                    }
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
    val formattedPrice = "${formatter.format(order.totalPrice)} đ"
    val displayTime = order.createdAt?.take(16)?.replace("T", " ") ?: "-"

    val isActionable = order.status == OrderStatus.WAITING_STORE_CONFIRMATION ||
            order.status == OrderStatus.PREPARING

    val (accentColor, statusIcon, statusLabel) = when (order.status) {
        OrderStatus.WAITING_STORE_CONFIRMATION -> Triple(AmberTertiary, Icons.Default.HourglassEmpty, "Chờ xác nhận")
        OrderStatus.PREPARING -> Triple(InfoBlue, Icons.Default.Inventory2, "Đang chuẩn bị")
        OrderStatus.WAITING_SHIPPER -> Triple(MaterialTheme.colorScheme.primary, Icons.Default.DeliveryDining, "Chờ nhận đơn")
        OrderStatus.PICKING_UP -> Triple(MaterialTheme.colorScheme.primary, Icons.Default.DeliveryDining, "Shipper đang lấy")
        OrderStatus.DELIVERING -> Triple(InfoBlue, Icons.Default.DeliveryDining, "Đang giao")
        OrderStatus.COMPLETED -> Triple(SuccessGreen, Icons.Default.CheckCircle, "Hoàn tất")
        else -> Triple(ErrorRed, Icons.Default.Cancel, "Đã hủy")
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.large),
        shape = MaterialTheme.shapes.large,
        color = if (isActionable) accentColor.copy(alpha = 0.07f) else MaterialTheme.colorScheme.surface,
        tonalElevation = if (isActionable) 4.dp else 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Max)
        ) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(accentColor, accentColor.copy(alpha = 0.5f))
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(MaterialTheme.shapes.medium)
                            .background(accentColor.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = statusIcon,
                            contentDescription = null,
                            tint = accentColor,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = stringResource(R.string.activity_history_order_title, order.id?.takeLast(6) ?: "N/A"),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = if (isActionable) FontWeight.ExtraBold else FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.weight(1f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            if (isActionable) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(accentColor)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = displayTime,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    VoucherBadge(
                        label = statusLabel,
                        containerColor = accentColor.copy(alpha = 0.9f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = stringResource(R.string.merchant_orders_customer_name, order.customerId.take(8)),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                if (!order.note.isNullOrBlank()) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(MaterialTheme.shapes.medium),
                        color = AmberTertiary.copy(alpha = 0.08f),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Text(
                            text = stringResource(R.string.merchant_orders_note_prefix, order.note),
                            style = MaterialTheme.typography.bodySmall,
                            color = AmberTertiary,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Payments,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(17.dp)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = formattedPrice,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (order.status == OrderStatus.WAITING_STORE_CONFIRMATION) {
                            OutlinedButton(
                                onClick = onCancel,
                                shape = MaterialTheme.shapes.medium,
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = ErrorRed),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = stringResource(R.string.merchant_orders_action_cancel),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Button(
                                onClick = onAccept,
                                shape = MaterialTheme.shapes.medium,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = Color.White
                                ),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = stringResource(R.string.merchant_orders_action_accept),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        } else if (order.status == OrderStatus.PREPARING) {
                            Button(
                                onClick = onMarkReady,
                                shape = MaterialTheme.shapes.medium,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = Color.White
                                ),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = stringResource(R.string.merchant_orders_action_prepared),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}