package com.example.foodienow.feature.notification

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.foodienow.R
import com.example.foodienow.core.designsystem.components.EmptyState
import com.example.foodienow.core.designsystem.theme.ErrorRed
import com.example.foodienow.core.designsystem.theme.FoodieNowTheme
import com.example.foodienow.core.designsystem.theme.InfoBlue
import com.example.foodienow.core.designsystem.theme.SuccessGreen
import com.example.foodienow.core.designsystem.theme.WarningYellow
import com.example.foodienow.domain.model.AppNotification
import java.time.Duration
import java.time.Instant

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationScreen(
    onBack: () -> Unit,
    viewModel: NotificationViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val spacing = FoodieNowTheme.spacing

    Scaffold(
        topBar = {
            NotificationHeader(
                unreadCount = uiState.unreadCount,
                totalCount = uiState.notifications.size,
                onMarkAllAsRead = { viewModel.markAllAsRead() }
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            NotificationFilterRow(
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
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        EmptyState(
                            icon = Icons.Default.NotificationsNone,
                            title = stringResource(R.string.notification_error_title),
                            subtitle = uiState.errorMessage,
                            actionLabel = stringResource(R.string.notification_error_retry),
                            onAction = { viewModel.loadNotifications() }
                        )
                    }
                }

                uiState.filteredNotifications.isEmpty() -> {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        EmptyState(
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
                            }
                        )
                    }
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentPadding = PaddingValues(
                            start = spacing.lg,
                            top = spacing.sm,
                            end = spacing.lg,
                            bottom = spacing.lg
                        ),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        itemsIndexed(
                            items = uiState.filteredNotifications,
                            key = { _, it -> it.id ?: it.hashCode() }
                        ) { index, notification ->
                            AnimatedVisibility(
                                visible = true,
                                enter = fadeIn(tween(220, delayMillis = index * 24)) +
                                    slideInVertically(tween(220, delayMillis = index * 24)) { it / 5 }
                            ) {
                                SwipeableNotificationCard(
                                    notification = notification,
                                    onMarkAsRead = {
                                        notification.id?.let { viewModel.markAsRead(it) }
                                    },
                                    onDelete = {
                                        notification.id?.let { viewModel.deleteNotification(it) }
                                    }
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
private fun NotificationHeader(
    unreadCount: Int,
    totalCount: Int,
    onMarkAllAsRead: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.notifications_tab_title),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = if (unreadCount > 0) {
                        stringResource(R.string.notification_unread_count, unreadCount)
                    } else {
                        stringResource(R.string.notification_all_caught_up)
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.82f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            if (unreadCount > 0 && totalCount > 0) {
                IconButton(
                    onClick = onMarkAllAsRead,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.12f))
                ) {
                    Icon(
                        imageVector = Icons.Default.DoneAll,
                        contentDescription = stringResource(R.string.notification_mark_all_read),
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }
    }
}

@Composable
private fun NotificationFilterRow(
    selectedFilter: NotificationFilter,
    totalCount: Int,
    unreadCount: Int,
    onFilterSelected: (NotificationFilter) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        NotificationFilterChip(
            selected = selectedFilter == NotificationFilter.ALL,
            label = stringResource(R.string.notification_filter_all),
            count = totalCount,
            modifier = Modifier.weight(1f),
            onClick = { onFilterSelected(NotificationFilter.ALL) }
        )
        NotificationFilterChip(
            selected = selectedFilter == NotificationFilter.UNREAD,
            label = stringResource(R.string.notification_filter_unread),
            count = unreadCount,
            modifier = Modifier.weight(1f),
            onClick = { onFilterSelected(NotificationFilter.UNREAD) }
        )
    }

    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.18f))
}

@Composable
private fun NotificationFilterChip(
    selected: Boolean,
    label: String,
    count: Int,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        modifier = modifier.height(40.dp),
        shape = RoundedCornerShape(8.dp),
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = MaterialTheme.colorScheme.primary,
            selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
            labelColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        label = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = label,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium
                )
                Spacer(modifier = Modifier.width(6.dp))
                CountPill(count = count, selected = selected)
            }
        }
    )
}

