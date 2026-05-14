package com.example.foodienow.core.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.example.foodienow.core.designsystem.theme.BadgeTextStyle
import com.example.foodienow.core.designsystem.theme.FoodieNowTheme
import com.example.foodienow.core.designsystem.theme.WarningYellow

/**
 * A compact badge showing a star icon + rating value.
 *
 * @param rating The rating value to display (e.g. 4.5).
 * @param modifier Modifier for the root layout.
 * @param reviewCount Optional review count shown in parentheses.
 */
@Composable
fun RatingBadge(
    rating: Double,
    modifier: Modifier = Modifier,
    reviewCount: Int? = null
) {
    val spacing = FoodieNowTheme.spacing

    Row(
        modifier = modifier
            .clip(MaterialTheme.shapes.extraSmall)
            .background(WarningYellow.copy(alpha = 0.15f))
            .padding(horizontal = spacing.sm, vertical = spacing.xs),
        horizontalArrangement = Arrangement.spacedBy(spacing.xs),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Star,
            contentDescription = null,
            modifier = Modifier.size(14.dp),
            tint = WarningYellow
        )
        Text(
            text = buildString {
                append(String.format("%.1f", rating))
                if (reviewCount != null) append(" ($reviewCount)")
            },
            style = BadgeTextStyle,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}