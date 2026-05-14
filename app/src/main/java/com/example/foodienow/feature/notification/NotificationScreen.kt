package com.example.foodienow.feature.notification

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
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
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.primary)
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
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.sendTestNotification() },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
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

            uiState.notifications.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    EmptyState(
                        icon = Icons.Default.NotificationsNone,
                        title = stringResource(R.string.notification_empty_title),
                        subtitle = stringResource(R.string.notification_empty_subtitle)
                    )
                }
            }

            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Section header
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                stringResource(R.string.notification_section_recent),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                letterSpacing = 0.5.sp
                            )
                            if (uiState.notifications.size > 0) {
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = MaterialTheme.colorScheme.primaryContainer
                                ) {
                                    Text(
                                        stringResource(R.string.notification_count_badge, uiState.notifications.size),
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }

                    itemsIndexed(
                        uiState.notifications,
                        key = { _, it -> it.id ?: it.hashCode() }
                    ) { index, notification ->
                        AnimatedVisibility(
                            visible = true,
                            enter = fadeIn(tween(300, delayMillis = index * 50)) +
                                    slideInVertically(tween(300, delayMillis = index * 50)) { it / 4 }
                        ) {
                            NotificationCard(
                                notification = notification,
                                onMarkAsRead = { notification.id?.let { viewModel.markAsRead(it) } },
                                onDelete = { notification.id?.let { viewModel.deleteNotification(it) } }
                            )
                        }
                    }

                    // Bottom spacer
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun NotificationCard(
    notification: AppNotification,
    onMarkAsRead: () -> Unit,
    onDelete: () -> Unit
) {
    val isUnread = !notification.isRead
    val (iconBg, icon) = resolveNotificationStyle(notification)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { if (isUnread) onMarkAsRead() },
        colors = CardDefaults.cardColors(
            containerColor = if (isUnread)
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            else
                MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isUnread) 2.dp else 0.5.dp
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Icon with gradient background
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                iconBg.copy(alpha = 0.15f),
                                iconBg.copy(alpha = 0.08f)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = iconBg,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                // Title row with unread dot
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        notification.title,
                        fontSize = 15.sp,
                        fontWeight = if (isUnread) FontWeight.Bold else FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (isUnread) {
                        Box(
                            modifier = Modifier
                                .padding(start = 8.dp)
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
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Footer: timestamp + actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Timestamp
                    Text(
                        formatTimestamp(notification.createdAt),
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.outline,
                        fontWeight = FontWeight.Normal
                    )

                    // Action buttons
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(0.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (isUnread) {
                            IconButton(
                                onClick = onMarkAsRead,
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    Icons.Default.CheckCircleOutline,
                                    contentDescription = stringResource(R.string.notification_mark_read),
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        IconButton(
                            onClick = onDelete,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                Icons.Default.DeleteOutline,
                                contentDescription = stringResource(R.string.notification_delete),
                                tint = MaterialTheme.colorScheme.outline,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Determine icon and color based on notification content.
 * Uses design system semantic colors instead of hardcoded hex values.
 */
private fun resolveNotificationStyle(notification: AppNotification): Pair<Color, ImageVector> {
    val title = notification.title.lowercase()
    val message = notification.message.lowercase()

    return when {
        // 2. Cần hành động ngay (Action required - High priority)
        listOf("hết món", "không liên lạc được", "không rõ", "thay đổi giá", "thất bại").any { title.contains(it) || message.contains(it) } ->
            ErrorRed to Icons.Default.WarningAmber

        // 3. Vấn đề phát sinh (Issues)
        listOf("trễ", "hủy đơn", "không tìm được", "sự cố", "thay đổi").any { title.contains(it) || message.contains(it) } ->
            WarningYellow to Icons.Default.ErrorOutline

        // 4. Thanh toán và hoàn tiền
        listOf("thanh toán", "hoàn tiền", "phí").any { title.contains(it) || message.contains(it) } ->
            Color(0xFF8B5CF6) to Icons.AutoMirrored.Filled.ReceiptLong

        // 6. Ưu đãi và khuyến mãi (Check before Order/Driver to catch promos first)
        listOf("khuyến mãi", "giảm giá", "ưu đãi", "voucher", "flash sale", "miễn phí").any { title.contains(it) || message.contains(it) } ->
            SuccessGreen to Icons.Default.LocalOffer

        // 5. Thông báo về tài xế
        listOf("tài xế").any { title.contains(it) || message.contains(it) } ->
            InfoBlue to Icons.Default.Moped

        // 1. Trạng thái đơn hàng (Order status)
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