@Composable
private fun CountPill(count: Int, selected: Boolean) {
    Box(
        modifier = Modifier
            .defaultMinSize(minWidth = 22.dp)
            .height(20.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(
                if (selected) {
                    MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f)
                } else {
                    MaterialTheme.colorScheme.outline.copy(alpha = 0.12f)
                }
            )
            .padding(horizontal = 6.dp),
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
private fun LoadingNotifications(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(
            color = MaterialTheme.colorScheme.primary,
            strokeWidth = 3.dp
        )
        Spacer(modifier = Modifier.height(FoodieNowTheme.spacing.lg))
        Text(
            text = stringResource(R.string.notification_loading),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyMedium
        )
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
                    if (!currentNotification.isRead) {
                        onMarkAsRead()
                    }
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
                    .padding(vertical = 2.dp)
                    .clip(RoundedCornerShape(8.dp))
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
                onCardClick = { if (!notification.isRead) onMarkAsRead() }
            )
        }
    )
}

@Composable
private fun NotificationCardContent(
    notification: AppNotification,
    onCardClick: () -> Unit
) {
    val context = LocalContext.current
    val (localizedTitle, localizedMessage) = NotificationLocalizationHelper.getLocalizedNotification(context, notification)
    val isUnread = !notification.isRead
    val (iconBg, icon) = resolveNotificationStyle(notification)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onCardClick),
        colors = CardDefaults.cardColors(
            containerColor = if (isUnread) {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        border = BorderStroke(
            width = 1.dp,
            color = if (isUnread) {
                MaterialTheme.colorScheme.primary.copy(alpha = 0.24f)
            } else {
                MaterialTheme.colorScheme.outline.copy(alpha = 0.16f)
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isUnread) 2.dp else 1.dp),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(iconBg.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconBg,
                    modifier = Modifier.size(23.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = localizedTitle,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = if (isUnread) FontWeight.Bold else FontWeight.SemiBold,
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
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = localizedMessage,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = formatTimestamp(context, notification.createdAt),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    if (isUnread) {
                        UnreadPill()
                    }
                }
            }
        }
    }
}

@Composable
private fun UnreadPill() {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = stringResource(R.string.notification_unread_label),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold,
            maxLines = 1
        )
    }
}

private fun resolveNotificationStyle(notification: AppNotification): Pair<Color, ImageVector> {
    val titleKey = notification.title

    return when {
        titleKey == "TXT_ORDER_CANCELLED" ->
            ErrorRed to Icons.Default.WarningAmber

        titleKey == "TXT_PAYMENT_SUCCESS" || titleKey == "TXT_WALLET_TRANSACTION" ->
            Color(0xFF7C3AED) to Icons.AutoMirrored.Filled.ReceiptLong

        titleKey == "TXT_ORDER_NEW" ||
            titleKey == "TXT_ORDER_PREPARING" ||
            titleKey == "TXT_ORDER_DELIVERING" ||
            titleKey == "TXT_ORDER_COMPLETED" ->
            InfoBlue to Icons.Default.ShoppingBag

        titleKey == "TXT_NEW_REVIEW" ->
            SuccessGreen to Icons.Default.CheckCircleOutline

        else -> {
            val title = notification.title.lowercase()
            val message = notification.message.lowercase()
            when {
                listOf("hết món", "không liên lạc", "không rõ", "thay đổi giá", "thất bại").any {
                    title.contains(it) || message.contains(it)
                } ->
                    ErrorRed to Icons.Default.WarningAmber

                listOf("trễ", "hủy đơn", "sự cố").any { title.contains(it) || message.contains(it) } ->
                    WarningYellow to Icons.Default.ErrorOutline

                listOf("thanh toán", "hoàn tiền", "phí").any { title.contains(it) || message.contains(it) } ->
                    Color(0xFF7C3AED) to Icons.AutoMirrored.Filled.ReceiptLong

                listOf("khuyến mãi", "giảm giá", "voucher", "freeship", "sale").any {
                    title.contains(it) || message.contains(it)
                } ->
                    SuccessGreen to Icons.Default.LocalOffer

                listOf("tài xế", "shipper").any { title.contains(it) || message.contains(it) } ->
                    InfoBlue to Icons.Default.Moped

                listOf("xác nhận", "chuẩn bị", "đã nhận đơn", "đang giao", "thành công", "đơn hàng").any {
                    title.contains(it) || message.contains(it)
                } ->
                    InfoBlue to Icons.Default.ShoppingBag

                listOf("tin tức", "news").any { title.contains(it) || message.contains(it) } ->
                    WarningYellow to Icons.Default.Campaign

                else ->
                    InfoBlue to Icons.Default.NotificationsNone
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
