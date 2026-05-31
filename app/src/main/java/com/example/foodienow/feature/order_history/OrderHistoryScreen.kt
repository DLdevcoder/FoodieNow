package com.example.foodienow.feature.order_history

import android.widget.Toast
import androidx.annotation.StringRes
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.shape.RoundedCornerShape

private enum class OrdersTab(
    @StringRes val titleResId: Int,
    val icon: ImageVector
) {
    CART(R.string.order_history_tab_cart, Icons.Default.ShoppingCart),
    ACTIVE(R.string.order_history_tab_active, Icons.Default.LocalShipping),
    HISTORY(R.string.order_history_tab_history, Icons.Default.History)
}

@Composable
fun OrderHistoryScreen(
    onBack: () -> Unit,
    onNavigateToOrderDetail: (String) -> Unit,
    onNavigateToTracking: (String) -> Unit,
    onNavigateToCart: () -> Unit = {},
    onNavigateToFoodDetail: (Food) -> Unit = {},
    initialTab: Int = 0,
    showBackButton: Boolean = true,
    viewModel: OrderHistoryViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    var selectedTab by remember(initialTab) { mutableIntStateOf(initialTab) }

    val activeStatuses = setOf(
        OrderStatus.WAITING_PAYMENT,
        OrderStatus.WAITING_STORE_CONFIRMATION,
        OrderStatus.PREPARING,
        OrderStatus.WAITING_SHIPPER,
        OrderStatus.PICKING_UP,
        OrderStatus.DELIVERING
    )
    val activeOrders = uiState.orders.filter { it.status in activeStatuses }
    val historyOrders = uiState.orders.filter { it.status.isTerminal }
    val cartItems = uiState.cartItems

    var orderToCancel by remember { mutableStateOf<Order?>(null) }
    var cancelReason by remember { mutableStateOf("") }

    if (orderToCancel != null) {
        AlertDialog(
            onDismissRequest = {
                orderToCancel = null
                cancelReason = ""
            },
            title = { Text(text = stringResource(R.string.order_history_cancel_dialog_title)) },
            text = {
                Column {
                    Text(text = stringResource(R.string.order_history_cancel_dialog_desc))
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = cancelReason,
                        onValueChange = { cancelReason = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text(stringResource(R.string.order_history_cancel_dialog_hint)) },
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
                    Text(stringResource(R.string.order_history_cancel_confirm), color = ErrorRed)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        orderToCancel = null
                        cancelReason = ""
                    }
                ) {
                    Text(stringResource(R.string.common_close))
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
                historyCount = historyOrders.size,
                showBackButton = showBackButton,
                onBack = onBack
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
                        title = stringResource(R.string.order_history_error_title),
                        subtitle = stringResource(errorResId),
                        actionLabel = stringResource(R.string.order_history_error_retry),
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
                        emptyTitle = stringResource(R.string.order_history_active_empty_title),
                        emptySubtitle = stringResource(R.string.order_history_active_empty_subtitle),
                        onNavigateToOrderDetail = onNavigateToOrderDetail,
                        onNavigateToTracking = onNavigateToTracking,
                        onReorder = null,
                        onCancelClick = { order -> orderToCancel = order },
                        modifier = Modifier.weight(1f)
                    )
                }

                else -> {
                    val reorderSuccessMsg = stringResource(R.string.order_history_reorder_success_toast)
                    val onReorder: (String) -> Unit = { orderId ->
                        viewModel.reorder(orderId) {
                            Toast.makeText(context, reorderSuccessMsg, Toast.LENGTH_SHORT).show()
                            onNavigateToCart()
                        }
                    }
                    OrdersListTab(
                        orders = historyOrders,
                        emptyIcon = Icons.Default.History,
                        emptyTitle = stringResource(R.string.order_history_history_empty_title),
                        emptySubtitle = stringResource(R.string.order_history_history_empty_subtitle),
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun OrdersHeader(
    cartCount: Int,
    activeCount: Int,
    historyCount: Int,
    showBackButton: Boolean,
    onBack: () -> Unit
) {
    TopAppBar(
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(PromoGradientStart, PromoGradientEnd)
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ReceiptLong,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Column {
                    Text(
                        text = stringResource(R.string.my_orders_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = stringResource(R.string.order_history_header_subtitle, cartCount, activeCount, historyCount),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        navigationIcon = {
            if (showBackButton) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.common_back),
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
            actionIconContentColor = MaterialTheme.colorScheme.onSurface
        ),
        modifier = Modifier.statusBarsPadding()
    )
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
                    title = stringResource(tab.titleResId),
                    count = counts.getOrElse(index) { 0 },
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
    modifier: Modifier,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier
            .height(44.dp)
            .clickable(onClick = onClick),
        shape = MaterialTheme.shapes.large,
        color = if (selected) MaterialTheme.colorScheme.primary else Color.Transparent,
        contentColor = if (selected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.width(6.dp))
            CountPill(count = count, selected = selected)
        }
    }
}

@Composable
private fun CountPill(count: Int, selected: Boolean) {
    Box(
        modifier = Modifier
            .defaultMinSize(minWidth = 24.dp)
            .height(22.dp)
            .clip(CircleShape)
            .background(
                if (selected) {
                    Color.White.copy(alpha = 0.2f)
                } else {
                    MaterialTheme.colorScheme.surfaceVariant
                }
            )
            .padding(horizontal = 7.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = count.toString(),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.ExtraBold,
            color = if (selected) {
                Color.White
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
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
            title = stringResource(R.string.order_history_cart_empty_title),
            subtitle = stringResource(R.string.order_history_cart_empty_subtitle),
            actionLabel = stringResource(R.string.order_history_cart_empty_action),
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
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.large),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp
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
                            colors = listOf(PromoGradientStart, PromoGradientEnd)
                        )
                    )
            )
            Row(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(MaterialTheme.shapes.medium)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.ShoppingCart,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.order_history_cart_summary_title, itemCount),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = stringResource(R.string.order_history_cart_summary_total, total.formatPrice()),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Button(
                    onClick = onNavigateToCart,
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = Color.White
                    ),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Text(stringResource(R.string.cart_checkout), fontWeight = FontWeight.Bold)
                }
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
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.large),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .clip(MaterialTheme.shapes.large)
                .clickable { onNavigateToFoodDetail(food) }
                .padding(12.dp),
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
    val defaultFoodName = stringResource(R.string.order_history_default_food_name)
    val activeStatuses = setOf(
        OrderStatus.WAITING_PAYMENT,
        OrderStatus.WAITING_STORE_CONFIRMATION,
        OrderStatus.PREPARING,
        OrderStatus.WAITING_SHIPPER,
        OrderStatus.PICKING_UP,
        OrderStatus.DELIVERING
    )
    val isActive = order.status in activeStatuses

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.large),
        shape = MaterialTheme.shapes.large,
        color = if (isActive) {
            MaterialTheme.colorScheme.primary.copy(alpha = 0.07f)
        } else {
            MaterialTheme.colorScheme.surface
        },
        tonalElevation = if (isActive) 4.dp else 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Max)
                .clickable { order.id?.let(onNavigateToOrderDetail) }
        ) {
            if (isActive) {
                Box(
                    modifier = Modifier
                        .width(4.dp)
                        .fillMaxHeight()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(PromoGradientStart, PromoGradientEnd)
                            )
                        )
                )
            }

            Column(modifier = Modifier.padding(14.dp).weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(MaterialTheme.shapes.medium)
                            .background(style.color.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = style.icon,
                            contentDescription = null,
                            tint = style.color,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = stringResource(R.string.order_card_title_no_id) + (order.id?.let { " #${it.takeLast(6)}" } ?: ""),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = if (isActive) FontWeight.ExtraBold else FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.weight(1f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            if (isActive) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primary)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = order.createdAt.toDisplayTime(),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    VoucherBadge(
                        label = style.label,
                        containerColor = style.color.copy(alpha = 0.9f)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
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
                                    order.previewFoodName ?: defaultFoodName,
                                    order.otherItemsCount
                                )
                            } else {
                                order.previewFoodName ?: defaultFoodName
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
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
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
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                Spacer(modifier = Modifier.height(12.dp))

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
                            if (order.status.canCustomerCancel) {
                                OutlinedButton(
                                    onClick = { onCancelClick?.invoke(order) },
                                    shape = MaterialTheme.shapes.medium,
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = ErrorRed),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                                ) {
                                    Text(stringResource(R.string.order_history_action_cancel), fontWeight = FontWeight.Bold)
                                }
                            } else {
                                Button(
                                    onClick = { order.id?.let(onNavigateToTracking) },
                                    shape = MaterialTheme.shapes.medium,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.primary,
                                        contentColor = Color.White
                                    ),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                                ) {
                                    Text(stringResource(R.string.order_history_action_track), fontWeight = FontWeight.Bold)
                                }
                            }
                        } else {
                            OutlinedButton(
                                onClick = { order.id?.let(onNavigateToOrderDetail) },
                                shape = MaterialTheme.shapes.medium,
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Text(stringResource(R.string.order_history_action_detail), fontWeight = FontWeight.Bold)
                            }

                            if (onReorder != null && order.status == OrderStatus.COMPLETED) {
                                Button(
                                    onClick = { order.id?.let(onReorder) },
                                    shape = MaterialTheme.shapes.medium,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.primary,
                                        contentColor = Color.White
                                    ),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                                ) {
                                    Text(stringResource(R.string.order_history_action_reorder), fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
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
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp,
        shadowElevation = 1.dp
    ) {
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

@Composable
private fun OrderStatus.toOrderStatusStyle(): OrderStatusStyle {
    return when (this) {
        OrderStatus.WAITING_PAYMENT -> OrderStatusStyle(stringResource(R.string.order_status_waiting_payment), AmberTertiary, Icons.Default.AccessTime)
        OrderStatus.WAITING_STORE_CONFIRMATION -> OrderStatusStyle(stringResource(R.string.order_status_waiting_confirmation), AmberTertiary, Icons.Default.AccessTime)
        OrderStatus.PREPARING -> OrderStatusStyle(stringResource(R.string.order_status_preparing), AmberTertiary, Icons.AutoMirrored.Filled.Assignment)
        OrderStatus.WAITING_SHIPPER -> OrderStatusStyle(stringResource(R.string.order_status_waiting_shipper), InfoBlue, Icons.Default.LocalShipping)
        OrderStatus.PICKING_UP -> OrderStatusStyle("Shipper đang lấy đơn", InfoBlue, Icons.Default.LocalShipping)
        OrderStatus.DELIVERING -> OrderStatusStyle(stringResource(R.string.order_status_delivering), InfoBlue, Icons.Default.LocalShipping)
        OrderStatus.COMPLETED -> OrderStatusStyle(stringResource(R.string.order_status_completed), SuccessGreen, Icons.Default.CheckCircle)
        OrderStatus.CANCELLED_BY_CUSTOMER -> OrderStatusStyle(stringResource(R.string.order_status_cancelled_by_customer), ErrorRed, Icons.Default.RemoveShoppingCart)
        OrderStatus.CANCELLED_BY_STORE -> OrderStatusStyle(stringResource(R.string.order_status_cancelled_by_store), ErrorRed, Icons.Default.RemoveShoppingCart)
        OrderStatus.NO_SHIPPER_FOUND -> OrderStatusStyle(stringResource(R.string.order_status_no_shipper_found), ErrorRed, Icons.Default.RemoveShoppingCart)
        OrderStatus.PAYMENT_FAILED -> OrderStatusStyle(stringResource(R.string.order_status_payment_failed), ErrorRed, Icons.Default.RemoveShoppingCart)
        OrderStatus.DELIVERY_TIMEOUT -> OrderStatusStyle(stringResource(R.string.order_status_delivery_timeout), ErrorRed, Icons.Default.RemoveShoppingCart)
    }
}

@Composable
private fun String?.toDisplayTime(): String {
    if (this.isNullOrBlank()) return stringResource(R.string.order_history_time_unknown)
    return if (length >= 16) substring(0, 16).replace("T", " ") else this
}