package com.example.foodienow.feature.order_history

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.RemoveShoppingCart
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.IconButton
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.foodienow.R
import com.example.foodienow.core.designsystem.components.FoodImage
import com.example.foodienow.core.designsystem.components.FoodieCard
import com.example.foodienow.core.designsystem.components.FoodieEmptyState
import com.example.foodienow.core.designsystem.components.FoodieErrorState
import com.example.foodienow.core.designsystem.components.FoodieLoadingState
import com.example.foodienow.core.designsystem.components.VoucherBadge
import com.example.foodienow.core.designsystem.components.shimmerEffect
import com.example.foodienow.core.designsystem.theme.AmberTertiary
import com.example.foodienow.core.designsystem.theme.ErrorRed
import com.example.foodienow.core.designsystem.theme.FoodieCream
import com.example.foodienow.core.designsystem.theme.FoodieNowTheme
import com.example.foodienow.core.designsystem.theme.InfoBlue
import com.example.foodienow.core.designsystem.theme.PromoGradientEnd
import com.example.foodienow.core.designsystem.theme.PromoGradientStart
import com.example.foodienow.core.designsystem.theme.SuccessGreen
import com.example.foodienow.domain.model.Food
import com.example.foodienow.domain.model.Order
import com.example.foodienow.domain.model.OrderStatus
import com.example.foodienow.feature.customer_home.components.formatPrice

private enum class OrdersTab(
    val title: String,
    val icon: ImageVector
) {
    CART("Giỏ hàng", Icons.Default.ShoppingCart),
    ACTIVE("Đang giao", Icons.Default.LocalShipping),
    HISTORY("Lịch sử", Icons.Default.History)
}

