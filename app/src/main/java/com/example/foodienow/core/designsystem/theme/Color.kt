package com.example.foodienow.core.designsystem.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.material3.MaterialTheme

val OrangePrimary = Color(0xFFFF5A1F)
val OrangeOnPrimary = Color(0xFFFFFFFF)
val OrangePrimaryContainer = Color(0xFFFFE7DA)
val OrangeOnPrimaryContainer = Color(0xFF5A1600)

val TealSecondary = Color(0xFF0E8A73)
val TealOnSecondary = Color(0xFFFFFFFF)
val TealSecondaryContainer = Color(0xFFD7F6EE)
val TealOnSecondaryContainer = Color(0xFF00382F)

val AmberTertiary = Color(0xFFFFB020)
val AmberOnTertiary = Color(0xFF1F1300)
val AmberTertiaryContainer = Color(0xFFFFE8B8)
val AmberOnTertiaryContainer = Color(0xFF2B1A00)

val ErrorRed = Color(0xFFBA1A1A)
val OnError = Color(0xFFFFFFFF)
val ErrorContainer = Color(0xFFFFDAD6)
val OnErrorContainer = Color(0xFF410E0B)

val LightBackground = Color(0xFFFFFBF7)
val LightOnBackground = Color(0xFF1F1B18)
val LightSurface = Color(0xFFFFFFFF)
val LightOnSurface = Color(0xFF1F1B18)
val LightSurfaceVariant = Color(0xFFF5EFE8)
val LightOnSurfaceVariant = Color(0xFF5E534B)
val LightOutline = Color(0xFFE0D6CD)

val DarkBackground = Color(0xFF17120F)
val DarkOnBackground = Color(0xFFF5EDE7)
val DarkSurface = Color(0xFF211A16)
val DarkOnSurface = Color(0xFFF6EEE8)
val DarkSurfaceVariant = Color(0xFF342821)
val DarkOnSurfaceVariant = Color(0xFFE4D3C6)
val DarkOutline = Color(0xFF65554B)

val DarkPrimaryContainer = Color(0xFF6B2608)
val DarkOnPrimaryContainer = Color(0xFFFFDBCF)
val DarkSecondaryContainer = Color(0xFF005143)
val DarkOnSecondaryContainer = Color(0xFF9CFCE0)
val DarkTertiaryContainer = Color(0xFF5E4200)
val DarkOnTertiaryContainer = Color(0xFFFFE0B2)
val DarkErrorContainer = Color(0xFF93000A)
val DarkOnErrorContainer = Color(0xFFFFDAD6)

// Semantic Colors
val SuccessGreen = Color(0xFF14A66A)
val OnSuccessGreen = Color(0xFFFFFFFF)
val SuccessGreenContainer = Color(0xFFDDF7E9)
val OnSuccessGreenContainer = Color(0xFF063F28)

val InfoBlue = Color(0xFF2F6FED)
val OnInfoBlue = Color(0xFFFFFFFF)
val InfoBlueContainer = Color(0xFFE0E9FF)
val OnInfoBlueContainer = Color(0xFF102E62)

val WarningYellow = Color(0xFFFFB020)
val OnWarningYellow = Color(0xFF1F1300)
val WarningYellowContainer = Color(0xFFFFF0C9)
val OnWarningYellowContainer = Color(0xFF713F12)

// Gradient Colors
val PromoGradientStart = Color(0xFFFF5A1F)
val PromoGradientEnd = Color(0xFFFFB020)

// Brand neutrals and commercial UI accents
val FoodieCream: Color
    @Composable
    @ReadOnlyComposable
    get() = MaterialTheme.colorScheme.background

val FoodieCreamSurface: Color
    @Composable
    @ReadOnlyComposable
    get() = if (MaterialTheme.colorScheme.background == DarkBackground) Color(0xFF2C2520) else Color(0xFFFFF6EE)

val FoodieInk: Color
    @Composable
    @ReadOnlyComposable
    get() = MaterialTheme.colorScheme.onSurface

val FoodieMuted: Color
    @Composable
    @ReadOnlyComposable
    get() = MaterialTheme.colorScheme.onSurfaceVariant

val FoodieDivider: Color
    @Composable
    @ReadOnlyComposable
    get() = MaterialTheme.colorScheme.outline

val FoodieDiscount = Color(0xFFE93535)
val FoodieRating = Color(0xFFFFB020)

// Order Status Colors
val StatusPendingColor: Color
    @Composable
    @ReadOnlyComposable
    get() = MaterialTheme.colorScheme.tertiaryContainer

val StatusDeliveringColor: Color
    @Composable
    @ReadOnlyComposable
    get() = MaterialTheme.colorScheme.secondaryContainer

val StatusCompletedColor: Color
    @Composable
    @ReadOnlyComposable
    get() = MaterialTheme.colorScheme.primaryContainer

val StatusCancelledColor: Color
    @Composable
    @ReadOnlyComposable
    get() = MaterialTheme.colorScheme.errorContainer

// Legacy aliases for existing screens
val ColorBackground: Color
    @Composable
    @ReadOnlyComposable
    get() = MaterialTheme.colorScheme.background

val ColorSurfaceLight: Color
    @Composable
    @ReadOnlyComposable
    get() = MaterialTheme.colorScheme.surfaceVariant

val ColorPrimary: Color
    @Composable
    @ReadOnlyComposable
    get() = MaterialTheme.colorScheme.primary

val ColorPrimaryDark: Color
    @Composable
    @ReadOnlyComposable
    get() = MaterialTheme.colorScheme.primary
