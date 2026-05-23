package com.example.foodienow.core.designsystem.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddShoppingCart
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.foodienow.core.designsystem.theme.FoodieNowTheme

/**
 * Reusable food card component used across features.
 *
 * Horizontal layout: image (80dp) + info + optional action button.
 * Follows the FoodieNow design guideline spacing and elevation tokens.
 *
 * @param name Food name.
 * @param price Formatted price string (e.g. "45.000 ₫").
 * @param imageUrl Optional image URL loaded via Coil.
 * @param onClick Called when the card is tapped.
 * @param modifier Modifier for the root card.
 * @param rating Optional rating value to show as a badge.
 * @param description Optional description text shown below name.
 * @param onAddToCart Optional callback for the add-to-cart action button.
 */
@Composable
fun FoodCard(
    name: String,
    price: String,
    imageUrl: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    rating: Double? = null,
    description: String? = null,
    onAddToCart: (() -> Unit)? = null
) {
    val spacing = FoodieNowTheme.spacing
    val elevation = FoodieNowTheme.elevation

    ElevatedCard(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.elevatedCardElevation(
            defaultElevation = elevation.card
        )
    ) {
        Row(
            modifier = Modifier.padding(spacing.md),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FoodImage(
                imageUrl = imageUrl,
                contentDescription = name,
                modifier = Modifier
                    .size(88.dp)
            )

            Spacer(modifier = Modifier.width(spacing.lg))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                if (description != null) {
                    Spacer(modifier = Modifier.height(spacing.xs))
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(spacing.xs))

                Text(
                    text = price,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                if (rating != null && rating > 0) {
                    Spacer(modifier = Modifier.height(spacing.xs))
                    RatingBadge(rating = rating)
                }
            }

            if (onAddToCart != null) {
                IconButton(
                    onClick = onAddToCart,
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Icon(
                        Icons.Default.AddShoppingCart,
                        contentDescription = null
                    )
                }
            }
        }
    }
}
