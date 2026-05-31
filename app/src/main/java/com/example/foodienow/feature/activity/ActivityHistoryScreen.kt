@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.example.foodienow.feature.activity

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.example.foodienow.core.designsystem.components.FoodieEmptyState
import com.example.foodienow.core.designsystem.components.VoucherBadge
import com.example.foodienow.core.designsystem.components.shimmerEffect
import com.example.foodienow.core.designsystem.theme.FoodieNowTheme
import com.example.foodienow.core.designsystem.theme.InfoBlue
import com.example.foodienow.core.designsystem.theme.SuccessGreen
import com.example.foodienow.domain.model.PaymentMethod
import com.example.foodienow.domain.model.WalletProvider
import com.example.foodienow.domain.model.WalletTransactionType
import com.example.foodienow.feature.customer_home.components.formatPrice

private enum class ActivityFilter(
    val titleRes: Int
) {
    ALL(R.string.activity_filter_all),
    ORDERS(R.string.activity_filter_orders),
    FINANCE(R.string.activity_filter_finance),
    REVIEWS(R.string.activity_filter_reviews)
}

@Composable
fun ActivityHistoryScreen(
    onBack: () -> Unit,
    viewModel: ActivityHistoryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedFilter by remember { mutableStateOf(ActivityFilter.ALL) }

    val filteredItems = remember(uiState.items, selectedFilter) {
        when (selectedFilter) {
            ActivityFilter.ALL -> uiState.items
            ActivityFilter.ORDERS -> uiState.items.filter { it.type == ActivityType.ORDER }
            ActivityFilter.FINANCE -> uiState.items.filter { it.type == ActivityType.PAYMENT || it.type == ActivityType.WALLET_TRANSACTION }
            ActivityFilter.REVIEWS -> uiState.items.filter { it.type == ActivityType.REVIEW }
        }
    }

    val counts = remember(uiState.items) {
        mapOf(
            ActivityFilter.ALL to uiState.items.size,
            ActivityFilter.ORDERS to uiState.items.count { it.type == ActivityType.ORDER },
            ActivityFilter.FINANCE to uiState.items.count { it.type == ActivityType.PAYMENT || it.type == ActivityType.WALLET_TRANSACTION },
            ActivityFilter.REVIEWS to uiState.items.count { it.type == ActivityType.REVIEW }
        )
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            ActivityHistoryHeader(
                totalCount = uiState.items.size,
                onBack = onBack
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(com.example.foodienow.core.designsystem.theme.FoodieCream)
        ) {
            ActivityFilterPanel(
                selectedFilter = selectedFilter,
                counts = counts,
                onFilterSelected = { selectedFilter = it }
            )

            when {
                uiState.isLoading -> {
                    LoadingActivityHistory(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                    )
                }
                filteredItems.isEmpty() -> {
                    FoodieEmptyState(
                        icon = Icons.Default.History,
                        title = stringResource(R.string.activity_history_empty),
                        subtitle = stringResource(R.string.activity_history_subtitle_empty),
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                    )
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentPadding = PaddingValues(
                            start = 18.dp,
                            top = 16.dp,
                            end = 18.dp,
                            bottom = 24.dp
                        ),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item {
                            SectionHeader(
                                title = stringResource(R.string.activity_history_section_recent),
                                countLabel = stringResource(
                                    R.string.activity_history_count_badge,
                                    filteredItems.size
                                )
                            )
                        }
                        items(filteredItems, key = { it.id }) { item ->
                            ActivityHistoryCard(item = item)
                        }
                    }
                }
            }

            uiState.errorResId?.let { resId ->
                Text(
                    text = stringResource(resId),
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier
                        .padding(horizontal = 18.dp)
                        .padding(bottom = 12.dp)
                )
            }
        }
    }
}

