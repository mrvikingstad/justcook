package com.justcook.feature.recipes

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun RecipeEditScreen(
    slug: String,
    onRecipeUpdated: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    // TODO: Implement recipe edit form (similar to create)
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Edit Recipe: $slug",
            style = MaterialTheme.typography.headlineMedium
        )
    }
}
