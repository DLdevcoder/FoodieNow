package com.example.foodienow.feature.notification

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import com.example.foodienow.core.designsystem.theme.SuccessGreen
import com.example.foodienow.core.designsystem.theme.WarningYellow
import com.example.foodienow.core.designsystem.theme.PromoGradientEnd
import com.example.foodienow.core.designsystem.theme.PromoGradientStart
import com.example.foodienow.domain.model.AppNotification
import com.example.foodienow.domain.model.UserRole
import java.time.Duration
import java.time.Instant

private data class NotificationStyle(
    val color: Color,
    val icon: ImageVector,
    val label: String
)

private data class RoleThemeConfig(
    val accentColor: Color,
    val secondaryColor: Color,
    val gradientColors: List<Color>
)

@Composable
private fun getRoleThemeConfig(role: UserRole?): RoleThemeConfig {
    val primary = MaterialTheme.colorScheme.primary
    return RoleThemeConfig(
        accentColor = primary,
        secondaryColor = MaterialTheme.colorScheme.primaryContainer,
        gradientColors = listOf(primary, MaterialTheme.colorScheme.primaryContainer)
    )
}

private data class CategoryChipData(
    val id: String,
    val label: String
)

private fun getCategoryChips(role: UserRole?): List<CategoryChipData> {
    return when (role) {
        UserRole.MERCHANT -> listOf(
            CategoryChipData("ALL", "Tất cả"),
            CategoryChipData("ORDER", "Đơn mới"),
            CategoryChipData("REVIEW", "Đánh giá"),
            CategoryChipData("CHAT", "Tin nhắn")
        )
        UserRole.SHIPPER -> listOf(
            CategoryChipData("ALL", "Tất cả"),
            CategoryChipData("TRIP", "Chuyến đi"),
            CategoryChipData("WALLET", "Thu nhập"),
            CategoryChipData("SYSTEM", "Hệ thống")
        )
        else -> listOf(
            CategoryChipData("ALL", "Tất cả"),
            CategoryChipData("ORDER", "Đơn hàng"),
            CategoryChipData("PROMO", "Khuyến mãi"),
            CategoryChipData("CHAT", "Tin nhắn")
        )
    }
}

private fun isNotificationInSubCategory(notification: AppNotification, role: UserRole?, category: String): Boolean {
    if (category == "ALL") return true
    val titleKey = notification.title
    val titleLower = notification.title.lowercase()
    val messageLower = notification.message.lowercase()
    
    return when (role) {
        UserRole.MERCHANT -> {
            when (category) {
                "ORDER" -> {
                    titleKey == "TXT_ORDER_NEW" ||
                    titleKey == "TXT_ORDER_PREPARING" ||
                    titleKey == "TXT_ORDER_DELIVERING" ||
                    titleKey == "TXT_ORDER_COMPLETED" ||
                    titleKey == "TXT_ORDER_CANCELLED" ||
                    listOf("hết món", "không liên lạc", "thay đổi giá", "thất bại", "hủy đơn", "sự cố", "xác nhận", "chuẩn bị", "đã nhận đơn", "thành công", "đơn hàng").any {
                        titleLower.contains(it) || messageLower.contains(it)
                    }
                }
                "REVIEW" -> {
                    titleKey == "TXT_NEW_REVIEW" || titleLower.contains("đánh giá") || messageLower.contains("đánh giá")
                }
                "CHAT" -> {
                    titleKey == "TXT_NEW_CHAT_MESSAGE" || titleLower.contains("tin nhắn") || messageLower.contains("tin nhắn")
                }
                else -> false
            }
        }
        UserRole.SHIPPER -> {
            when (category) {
                "TRIP" -> {
                    titleKey == "TXT_SHIPPER_NEW_ORDER" ||
                    titleKey == "TXT_ORDER_DRIVER_ASSIGNED" ||
                    titleKey == "TXT_ORDER_DELIVERING" ||
                    titleKey == "TXT_ORDER_COMPLETED" ||
                    titleKey == "TXT_ORDER_CANCELLED_SHIPPER" ||
                    listOf("tài xế", "shipper", "giao hàng", "chuyến đi", "hủy đơn", "đang giao").any {
                        titleLower.contains(it) || messageLower.contains(it)
                    }
                }
                "WALLET" -> {
                    titleKey == "TXT_PAYMENT_SUCCESS" ||
                    titleKey == "TXT_WALLET_TRANSACTION" ||
                    listOf("thanh toán", "tiền", "ví", "thu nhập", "phí", "hoàn tiền").any {
                        titleLower.contains(it) || messageLower.contains(it)
                    }
                }
                "SYSTEM" -> {
                    !titleKey.startsWith("TXT_") ||
                    titleKey == "TXT_NEW_CHAT_MESSAGE" ||
                    listOf("hệ thống", "cảnh báo", "tin tức", "news", "sự cố").any {
                        titleLower.contains(it) || messageLower.contains(it)
                    }
                }
                else -> false
            }
        }
        else -> {
            when (category) {
                "ORDER" -> {
                    titleKey == "TXT_ORDER_NEW" ||
                    titleKey == "TXT_ORDER_PREPARING" ||
                    titleKey == "TXT_ORDER_DELIVERING" ||
                    titleKey == "TXT_ORDER_COMPLETED" ||
                    titleKey == "TXT_ORDER_CANCELLED" ||
                    titleKey == "TXT_ORDER_DRIVER_ASSIGNED" ||
                    listOf("xác nhận", "chuẩn bị", "đã nhận đơn", "thành công", "đơn hàng", "tài xế", "shipper", "đang giao", "hủy đơn", "sự cố").any {
                        titleLower.contains(it) || messageLower.contains(it)
                    }
                }
                "PROMO" -> {
                    listOf("khuyến mãi", "giảm giá", "voucher", "freeship", "sale", "ưu đãi").any {
                        titleLower.contains(it) || messageLower.contains(it)
                    }
                }
                "CHAT" -> {
                    titleKey == "TXT_NEW_CHAT_MESSAGE" || titleLower.contains("tin nhắn") || messageLower.contains("tin nhắn")
                }
                else -> false
            }
        }
    }
}

