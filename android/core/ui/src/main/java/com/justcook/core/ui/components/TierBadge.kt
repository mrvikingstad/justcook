package com.justcook.core.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.justcook.core.ui.theme.TierAuthor
import com.justcook.core.ui.theme.TierChef
import com.justcook.core.ui.theme.TierUser

/**
 * Badge displaying user tier (User, Author, Chef).
 * Matches the web's TierBadge component.
 */
@Composable
fun TierBadge(
    tier: String,
    modifier: Modifier = Modifier
) {
    val (text, color) = when (tier.lowercase()) {
        "author" -> "Author" to TierAuthor
        "chef" -> "Chef" to TierChef
        else -> "User" to TierUser
    }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(4.dp),
        color = color.copy(alpha = 0.1f)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = color,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}