@Composable
fun OrderHistoryScreen(
    onBack: () -> Unit,
    onNavigateToOrderDetail: (String) -> Unit,
    onNavigateToTracking: (String) -> Unit,
    onNavigateToCart: () -> Unit = {},
    onNavigateToFoodDetail: (Food) -> Unit = {},
    initialTab: Int = 0,
    viewModel: OrderHistoryViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    var selectedTab by remember(initialTab) { mutableIntStateOf(initialTab) }

    val activeStatuses = setOf(
        OrderStatus.PENDING,
        OrderStatus.PREPARING,
        OrderStatus.DRIVER_ASSIGNED,
        OrderStatus.DELIVERING
    )
    val activeOrders = uiState.orders.filter { it.status in activeStatuses }
    val historyOrders = uiState.orders.filter {
        it.status == OrderStatus.COMPLETED || it.status == OrderStatus.CANCELLED
    }
    val cartItems = uiState.cartItems

    var orderToCancel by remember { mutableStateOf<Order?>(null) }
    var cancelReason by remember { mutableStateOf("") }

    if (orderToCancel != null) {
        AlertDialog(
            onDismissRequest = {
                orderToCancel = null
                cancelReason = ""
            },
            title = { Text(text = "Hủy đơn hàng") },
            text = {
                Column {
                    Text(text = "Vui lòng cho biết lý do bạn muốn hủy đơn hàng này:")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = cancelReason,
                        onValueChange = { cancelReason = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Ví dụ: Đổi ý, đặt nhầm món...") },
                        maxLines = 3
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.cancelOrder(orderToCancel!!, cancelReason)
                        orderToCancel = null
                        cancelReason = ""
                    },
                    enabled = cancelReason.isNotBlank()
                ) {
                    Text("Xác nhận hủy", color = ErrorRed)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        orderToCancel = null
                        cancelReason = ""
                    }
                ) {
                    Text("Đóng")
                }
            }
        )
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            OrdersHeader(
                cartCount = cartItems.values.sum(),
                activeCount = activeOrders.size,
                historyCount = historyOrders.size
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(FoodieCream)
        ) {
            OrdersSegmentedTabs(
                selectedIndex = selectedTab,
                counts = listOf(cartItems.values.sum(), activeOrders.size, historyOrders.size),
                onSelected = { selectedTab = it }
            )

            when {
                uiState.isLoading -> {
                    OrdersLoadingState(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                    )
                }

                uiState.errorResId != null -> {
                    val errorResId = uiState.errorResId ?: R.string.error_load_order_history
                    FoodieErrorState(
                        title = "Không thể tải đơn hàng",
                        subtitle = stringResource(errorResId),
                        actionLabel = "Thử lại",
                        onAction = viewModel::loadOrders,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                    )
                }

                selectedTab == OrdersTab.CART.ordinal -> {
                    CartOrdersTab(
                        cartItems = cartItems,
                        onNavigateToCart = onNavigateToCart,
                        onNavigateToFoodDetail = onNavigateToFoodDetail,
                        onUpdateQuantity = { food, qty -> viewModel.updateQuantity(food, qty) },
                        onBack = onBack,
                        modifier = Modifier.weight(1f)
                    )
                }

                selectedTab == OrdersTab.ACTIVE.ordinal -> {
                    OrdersListTab(
                        orders = activeOrders,
                        emptyIcon = Icons.Default.LocalShipping,
                        emptyTitle = "Chưa có đơn đang giao",
                        emptySubtitle = "Các đơn đã thanh toán và đang được chuẩn bị, lấy hàng hoặc giao đến bạn sẽ xuất hiện tại đây.",
                        onNavigateToOrderDetail = onNavigateToOrderDetail,
                        onNavigateToTracking = onNavigateToTracking,
                        onReorder = null,
                        onCancelClick = { order -> orderToCancel = order },
                        modifier = Modifier.weight(1f)
                    )
                }

                else -> {
                    val onReorder: (String) -> Unit = { orderId ->
                        viewModel.reorder(orderId) {
                            Toast.makeText(context, "Đã thêm món vào giỏ hàng", Toast.LENGTH_SHORT).show()
                            onNavigateToCart()
                        }
                    }
                    OrdersListTab(
                        orders = historyOrders,
                        emptyIcon = Icons.Default.History,
                        emptyTitle = "Chưa có lịch sử đơn hàng",
                        emptySubtitle = "Những đơn đã giao thành công hoặc đã hủy sẽ được lưu tại đây.",
                        onNavigateToOrderDetail = onNavigateToOrderDetail,
                        onNavigateToTracking = null,
                        onReorder = onReorder,
                        onCancelClick = null,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun OrdersHeader(
    cartCount: Int,
    activeCount: Int,
    historyCount: Int
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    listOf(
                        PromoGradientStart,
                        MaterialTheme.colorScheme.primary,
                        PromoGradientEnd
                    )
                )
            )
            .statusBarsPadding()
            .padding(horizontal = 18.dp, vertical = 16.dp)
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
                    Icons.AutoMirrored.Filled.ReceiptLong,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.my_orders_title),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "$cartCount món trong giỏ • $activeCount đơn đang xử lý • $historyCount đơn đã lưu",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.84f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun OrdersSegmentedTabs(
    selectedIndex: Int,
    counts: List<Int>,
    onSelected: (Int) -> Unit
) {
    val tabs = OrdersTab.entries

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp)
            .padding(top = 16.dp),
        shape = MaterialTheme.shapes.extraLarge,
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = FoodieNowTheme.elevation.card,
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier.padding(6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            tabs.forEachIndexed { index, tab ->
                OrdersSegment(
                    selected = selectedIndex == index,
                    title = tab.title,
                    count = counts.getOrElse(index) { 0 },
                    icon = tab.icon,
                    modifier = Modifier.weight(1f),
                    onClick = { onSelected(index) }
                )
            }
        }
    }
}

@Composable
private fun OrdersSegment(
    selected: Boolean,
    title: String,
    count: Int,
    icon: ImageVector,
    modifier: Modifier,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier
            .height(48.dp)
            .clickable(onClick = onClick),
        shape = MaterialTheme.shapes.large,
        color = if (selected) MaterialTheme.colorScheme.primary else Color.Transparent,
        contentColor = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 9.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(17.dp))
            Spacer(modifier = Modifier.width(5.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.width(5.dp))
            CountPill(count = count, selected = selected)
        }
    }
}

@Composable
private fun CountPill(count: Int, selected: Boolean) {
    Surface(
        shape = CircleShape,
        color = if (selected) {
            MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.18f)
        } else {
            MaterialTheme.colorScheme.surfaceVariant
        },
        contentColor = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
    ) {
        Text(
            text = count.toString(),
            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.ExtraBold,
            maxLines = 1
        )
    }
}

