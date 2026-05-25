package com.example.foodienow.feature.notification

import android.content.Context
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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.CheckCircleOutline
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.Moped
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.foodienow.R
import com.example.foodienow.core.designsystem.components.FoodieEmptyState
import com.example.foodienow.core.designsystem.components.FoodieErrorState
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
import com.example.foodienow.core.designsystem.theme.WarningYellow
import com.example.foodienow.domain.model.AppNotification
import java.time.Duration
import java.time.Instant

private data class NotificationStyle(
    val color: Color,
    val icon: ImageVector,
    val label: String
)

@Composable
fun NotificationScreen(
    onBack: () -> Unit,
    viewModel: NotificationViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            NotificationHeader(
                unreadCount = uiState.unreadCount,
                totalCount = uiState.notifications.size,
                onMarkAllAsRead = viewModel::markAllAsRead
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(FoodieCream)
        ) {
            NotificationFilterPanel(
                selectedFilter = uiState.filterType,
                totalCount = uiState.notifications.size,
                unreadCount = uiState.unreadCount,
                onFilterSelected = viewModel::setFilter
            )

            when {
                uiState.isLoading -> {
                    LoadingNotifications(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                    )
                }

                uiState.errorMessage != null -> {
                    FoodieErrorState(
                        title = stringResource(R.string.notification_error_title),
                        subtitle = uiState.errorMessage.orEmpty(),
                        actionLabel = stringResource(R.string.notification_error_retry),
                        onAction = viewModel::loadNotifications,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                    )
                }

                uiState.filteredNotifications.isEmpty() -> {
                    FoodieEmptyState(
                        icon = Icons.Default.NotificationsNone,
                        title = if (uiState.filterType == NotificationFilter.UNREAD) {
                            stringResource(R.string.notification_empty_unread_title)
                        } else {
                            stringResource(R.string.notification_empty_title)
                        },
                        subtitle = if (uiState.filterType == NotificationFilter.UNREAD) {
                            stringResource(R.string.notification_empty_unread_subtitle)
                        } else {
                            stringResource(R.string.notification_empty_subtitle)
                        },
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
                                title = stringResource(R.string.notification_section_recent),
                                countLabel = stringResource(
                                    R.string.notification_count_badge,
                                    uiState.filteredNotifications.size
                                )
                            )
                        }

                        itemsIndexed(
                            items = uiState.filteredNotifications,
                            key = { _, notification -> notification.id ?: notification.hashCode() }
                        ) { index, notification ->
                            AnimatedVisibility(
                                visible = true,
                                enter = fadeIn(tween(200, delayMillis = index.coerceAtMost(8) * 18)) +
                                    slideInVertically(tween(200, delayMillis = index.coerceAtMost(8) * 18)) { it / 5 }
                            ) {
                                SwipeableNotificationCard(
                                    notification = notification,
                                    onMarkAsRead = {
                                        notification.id?.let(viewModel::markAsRead)
                                    },
                                    onDelete = {
                                        notification.id?.let(viewModel::deleteNotification)
                                    }
                                )
                            }
                        }

                        item {
                            Spacer(modifier = Modifier.navigationBarsPadding())
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun NotificationHeader(
    unreadCount: Int,
    totalCount: Int,
    onMarkAllAsRead: () -> Unit
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
                        imageVector = Icons.Default.Notifications,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.notifications_tab_title),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = if (unreadCount > 0) {
                            stringResource(R.string.notification_unread_count, unreadCount)
                        } else {
                            stringResource(R.string.notification_all_caught_up)
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.84f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                if (unreadCount > 0 && totalCount > 0) {
                    Surface(
                        shape = CircleShape,
                        color = Color.White.copy(alpha = 0.18f),
                        contentColor = Color.White
                    ) {
                        IconButton(
                            onClick = onMarkAllAsRead,
                            modifier = Modifier.size(44.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.DoneAll,
                                contentDescription = stringResource(R.string.notification_mark_all_read)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun NotificationFilterPanel(
    selectedFilter: NotificationFilter,
    totalCount: Int,
    unreadCount: Int,
    onFilterSelected: (NotificationFilter) -> Unit
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
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            NotificationSegment(
                selected = selectedFilter == NotificationFilter.ALL,
                label = stringResource(R.string.notification_filter_all),
                count = totalCount,
                modifier = Modifier.weight(1f),
                onClick = { onFilterSelected(NotificationFilter.ALL) }
            )
            NotificationSegment(
                selected = selectedFilter == NotificationFilter.UNREAD,
                label = stringResource(R.string.notification_filter_unread),
                count = unreadCount,
                modifier = Modifier.weight(1f),
                onClick = { onFilterSelected(NotificationFilter.UNREAD) }
            )
        }
    }
}

@Composable
private fun NotificationSegment(
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
            modifier = Modifier.padding(horizontal = 12.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.width(7.dp))
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
                    MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f)
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
                MaterialTheme.colorScheme.onPrimary
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
            maxLines = 1
        )
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

@Composable
private fun LoadingNotifications(modifier: Modifier = Modifier) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(18.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(5) {
            NotificationSkeletonCard()
        }
    }
}

@Composable
private fun NotificationSkeletonCard() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = FoodieNowTheme.elevation.card
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
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
                        .fillMaxWidth(0.72f)
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
                        .fillMaxWidth(0.42f)
                        .height(14.dp)
                        .clip(MaterialTheme.shapes.small)
                        .shimmerEffect()
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwipeableNotificationCard(
    notification: AppNotification,
    onMarkAsRead: () -> Unit,
    onDelete: () -> Unit
) {
    val currentNotification by rememberUpdatedState(notification)

    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { dismissValue ->
            when (dismissValue) {
                SwipeToDismissBoxValue.StartToEnd -> {
                    if (!currentNotification.isRead) onMarkAsRead()
                    false
                }

                SwipeToDismissBoxValue.EndToStart -> {
                    onDelete()
                    true
                }

                else -> false
            }
        }
    )

    SwipeToDismissBox(
        state = dismissState,
        enableDismissFromStartToEnd = !notification.isRead,
        enableDismissFromEndToStart = true,
        backgroundContent = {
            val direction = dismissState.dismissDirection
            val color by animateColorAsState(
                targetValue = when (dismissState.targetValue) {
                    SwipeToDismissBoxValue.StartToEnd -> SuccessGreen
                    SwipeToDismissBoxValue.EndToStart -> ErrorRed
                    else -> Color.Transparent
                },
                label = "notification_swipe_background"
            )
            val icon = when (direction) {
                SwipeToDismissBoxValue.StartToEnd -> Icons.Default.CheckCircleOutline
                SwipeToDismissBoxValue.EndToStart -> Icons.Default.DeleteOutline
                else -> Icons.Default.DeleteOutline
            }
            val alignment = when (direction) {
                SwipeToDismissBoxValue.StartToEnd -> Alignment.CenterStart
                SwipeToDismissBoxValue.EndToStart -> Alignment.CenterEnd
                else -> Alignment.Center
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(MaterialTheme.shapes.large)
                    .background(color)
                    .padding(horizontal = 22.dp),
                contentAlignment = alignment
            ) {
                if (direction != SwipeToDismissBoxValue.Settled) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(26.dp)
                    )
                }
            }
        },
        content = {
            NotificationCardContent(
                notification = notification,
                onCardClick = { if (!notification.isRead) onMarkAsRead() },
                onDelete = onDelete
            )
        }
    )
}

@Composable
private fun NotificationCardContent(
    notification: AppNotification,
    onCardClick: () -> Unit,
    onDelete: () -> Unit
) {
    val context = LocalContext.current
    val (localizedTitle, localizedMessage) = NotificationLocalizationHelper.getLocalizedNotification(context, notification)
    val isUnread = !notification.isRead
    val style = resolveNotificationStyle(notification)

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onCardClick),
        shape = MaterialTheme.shapes.large,
        color = if (isUnread) {
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.48f)
        } else {
            MaterialTheme.colorScheme.surface
        },
        tonalElevation = FoodieNowTheme.elevation.card,
        shadowElevation = if (isUnread) 2.dp else 1.dp
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
                        .background(style.color.copy(alpha = 0.14f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = style.icon,
                        contentDescription = null,
                        tint = style.color,
                        modifier = Modifier.size(25.dp)
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = localizedTitle,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = if (isUnread) FontWeight.ExtraBold else FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (isUnread) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .padding(top = 6.dp)
                                    .size(9.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(5.dp))

                    Text(
                        text = localizedMessage,
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
                    label = style.label,
                    containerColor = style.color.copy(alpha = 0.9f)
                )
                Text(
                    text = formatTimestamp(context, notification.createdAt),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                if (isUnread) {
                    UnreadPill()
                }
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(34.dp)
                ) {
                    Icon(
                        Icons.Default.DeleteOutline,
                        contentDescription = stringResource(R.string.notification_delete),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun UnreadPill() {
    Surface(
        shape = CircleShape,
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.11f),
        contentColor = MaterialTheme.colorScheme.primary
    ) {
        Text(
            text = stringResource(R.string.notification_unread_label),
            modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            maxLines = 1
        )
    }
}

@Composable
private fun resolveNotificationStyle(notification: AppNotification): NotificationStyle {
    val titleKey = notification.title

    return when {
        titleKey == "TXT_ORDER_CANCELLED" ->
            NotificationStyle(ErrorRed, Icons.Default.WarningAmber, "Hủy đơn")

        titleKey == "TXT_PAYMENT_SUCCESS" || titleKey == "TXT_WALLET_TRANSACTION" ->
            NotificationStyle(Color(0xFF7C3AED), Icons.AutoMirrored.Filled.ReceiptLong, "Thanh toán")

        titleKey == "TXT_ORDER_NEW" ||
            titleKey == "TXT_ORDER_PREPARING" ||
            titleKey == "TXT_ORDER_DELIVERING" ||
            titleKey == "TXT_ORDER_COMPLETED" ->
            NotificationStyle(InfoBlue, Icons.Default.ShoppingBag, "Đơn hàng")

        titleKey == "TXT_NEW_REVIEW" ->
            NotificationStyle(SuccessGreen, Icons.Default.CheckCircleOutline, "Đánh giá")

        else -> {
            val title = notification.title.lowercase()
            val message = notification.message.lowercase()
            when {
                listOf("hết món", "không liên lạc", "thay đổi giá", "thất bại").any {
                    title.contains(it) || message.contains(it)
                } ->
                    NotificationStyle(ErrorRed, Icons.Default.WarningAmber, "Cần xử lý")

                listOf("trễ", "hủy đơn", "sự cố").any { title.contains(it) || message.contains(it) } ->
                    NotificationStyle(WarningYellow, Icons.Default.ErrorOutline, "Cảnh báo")

                listOf("thanh toán", "hoàn tiền", "phí", "ví").any { title.contains(it) || message.contains(it) } ->
                    NotificationStyle(Color(0xFF7C3AED), Icons.AutoMirrored.Filled.ReceiptLong, "Thanh toán")

                listOf("khuyến mãi", "giảm giá", "voucher", "freeship", "sale").any {
                    title.contains(it) || message.contains(it)
                } ->
                    NotificationStyle(SuccessGreen, Icons.Default.LocalOffer, "Ưu đãi")

                listOf("tài xế", "shipper", "đang giao").any { title.contains(it) || message.contains(it) } ->
                    NotificationStyle(InfoBlue, Icons.Default.Moped, "Giao hàng")

                listOf("xác nhận", "chuẩn bị", "đã nhận đơn", "thành công", "đơn hàng").any {
                    title.contains(it) || message.contains(it)
                } ->
                    NotificationStyle(InfoBlue, Icons.Default.ShoppingBag, "Đơn hàng")

                listOf("tin tức", "news").any { title.contains(it) || message.contains(it) } ->
                    NotificationStyle(AmberTertiary, Icons.Default.Campaign, "Tin tức")

                else ->
                    NotificationStyle(InfoBlue, Icons.Default.NotificationsNone, "Hệ thống")
            }
        }
    }
}

private fun formatTimestamp(context: Context, createdAt: String?): String {
    if (createdAt.isNullOrBlank()) return ""

    return try {
        val instant = Instant.parse(createdAt)
        val duration = Duration.between(instant, Instant.now())

        when {
            duration.toMinutes() < 1 ->
                context.getString(R.string.notification_time_just_now)

            duration.toMinutes() < 60 ->
                context.getString(R.string.notification_time_minutes_ago, duration.toMinutes())

            duration.toHours() < 24 ->
                context.getString(R.string.notification_time_hours_ago, duration.toHours())

            duration.toDays() < 7 ->
                context.getString(R.string.notification_time_days_ago, duration.toDays())

            else -> createdAt.take(10)
        }
    } catch (_: Exception) {
        createdAt.take(16)
    }
}
