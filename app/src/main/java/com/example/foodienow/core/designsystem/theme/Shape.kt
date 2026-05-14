package com.example.foodienow.core.designsystem.theme

import androidx.compose.material3.Shapes
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp

val Shapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(8.dp),   // Chip, badge, input field
    medium = RoundedCornerShape(16.dp), // Card, bottom sheet
    large = RoundedCornerShape(24.dp),  // Modal, dialog
    extraLarge = RoundedCornerShape(32.dp)
)

// Additional shape tokens for specific components
val ShapeImage = RoundedCornerShape(12.dp)      // Food images
val ShapePill = RoundedCornerShape(999.dp)       // Avatar, FAB, pill button
val ShapeBottomSheet = RoundedCornerShape(
    topStart = 24.dp,
    topEnd = 24.dp,
    bottomStart = 0.dp,
    bottomEnd = 0.dp
)