@Composable
private fun CartOrdersTab(
    cartItems: Map<Food, Int>,
    onNavigateToCart: () -> Unit,
    onNavigateToFoodDetail: (Food) -> Unit,
    onUpdateQuantity: (Food, Int) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (cartItems.isEmpty()) {
        FoodieEmptyState(
            icon = Icons.Default.RemoveShoppingCart,
            title = "Giỏ hàng đang trống",
            subtitle = "Các món bạn thêm nhưng chưa thanh toán sẽ nằm ở đây.",
            actionLabel = "Khám phá món ngon",
            onAction = onBack,
            modifier = modifier.fillMaxWidth()
        )
        return
    }

    val total = cartItems.entries.sumOf { (food, quantity) -> food.price * quantity }

    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            CartSummaryCard(
                itemCount = cartItems.values.sum(),
                total = total,
                onNavigateToCart = onNavigateToCart
            )
        }
        items(
            items = cartItems.entries.toList(),
            key = { it.key.id.ifBlank { it.key.name } }
        ) { entry ->
            CartFoodCard(
                food = entry.key,
                quantity = entry.value,
                onNavigateToFoodDetail = onNavigateToFoodDetail,
                onUpdateQuantity = onUpdateQuantity
            )
        }
        item {
            Spacer(modifier = Modifier.navigationBarsPadding())
        }
    }
}

