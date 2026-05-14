package com.example.foodienow.core.designsystem.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * FoodieNow Spacing System
 *
 * Consistent spacing tokens used throughout the app.
 * Access via FoodieNowTheme.spacing or LocalSpacing.current
 */
@Immutable
data class Spacing(
    /** 4dp — Icon-text inline gap, tight padding */
    val xs: Dp = 4.dp,

    /** 8dp — Badge padding, small gaps */
    val sm: Dp = 8.dp,

    /** 12dp — Card internal padding */
    val md: Dp = 12.dp,

    /** 16dp — Screen horizontal padding, section gaps */
    val lg: Dp = 16.dp,

    /** 24dp — Major section gaps */
    val xl: Dp = 24.dp,

    /** 32dp — Top/bottom screen padding */
    val xxl: Dp = 32.dp,

    /** 48dp — Large spacers, hero sections */
    val xxxl: Dp = 48.dp,

    // Semantic spacing aliases
    /** Screen horizontal padding (16dp) */
    val screenHorizontal: Dp = 16.dp,

    /** Padding below AppBar (8dp) */
    val screenTopBelowAppBar: Dp = 8.dp,

    /** Padding above BottomNav (16dp) */
    val screenBottomAboveNav: Dp = 16.dp,

    /** Gap between food cards in horizontal scroll (12dp) */
    val cardGap: Dp = 12.dp,

    /** Gap between restaurant cards in vertical list (16dp) */
    val listItemGap: Dp = 16.dp,

    /** Gap between category chips (8dp) */
    val chipGap: Dp = 8.dp
)

val LocalSpacing = staticCompositionLocalOf { Spacing() }