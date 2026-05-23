package com.example.foodienow.core.designsystem.theme

import androidx.compose.material3.Shapes
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp

val Shapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),  // Chip, badge, compact input
    medium = RoundedCornerShape(18.dp), // Card, button, input field
    large = RoundedCornerShape(24.dp),  // Modal, dialog
    extraLarge = RoundedCornerShape(30.dp)
)

// Additional shape tokens for specific components
val ShapeImage = RoundedCornerShape(18.dp)      // Food images
val ShapePill = RoundedCornerShape(999.dp)       // Avatar, FAB, pill button
val ShapeBottomSheet = RoundedCornerShape(
    topStart = 24.dp,
    topEnd = 24.dp,
    bottomStart = 0.dp,
    bottomEnd = 0.dp
)
