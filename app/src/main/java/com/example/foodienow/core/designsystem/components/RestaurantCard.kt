package com.example.foodienow.core.designsystem.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.example.foodienow.core.designsystem.theme.FoodieNowTheme
import com.example.foodienow.core.designsystem.theme.ShapeImage

/**
 * Reusable restaurant/store card component.
 *
 * Vertical layout: cover image + name + address + rating.
 *
 * @param name Store name.
 * @param imageUrl Cover image URL.
 * @param onClick Called when card is tapped.
 * @param modifier Modifier.
 * @param address Optional address text.
 * @param rating Optional rating to show via [RatingBadge].
 * @param reviewCount Optional review count shown next to rating.
 */
@Composable
fun RestaurantCard(
    name: String,
    imageUrl: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    address: String? = null,
    rating: Double? = null,
    reviewCount: Int? = null
) {
    val spacing = FoodieNowTheme.spacing
    val elevation = FoodieNowTheme.elevation

    ElevatedCard(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.elevatedCardElevation(
            defaultElevation = elevation.card
        )
    ) {
        Column {
            AsyncImage(
                model = imageUrl,
                contentDescription = name,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .clip(ShapeImage),
                contentScale = ContentScale.Crop
            )

            Column(modifier = Modifier.padding(spacing.md)) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                if (address != null) {
                    Spacer(modifier = Modifier.height(spacing.xs))
                    Text(
                        text = address,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                if (rating != null && rating > 0) {
                    Spacer(modifier = Modifier.height(spacing.sm))
                    RatingBadge(
                        rating = rating,
                        reviewCount = reviewCount
                    )
                }
            }
        }
    }
}