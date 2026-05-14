package com.example.foodienow.core.designsystem.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextDecoration
import com.example.foodienow.core.designsystem.theme.FoodieNowTheme
import com.example.foodienow.core.designsystem.theme.PriceDisplayStyle
import com.example.foodienow.core.designsystem.theme.PriceStrikethroughStyle
import java.text.NumberFormat
import java.util.Locale

/**
 * Displays a price with optional original (strikethrough) price.
 *
 * @param currentPrice The current/discounted price to display prominently.
 * @param originalPrice Optional original price shown with strikethrough when different from currentPrice.
 * @param modifier Modifier for the root layout.
 * @param currentPriceColor Color of the current price text. Defaults to primary.
 */
@Composable
fun PriceTag(
    currentPrice: Long,
    modifier: Modifier = Modifier,
    originalPrice: Long? = null,
    currentPriceColor: Color = MaterialTheme.colorScheme.primary
) {
    val formatter = remember { NumberFormat.getCurrencyInstance(Locale("vi", "VN")) }
    val spacing = FoodieNowTheme.spacing

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(spacing.sm),
        verticalAlignment = Alignment.Bottom
    ) {
        Text(
            text = formatter.format(currentPrice),
            style = PriceDisplayStyle,
            color = currentPriceColor
        )
        if (originalPrice != null && originalPrice > currentPrice) {
            Text(
                text = formatter.format(originalPrice),
                style = PriceStrikethroughStyle.copy(textDecoration = TextDecoration.LineThrough),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}