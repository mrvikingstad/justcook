package com.justcook.core.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.justcook.core.ui.R
import com.justcook.core.ui.theme.LocalJustCookColors

/**
 * Displays difficulty level as chef hat icons (1-3 filled).
 * Matches the web's DifficultyIndicator component.
 */
@Composable
fun DifficultyIndicator(
    level: Int,
    modifier: Modifier = Modifier
) {
    val colors = LocalJustCookColors.current

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(3) { index ->
            val filled = index < level
            Icon(
                painter = painterResource(id = R.drawable.ic_chef_hat),
                contentDescription = null,
                modifier = Modifier.size(14.dp),
                tint = if (filled) {
                    MaterialTheme.colorScheme.onBackground
                } else {
                    colors.textMuted.copy(alpha = 0.3f)
                }
            )
        }
    }
}

/**
 * Overload that takes difficulty as string (easy, medium, hard)
 */
@Composable
fun DifficultyIndicator(
    difficulty: String,
    modifier: Modifier = Modifier
) {
    val level = when (difficulty.lowercase()) {
        "easy" -> 1
        "medium" -> 2
        "hard" -> 3
        else -> 1
    }
    DifficultyIndicator(level = level, modifier = modifier)
}
