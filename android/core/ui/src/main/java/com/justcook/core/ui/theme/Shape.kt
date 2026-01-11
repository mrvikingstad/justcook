package com.justcook.core.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

val JustCookShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(6.dp),
    medium = RoundedCornerShape(8.dp),  // Primary card radius (matching web)
    large = RoundedCornerShape(12.dp),
    extraLarge = RoundedCornerShape(16.dp)
)

/**
 * Elevation values matching web shadow system
 */
object JustCookElevation {
    val Low = 2.dp
    val Medium = 6.dp
    val High = 12.dp
}
