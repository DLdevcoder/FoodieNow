package com.example.foodienow.feature.notification

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.CheckCircleOutline
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Moped
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material3.*
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
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.primary)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            stringResource(R.string.notifications_tab_title),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        if (uiState.unreadCount > 0) {
                            Text(
                                stringResource(R.string.notification_unread_count, uiState.unreadCount),
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                            )
                        }
                    }
                    if (uiState.unreadCount > 0) {
                        IconButton(
                            onClick = { viewModel.markAllAsRead() },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                Icons.Default.DoneAll,
                                contentDescription = stringResource(R.string.notification_mark_all_read),
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                }
                
                // Filter Tabs
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = uiState.filterType == NotificationFilter.ALL,
                        onClick = { viewModel.setFilter(NotificationFilter.ALL) },
                        label = { Text("Tất cả") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.onPrimary,
                            selectedLabelColor = MaterialTheme.colorScheme.primary
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            borderColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.3f),
                            selected = uiState.filterType == NotificationFilter.ALL,
                            enabled = true
                        )
                    )
                    FilterChip(
                        selected = uiState.filterType == NotificationFilter.UNREAD,
                        onClick = { viewModel.setFilter(NotificationFilter.UNREAD) },
                        label = { Text("Chưa đọc") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.onPrimary,
                            selectedLabelColor = MaterialTheme.colorScheme.primary
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            borderColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.3f),
                            selected = uiState.filterType == NotificationFilter.UNREAD,
                            enabled = true
                        )
                    )
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.sendTestNotification() },
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            ) {
                Icon(Icons.Default.Add, contentDescription = "Test Notification")
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.primary,
                            strokeWidth = 3.dp
                        )
                        Spacer(modifier = Modifier.height(spacing.lg))
                        Text(
                            stringResource(R.string.notification_loading),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            uiState.errorMessage != null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
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
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    EmptyState(
                        icon = Icons.Default.NotificationsNone,
                        title = stringResource(R.string.notification_empty_title),
                        subtitle = if (uiState.filterType == NotificationFilter.UNREAD) 
                            "Bạn không có thông báo nào chưa đọc" 
                            else stringResource(R.string.notification_empty_subtitle)
                    )
                }
            }

            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    itemsIndexed(
                        uiState.filteredNotifications,
                        key = { _, it -> it.id ?: it.hashCode() }
                    ) { index, notification ->
                        AnimatedVisibility(
                            visible = true,
                            enter = fadeIn(tween(300, delayMillis = index * 30)) +
                                    slideInVertically(tween(300, delayMillis = index * 30)) { it / 4 }
                        ) {
                            SwipeableNotificationCard(
                                notification = notification,
                                onMarkAsRead = { notification.id?.let { viewModel.markAsRead(it) } },
                                onDelete = { notification.id?.let { viewModel.deleteNotification(it) } }
                            )
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
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
                    if (!currentNotification.isRead) {
                        onMarkAsRead()
                    }
                    false // Don't actually dismiss the UI element if just marked as read
                }
                SwipeToDismissBoxValue.EndToStart -> {
                    onDelete()
                    true // Dismiss UI element
                }
                else -> false
            }
        }
    )

    SwipeToDismissBox(
        state = dismissState,
        enableDismissFromStartToEnd = !notification.isRead, // Only allow swipe right if unread
        enableDismissFromEndToStart = true, // Always allow swipe left to delete
        backgroundContent = {
            val direction = dismissState.dismissDirection
            val color by animateColorAsState(
                when (dismissState.targetValue) {
                    SwipeToDismissBoxValue.StartToEnd -> SuccessGreen
                    SwipeToDismissBoxValue.EndToStart -> ErrorRed
                    else -> Color.Transparent
                }, label = "swipe_bg"
            )
            
            val icon = when (direction) {
                SwipeToDismissBoxValue.StartToEnd -> Icons.Default.CheckCircleOutline
                SwipeToDismissBoxValue.EndToStart -> Icons.Default.DeleteOutline
                else -> Icons.Default.Archive
            }

            val alignment = when (direction) {
                SwipeToDismissBoxValue.StartToEnd -> Alignment.CenterStart
                SwipeToDismissBoxValue.EndToStart -> Alignment.CenterEnd
                else -> Alignment.Center
            }

            Box(
                Modifier
                    .fillMaxSize()
                    .padding(vertical = 4.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(color)
                    .padding(horizontal = 24.dp),
                contentAlignment = alignment
            ) {
                if (direction != null) {
                    Icon(
                        icon,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
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
    val isUnread = !notification.isRead
    val (iconBg, icon) = resolveNotificationStyle(notification)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onCardClick),
        colors = CardDefaults.cardColors(
            containerColor = if (isUnread)
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f)
            else
                MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Icon
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(iconBg.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = iconBg,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                // Title & Unread indicator
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        notification.title,
                        fontSize = 16.sp,
                        fontWeight = if (isUnread) FontWeight.Bold else FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    
                    if (isUnread) {
                        Box(
                            modifier = Modifier
                                .padding(top = 6.dp, start = 8.dp)
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Message
                Text(
                    notification.message,
                    fontSize = 14.sp,
                    color = if (isUnread) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Timestamp
                Text(
                    formatTimestamp(notification.createdAt),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

/**
 * Determine icon and color based on notification content.
 */
private fun resolveNotificationStyle(notification: AppNotification): Pair<Color, ImageVector> {
    val title = notification.title.lowercase()
    val message = notification.message.lowercase()

    return when {
        // Cần hành động ngay
        listOf("hết món", "không liên lạc", "không rõ", "thay đổi giá", "thất bại").any { title.contains(it) || message.contains(it) } ->
            ErrorRed to Icons.Default.WarningAmber

        // Vấn đề phát sinh
        listOf("trễ", "hủy đơn", "sự cố").any { title.contains(it) || message.contains(it) } ->
            WarningYellow to Icons.Default.ErrorOutline

        // Thanh toán & Hoàn tiền
        listOf("thanh toán", "hoàn tiền", "phí").any { title.contains(it) || message.contains(it) } ->
            Color(0xFF8B5CF6) to Icons.AutoMirrored.Filled.ReceiptLong

        // Khuyến mãi
        listOf("khuyến mãi", "giảm giá", "voucher", "freeship", "sale").any { title.contains(it) || message.contains(it) } ->
            SuccessGreen to Icons.Default.LocalOffer

        // Tài xế
        listOf("tài xế").any { title.contains(it) || message.contains(it) } ->
            InfoBlue to Icons.Default.Moped

        // Đơn hàng
        listOf("đã xác nhận", "chuẩn bị", "đã nhận đơn", "đang giao", "thành công", "đơn hàng").any { title.contains(it) || message.contains(it) } ->
            InfoBlue to Icons.Default.ShoppingBag

        else ->
            InfoBlue to Icons.Default.NotificationsNone
    }
}

private fun formatTimestamp(createdAt: String?): String {
    if (createdAt.isNullOrBlank()) return ""
    return try {
        val instant = java.time.Instant.parse(createdAt)
        val now = java.time.Instant.now()
        val duration = java.time.Duration.between(instant, now)
        when {
            duration.toMinutes() < 1 -> "Vừa xong"
            duration.toMinutes() < 60 -> "${duration.toMinutes()} phút trước"
            duration.toHours() < 24 -> "${duration.toHours()} giờ trước"
            duration.toDays() < 7 -> "${duration.toDays()} ngày trước"
            else -> createdAt.take(10)
        }
    } catch (_: Exception) {
        createdAt.take(16)
    }
}