@Composable
private fun CartSummaryCard(
    itemCount: Int,
    total: Long,
    onNavigateToCart: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.extraLarge)
            .background(Brush.linearGradient(listOf(PromoGradientStart, PromoGradientEnd)))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(MaterialTheme.shapes.large)
                    .background(Color.White.copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.ShoppingCart, contentDescription = null, tint = Color.White)
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "$itemCount món chưa thanh toán",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "Tạm tính ${total.formatPrice()}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.86f)
                )
            }
            Button(
                onClick = onNavigateToCart,
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = MaterialTheme.colorScheme.primary
                ),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
            ) {
                Text("Thanh toán", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun CartFoodCard(
    food: Food,
    quantity: Int,
    onNavigateToFoodDetail: (Food) -> Unit,
    onUpdateQuantity: (Food, Int) -> Unit
) {
    FoodieCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onNavigateToFoodDetail(food) }
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FoodImage(
                imageUrl = food.imageUrl,
                contentDescription = food.name,
                modifier = Modifier.size(78.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = food.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = food.price.formatPrice(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = (food.price * quantity).formatPrice(),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .background(
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                shape = MaterialTheme.shapes.large
                            )
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    ) {
                        IconButton(
                            onClick = { onUpdateQuantity(food, quantity - 1) },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = if (quantity == 1) Icons.Default.Delete else Icons.Default.Remove,
                                contentDescription = null,
                                tint = if (quantity == 1) ErrorRed else MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Text(
                            text = quantity.toString(),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                        IconButton(
                            onClick = { onUpdateQuantity(food, quantity + 1) },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
@Composable
private fun OrdersListTab(
    orders: List<Order>,
    emptyIcon: ImageVector,
    emptyTitle: String,
    emptySubtitle: String,
    onNavigateToOrderDetail: (String) -> Unit,
    onNavigateToTracking: ((String) -> Unit)?,
    onReorder: ((String) -> Unit)?,
    onCancelClick: ((Order) -> Unit)?,
    modifier: Modifier = Modifier
) {
    if (orders.isEmpty()) {
        FoodieEmptyState(
            icon = emptyIcon,
            title = emptyTitle,
            subtitle = emptySubtitle,
            modifier = modifier.fillMaxWidth()
        )
        return
    }

    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(
            items = orders,
            key = { it.id ?: it.hashCode() }
        ) { order ->
            OrderCardItem(
                order = order,
                onNavigateToOrderDetail = onNavigateToOrderDetail,
                onNavigateToTracking = onNavigateToTracking,
                onReorder = onReorder,
                onCancelClick = onCancelClick
            )
        }
        item {
            Spacer(modifier = Modifier.navigationBarsPadding())
        }
    }
}

@Composable
private fun OrderCardItem(
    order: Order,
    onNavigateToOrderDetail: (String) -> Unit,
    onNavigateToTracking: ((String) -> Unit)?,
    onReorder: ((String) -> Unit)?,
    onCancelClick: ((Order) -> Unit)?
) {
    val style = order.status.toOrderStatusStyle()

    FoodieCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = { order.id?.let(onNavigateToOrderDetail) }
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Khối Header, Divider, Detail của OrderCardItem được giữ nguyên
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(MaterialTheme.shapes.medium)
                        .background(style.color.copy(alpha = 0.14f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(style.icon, contentDescription = null, tint = style.color, modifier = Modifier.size(22.dp))
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.order_card_title_no_id),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = order.createdAt.toDisplayTime(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                VoucherBadge(label = style.label, containerColor = style.color)
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.16f))
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FoodImage(
                    imageUrl = order.previewImageUrl,
                    contentDescription = order.previewFoodName,
                    modifier = Modifier.size(74.dp)
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (order.otherItemsCount != null && order.otherItemsCount > 0) {
                            stringResource(
                                R.string.order_food_name_with_others,
                                order.previewFoodName ?: "Đơn hàng từ FoodieNow",
                                order.otherItemsCount
                            )
                        } else {
                            order.previewFoodName ?: "Đơn hàng từ FoodieNow"
                        },
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = order.deliveryAddress,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.16f))
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Payments,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(17.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = order.totalPrice.formatPrice(),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (onNavigateToTracking != null) {
                        if (order.status == OrderStatus.PREPARING || order.status == OrderStatus.PENDING) {
                            OutlinedButton(
                                onClick = { onCancelClick?.invoke(order) },
                                shape = MaterialTheme.shapes.medium,
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = ErrorRed),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Text("Hủy đơn", fontWeight = FontWeight.Bold)
                            }
                        } else {
                            Button(
                                onClick = { order.id?.let(onNavigateToTracking) },
                                shape = MaterialTheme.shapes.medium,
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Text("Theo dõi", fontWeight = FontWeight.Bold)
                            }
                        }
                    } else {
                        // TAB LỊCH SỬ
                        OutlinedButton(
                            onClick = { order.id?.let(onNavigateToOrderDetail) },
                            shape = MaterialTheme.shapes.medium,
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Text("Chi tiết", fontWeight = FontWeight.Bold)
                        }

                        if (onReorder != null && order.status == OrderStatus.COMPLETED) {
                            Button(
                                onClick = { order.id?.let(onReorder) },
                                shape = MaterialTheme.shapes.medium,
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Text("Đặt lại", fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
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
private fun OrdersLoadingState(modifier: Modifier = Modifier) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(4) {
            OrderSkeletonCard()
        }
    }
}

@Composable
private fun OrderSkeletonCard() {
    FoodieCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(74.dp)
                    .clip(MaterialTheme.shapes.medium)
                    .shimmerEffect()
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.7f)
                        .height(18.dp)
                        .clip(MaterialTheme.shapes.small)
                        .shimmerEffect()
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(15.dp)
                        .clip(MaterialTheme.shapes.small)
                        .shimmerEffect()
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.45f)
                        .height(16.dp)
                        .clip(MaterialTheme.shapes.small)
                        .shimmerEffect()
                )
            }
        }
    }
}

private data class OrderStatusStyle(
    val label: String,
    val color: Color,
    val icon: ImageVector
)

private fun OrderStatus.toOrderStatusStyle(): OrderStatusStyle {
    return when (this) {
        OrderStatus.PENDING -> OrderStatusStyle("Chờ xác nhận", AmberTertiary, Icons.Default.AccessTime)
        OrderStatus.PREPARING -> OrderStatusStyle("Đang chuẩn bị", AmberTertiary, Icons.AutoMirrored.Filled.Assignment)
        OrderStatus.DRIVER_ASSIGNED -> OrderStatusStyle("Đã có tài xế", InfoBlue, Icons.Default.LocalShipping)
        OrderStatus.DELIVERING -> OrderStatusStyle("Đang giao", InfoBlue, Icons.Default.LocalShipping)
        OrderStatus.COMPLETED -> OrderStatusStyle("Đã giao", SuccessGreen, Icons.Default.CheckCircle)
        OrderStatus.CANCELLED -> OrderStatusStyle("Đã hủy", ErrorRed, Icons.Default.RemoveShoppingCart)
    }
}

private fun String?.toDisplayTime(): String {
    if (this.isNullOrBlank()) return "Chưa rõ thời gian"
    return if (length >= 16) substring(0, 16).replace("T", " ") else this
}