@Composable
private fun ActivityHistoryHeader(
    totalCount: Int,
    onBack: () -> Unit
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
                            com.example.foodienow.core.designsystem.theme.PromoGradientStart,
                            MaterialTheme.colorScheme.primary,
                            com.example.foodienow.core.designsystem.theme.PromoGradientEnd
                        )
                    )
                )
                .statusBarsPadding()
                .padding(start = 6.dp, top = 16.dp, end = 18.dp, bottom = 18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.size(44.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(MaterialTheme.shapes.large)
                        .background(Color.White.copy(alpha = 0.18f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(26.dp)
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.activity_history_title),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = if (totalCount > 0) {
                            stringResource(R.string.activity_history_subtitle_count, totalCount)
                        } else {
                            stringResource(R.string.activity_history_subtitle_empty)
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.84f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
private fun ActivityFilterPanel(
    selectedFilter: ActivityFilter,
    counts: Map<ActivityFilter, Int>,
    onFilterSelected: (ActivityFilter) -> Unit
) {
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
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ActivityFilter.values().forEach { filter ->
                ActivityFilterSegment(
                    selected = selectedFilter == filter,
                    label = stringResource(filter.titleRes),
                    count = counts[filter] ?: 0,
                    modifier = Modifier.weight(1f),
                    onClick = { onFilterSelected(filter) }
                )
            }
        }
    }
}

@Composable
private fun ActivityFilterSegment(
    selected: Boolean,
    label: String,
    count: Int,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier
            .height(44.dp)
            .clickable(onClick = onClick),
        shape = MaterialTheme.shapes.large,
        color = if (selected) MaterialTheme.colorScheme.primary else Color.Transparent,
        contentColor = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 6.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (count > 0) {
                Spacer(modifier = Modifier.width(4.dp))
                CountPill(count = count, selected = selected)
            }
        }
    }
}

@Composable
private fun CountPill(count: Int, selected: Boolean) {
    Box(
        modifier = Modifier
            .defaultMinSize(minWidth = 18.dp)
            .height(18.dp)
            .clip(CircleShape)
            .background(
                if (selected) {
                    MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f)
                } else {
                    MaterialTheme.colorScheme.surfaceVariant
                }
            )
            .padding(horizontal = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = count.toString(),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = if (selected) {
                MaterialTheme.colorScheme.onPrimary
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
            maxLines = 1
        )
    }
}

