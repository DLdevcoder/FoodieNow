package com.example.foodienow.core.designsystem.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * FoodieNow Elevation System
 *
 * Consistent elevation tokens used throughout the app.
 * Uses tonalElevation for Material3 compatibility.
 * Access via FoodieNowTheme.elevation or LocalElevation.current
 */
@Immutable
data class Elevation(
    /** 0dp — Flat elements, no elevation */
    val level0: Dp = 0.dp,

    /** 1dp — Default card elevation */
    val level1: Dp = 1.dp,

    /** 3dp — Hovered/pressed card, floating button */
    val level2: Dp = 3.dp,

    /** 6dp — Bottom sheet, dialog, navigation bar */
    val level3: Dp = 6.dp,

    /** 8dp — Sticky header, elevated app bar */
    val level4: Dp = 8.dp,

    /** 12dp — Modal overlay */
    val level5: Dp = 12.dp,

    // Semantic elevation aliases
    /** Card default elevation (1dp) */
    val card: Dp = 1.dp,

    /** Card pressed/hovered elevation (3dp) */
    val cardPressed: Dp = 3.dp,

    /** Bottom navigation bar (3dp) */
    val bottomNav: Dp = 3.dp,

    /** Bottom sheet (6dp) */
    val bottomSheet: Dp = 6.dp,

    /** Dialog/Modal (6dp) */
    val dialog: Dp = 6.dp,

    /** Floating action button (6dp) */
    val fab: Dp = 6.dp
)

val LocalElevation = staticCompositionLocalOf { Elevation() }