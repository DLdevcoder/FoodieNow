package com.example.foodienow.core.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import com.example.foodienow.core.designsystem.theme.AmberTertiary
import com.example.foodienow.core.designsystem.theme.BadgeTextStyle
import com.example.foodienow.core.designsystem.theme.ErrorRed
import com.example.foodienow.core.designsystem.theme.FoodieNowTheme
import com.example.foodienow.core.designsystem.theme.InfoBlue
import com.example.foodienow.core.designsystem.theme.StatusCancelledColor
import com.example.foodienow.core.designsystem.theme.StatusCompletedColor
import com.example.foodienow.core.designsystem.theme.StatusDeliveringColor
import com.example.foodienow.core.designsystem.theme.StatusPendingColor
import com.example.foodienow.core.designsystem.theme.SuccessGreen

/**
 * Predefined order status types with associated colors.
 */
enum class OrderStatus(
    val containerColor: Color,
    val contentColor: Color
) {
    PENDING(StatusPendingColor, AmberTertiary),
    PREPARING(StatusPendingColor, AmberTertiary),
    DELIVERING(StatusDeliveringColor, InfoBlue),
    COMPLETED(StatusCompletedColor, SuccessGreen),
    CANCELLED(StatusCancelledColor, ErrorRed)
}

/**
 * A small chip displaying an order status label with semantic colors.
 *
 * @param label The text to display (e.g. "Pending", "Delivering").
 * @param status The [OrderStatus] determining the chip's color scheme.
 * @param modifier Modifier for the root layout.
 */
@Composable
fun StatusChip(
    label: String,
    status: OrderStatus,
    modifier: Modifier = Modifier
) {
    val spacing = FoodieNowTheme.spacing

    Text(
        text = label,
        style = BadgeTextStyle,
        color = status.contentColor,
        modifier = modifier
            .clip(MaterialTheme.shapes.extraSmall)
            .background(status.containerColor)
            .padding(horizontal = spacing.sm, vertical = spacing.xs)
    )
}

/**
 * Overload accepting custom colors for non-standard statuses.
 */
@Composable
fun StatusChip(
    label: String,
    containerColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier
) {
    val spacing = FoodieNowTheme.spacing

    Text(
        text = label,
        style = BadgeTextStyle,
        color = contentColor,
        modifier = modifier
            .clip(MaterialTheme.shapes.extraSmall)
            .background(containerColor)
            .padding(horizontal = spacing.sm, vertical = spacing.xs)
    )
}