@Composable
private fun ActivityHistoryCard(item: ActivityHistoryItem) {
    val title = when (item.type) {
        ActivityType.ORDER -> stringResource(R.string.order_card_title_no_id)
        ActivityType.PAYMENT -> stringResource(R.string.activity_history_payment_title_no_id)
        ActivityType.REVIEW -> {
            if (!item.foodName.isNullOrBlank()) {
                stringResource(R.string.activity_history_review_title_with_food, item.foodName)
            } else {
                stringResource(R.string.activity_history_review_title)
            }
        }
        ActivityType.WALLET_TRANSACTION -> {
            when (item.transactionType) {
                WalletTransactionType.TOP_UP -> stringResource(R.string.activity_history_wallet_top_up_title)
                WalletTransactionType.REFUND -> stringResource(R.string.activity_history_wallet_refund_title)
                else -> stringResource(R.string.activity_history_wallet_payment_title)
            }
        }
    }

    val subtitle = when (item.type) {
        ActivityType.ORDER -> stringResource(
            R.string.activity_history_item_subtitle,
            item.status ?: "-",
            item.totalPrice?.formatPrice() ?: "-"
        )
        ActivityType.PAYMENT -> stringResource(
            R.string.activity_history_payment_subtitle_no_id,
            resolvePaymentMethodLabel(item.method, item.provider),
            item.status ?: "-"
        )
        ActivityType.REVIEW -> stringResource(
            R.string.activity_history_review_subtitle,
            item.rating ?: 0,
            item.comment ?: ""
        )
        ActivityType.WALLET_TRANSACTION -> {
            val amountFormatted = if (item.transactionType == WalletTransactionType.TOP_UP ||
                item.transactionType == WalletTransactionType.REFUND) {
                "+" + (item.amount?.formatPrice() ?: "-")
            } else {
                "-" + (item.amount?.formatPrice() ?: "-")
            }
            stringResource(
                R.string.activity_history_wallet_subtitle,
                (item.description ?: "")
                    .replace("Thanh toan don hang", "Thanh toán đơn hàng", ignoreCase = true)
                    .replace("Nhan thanh toan don hang", "Nhận thanh toán đơn hàng", ignoreCase = true)
                    .replace("Thanh toan Merchant don hang", "Thanh toán Merchant đơn hàng", ignoreCase = true)
                    .replace("Thanh toan Shipper don hang", "Thanh toán Shipper đơn hàng", ignoreCase = true)
                    .replace("Thanh toan", "Thanh toán", ignoreCase = true)
                    .replace("don hang", "đơn hàng", ignoreCase = true)
                    .replace("Nhan thanh toan", "Nhận thanh toán", ignoreCase = true),
                amountFormatted
            )
        }
    }

    val styleColor = when (item.type) {
        ActivityType.ORDER -> InfoBlue
        ActivityType.PAYMENT -> Color(0xFF7C3AED)
        ActivityType.WALLET_TRANSACTION -> Color(0xFF7C3AED)
        ActivityType.REVIEW -> SuccessGreen
    }

    val categoryLabel = when (item.type) {
        ActivityType.ORDER -> stringResource(R.string.activity_filter_orders)
        ActivityType.PAYMENT -> stringResource(R.string.activity_filter_finance)
        ActivityType.WALLET_TRANSACTION -> stringResource(R.string.activity_filter_finance)
        ActivityType.REVIEW -> stringResource(R.string.activity_filter_reviews)
    }

    FoodieCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(MaterialTheme.shapes.large)
                        .background(styleColor.copy(alpha = 0.14f)),
                    contentAlignment = Alignment.Center
                ) {
                    ActivityIcon(type = item.type, transactionType = item.transactionType, tint = styleColor)
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(5.dp))
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.16f))
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                VoucherBadge(
                    label = categoryLabel,
                    containerColor = styleColor.copy(alpha = 0.9f)
                )
                Text(
                    text = item.createdAt ?: stringResource(R.string.activity_history_time_unknown),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun ActivityIcon(type: ActivityType, transactionType: WalletTransactionType?, tint: Color) {
    val icon = when (type) {
        ActivityType.ORDER -> Icons.Default.ShoppingCart
        ActivityType.PAYMENT -> Icons.Default.Payments
        ActivityType.REVIEW -> Icons.Default.Star
        ActivityType.WALLET_TRANSACTION -> Icons.Default.AccountBalanceWallet
    }
    Icon(
        imageVector = icon,
        contentDescription = null,
        tint = tint,
        modifier = Modifier.size(24.dp)
    )
}

@Composable
private fun resolvePaymentMethodLabel(
    method: PaymentMethod?,
    provider: WalletProvider?
): String {
    return when (method) {
        PaymentMethod.COD -> stringResource(R.string.payment_method_cod)
        PaymentMethod.WALLET -> {
            val providerLabel = when (provider) {
                WalletProvider.ZALOPAY -> stringResource(R.string.payment_wallet_provider_zalopay)
                WalletProvider.MOMO -> stringResource(R.string.payment_wallet_provider_momo)
                WalletProvider.VNPAY -> stringResource(R.string.payment_wallet_provider_vnpay)
                WalletProvider.PAYPAL -> stringResource(R.string.payment_wallet_provider_paypal)
                null -> stringResource(R.string.payment_method_wallet)
            }
            if (provider == null) {
                stringResource(R.string.payment_method_wallet)
            } else {
                stringResource(R.string.payment_wallet_method_with_provider, providerLabel)
            }
        }
        PaymentMethod.FOODIE_PAY -> stringResource(R.string.payment_method_foodie_pay)
        null -> "-"
    }
}

@Composable
private fun LoadingActivityHistory(modifier: Modifier = Modifier) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(
            start = 18.dp,
            top = 16.dp,
            end = 18.dp,
            bottom = 24.dp
        ),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(5) {
            ActivityHistorySkeletonCard()
        }
    }
}

@Composable
private fun ActivityHistorySkeletonCard() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = FoodieNowTheme.elevation.card
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(MaterialTheme.shapes.large)
                        .shimmerEffect()
                )
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.6f)
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
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.16f))
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .width(70.dp)
                        .height(20.dp)
                        .clip(MaterialTheme.shapes.small)
                        .shimmerEffect()
                )
                Box(
                    modifier = Modifier
                        .width(90.dp)
                        .height(14.dp)
                        .clip(MaterialTheme.shapes.small)
                        .shimmerEffect()
                )
            }
        }
    }
}

@Composable
private fun SectionHeader(
    title: String,
    countLabel: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onBackground
        )
        VoucherBadge(
            label = countLabel,
            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.9f)
        )
    }
}