@Composable
fun NotificationScreen(
    onBack: () -> Unit,
    onNavigateToDestination: (String) -> Unit = {},
    showBackButton: Boolean = true,
    viewModel: NotificationViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val roleTheme = getRoleThemeConfig(uiState.userRole)

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            NotificationHeader(
                unreadCount = uiState.unreadCount,
                totalCount = uiState.notifications.size,
                showBackButton = showBackButton,
                onBack = onBack,
                roleTheme = roleTheme,
                onMarkAllAsRead = viewModel::markAllAsRead,
                userRole = uiState.userRole
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
                roleTheme = roleTheme,
                onFilterSelected = viewModel::setFilter
            )

            NotificationCategoryFilterRow(
                selectedCategory = uiState.subFilter,
                role = uiState.userRole,
                notifications = uiState.notifications,
                roleTheme = roleTheme,
                onCategorySelected = viewModel::setSubFilter
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
                            top = 8.dp,
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
                                    roleTheme = roleTheme,
                                    onMarkAsRead = {
                                        notification.id?.let(viewModel::markAsRead)
                                    },
                                    onDelete = {
                                        notification.id?.let(viewModel::deleteNotification)
                                    },
                                    onNavigateToDestination = onNavigateToDestination
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NotificationHeader(
    unreadCount: Int,
    totalCount: Int,
    showBackButton: Boolean,
    onBack: () -> Unit,
    roleTheme: RoleThemeConfig,
    onMarkAllAsRead: () -> Unit,
    userRole: UserRole? = null
) {
    if (userRole == UserRole.MERCHANT || userRole == UserRole.SHIPPER) {
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
                    if (showBackButton) {
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier.size(38.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(R.string.common_back),
                                tint = Color.White
                            )
                        }
                    }

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
                            modifier = Modifier.size(27.dp),
                            tint = Color.White
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
                        IconButton(
                            onClick = onMarkAllAsRead,
                            modifier = Modifier.size(44.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.DoneAll,
                                contentDescription = stringResource(R.string.notification_mark_all_read),
                                tint = Color.White
                            )
                        }
                    }
                }
            }
        }
    } else {
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
                                    colors = roleTheme.gradientColors
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Column {
                        Text(
                            text = stringResource(R.string.notifications_tab_title),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (unreadCount > 0) {
                                stringResource(R.string.notification_unread_count, unreadCount)
                            } else {
                                stringResource(R.string.notification_all_caught_up)
                            },
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
            actions = {
                if (unreadCount > 0 && totalCount > 0) {
                    IconButton(
                        onClick = onMarkAllAsRead,
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DoneAll,
                            contentDescription = stringResource(R.string.notification_mark_all_read),
                            tint = roleTheme.accentColor
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
}

@Composable
private fun NotificationFilterPanel(
    selectedFilter: NotificationFilter,
    totalCount: Int,
    unreadCount: Int,
    roleTheme: RoleThemeConfig,
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
                roleTheme = roleTheme,
                modifier = Modifier.weight(1f),
                onClick = { onFilterSelected(NotificationFilter.ALL) }
            )
            NotificationSegment(
                selected = selectedFilter == NotificationFilter.UNREAD,
                label = stringResource(R.string.notification_filter_unread),
                count = unreadCount,
                roleTheme = roleTheme,
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
    roleTheme: RoleThemeConfig,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier
            .height(44.dp)
            .clickable(onClick = onClick),
        shape = MaterialTheme.shapes.large,
        color = if (selected) roleTheme.accentColor else Color.Transparent,
        contentColor = if (selected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
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
            CountPill(count = count, selected = selected, roleTheme = roleTheme)
        }
    }
}

@Composable
private fun CountPill(count: Int, selected: Boolean, roleTheme: RoleThemeConfig) {
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
private fun NotificationCategoryFilterRow(
    selectedCategory: String,
    role: UserRole?,
    notifications: List<AppNotification>,
    roleTheme: RoleThemeConfig,
    onCategorySelected: (String) -> Unit
) {
    val chips = getCategoryChips(role)
    LazyRow(
        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        itemsIndexed(chips) { _, chip ->
            val isSelected = selectedCategory == chip.id
            val hasUnread = notifications.any {
                !it.isRead && isNotificationInSubCategory(it, role, chip.id)
            }
            
            val backgroundColor by animateColorAsState(
                targetValue = if (isSelected) roleTheme.accentColor else MaterialTheme.colorScheme.surface,
                label = "chip_bg"
            )
            val textColor by animateColorAsState(
                targetValue = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                label = "chip_text"
            )
            val borderModifier = if (isSelected) {
                Modifier
            } else {
                Modifier.border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                    shape = CircleShape
                )
            }

            Surface(
                modifier = Modifier
                    .clip(CircleShape)
                    .clickable { onCategorySelected(chip.id) }
                    .then(borderModifier),
                color = backgroundColor,
                contentColor = textColor,
                shape = CircleShape
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = chip.label,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    )
                    if (hasUnread) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(if (isSelected) Color.White else roleTheme.accentColor)
                        )
                    }
                }
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
        tonalElevation = 0.dp
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
    roleTheme: RoleThemeConfig,
    onMarkAsRead: () -> Unit,
    onDelete: () -> Unit,
    onNavigateToDestination: (String) -> Unit
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
                roleTheme = roleTheme,
                onCardClick = {
                    if (!notification.isRead) onMarkAsRead()
                    NotificationLocalizationHelper.getDestinationRoute(notification)?.let(onNavigateToDestination)
                },
                onDelete = onDelete
            )
        }
    )
}

@Composable
private fun NotificationCardContent(
    notification: AppNotification,
    roleTheme: RoleThemeConfig,
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
            .clip(MaterialTheme.shapes.large),
        shape = MaterialTheme.shapes.large,
        color = if (isUnread) {
            roleTheme.accentColor.copy(alpha = 0.07f)
        } else {
            MaterialTheme.colorScheme.surface
        },
        tonalElevation = if (isUnread) 4.dp else 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Max)
                .clickable(onClick = onCardClick)
        ) {
            if (isUnread) {
                Box(
                    modifier = Modifier
                        .width(4.dp)
                        .fillMaxHeight()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(roleTheme.accentColor, roleTheme.secondaryColor)
                            )
                        )
                )
            }
            
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(14.dp)
            ) {
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
                                text = localizedTitle,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = if (isUnread) FontWeight.ExtraBold else FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.weight(1f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            if (isUnread) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(roleTheme.accentColor)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

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

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (style.label != "Thanh toán") {
                            VoucherBadge(
                                label = style.label,
                                containerColor = style.color.copy(alpha = 0.9f)
                            )
                        }
                        Text(
                            text = formatTimestamp(context, notification.createdAt),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        if (isUnread) {
                            Surface(
                                shape = CircleShape,
                                color = roleTheme.accentColor.copy(alpha = 0.1f),
                                contentColor = roleTheme.accentColor
                            ) {
                                Text(
                                    text = stringResource(R.string.notification_unread_label),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        IconButton(
                            onClick = onDelete,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteOutline,
                                contentDescription = stringResource(R.string.notification_delete),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun resolveNotificationStyle(notification: AppNotification): NotificationStyle {
    val titleKey = notification.title

    return when {
        titleKey == "TXT_ORDER_CANCELLED" || titleKey == "TXT_ORDER_CANCELLED_SHIPPER" ->
            NotificationStyle(ErrorRed, Icons.Default.WarningAmber, "Hủy đơn")

        titleKey == "TXT_PAYMENT_SUCCESS" || titleKey == "TXT_WALLET_TRANSACTION" ->
            NotificationStyle(Color(0xFF7C3AED), Icons.AutoMirrored.Filled.ReceiptLong, "Thanh toán")

        titleKey == "TXT_ORDER_NEW" ||
            titleKey == "TXT_ORDER_PREPARING" ||
            titleKey == "TXT_ORDER_DELIVERING" ||
            titleKey == "TXT_ORDER_COMPLETED" ->
            NotificationStyle(InfoBlue, Icons.Default.ShoppingBag, "Đơn hàng")

        titleKey == "TXT_ORDER_DRIVER_ASSIGNED" || titleKey == "TXT_SHIPPER_NEW_ORDER" ->
            NotificationStyle(InfoBlue, Icons.Default.Moped, "Giao hàng")

        titleKey == "TXT_NEW_CHAT_MESSAGE" ->
            NotificationStyle(AmberTertiary, Icons.Default.Campaign, "Tin nhắn")

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
