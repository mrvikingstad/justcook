package com.justcook.feature.recipes

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.outlined.Bookmark
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.ThumbDown
import androidx.compose.material.icons.outlined.ThumbUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.justcook.core.ui.theme.JustCookElevation
import com.justcook.core.ui.theme.LocalJustCookColors
import com.justcook.core.ui.theme.TierAuthor
import com.justcook.core.ui.theme.TierChef
import com.justcook.core.ui.theme.TierUser
import com.justcook.domain.model.Difficulty
import com.justcook.domain.model.Ingredient
import com.justcook.domain.model.ProfileTier
import com.justcook.domain.model.RecipeWithDetails
import com.justcook.domain.model.Step
import kotlin.math.roundToInt

@Composable
fun RecipeDetailScreen(
    slug: String,
    onBack: () -> Unit,
    onEditClick: () -> Unit,
    onCommentsClick: () -> Unit,
    onChefClick: (String) -> Unit,
    onLoginRequired: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: RecipeDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isLoggedIn by viewModel.isLoggedIn.collectAsStateWithLifecycle()
    val colors = LocalJustCookColors.current

    Box(modifier = modifier.fillMaxSize()) {
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            uiState.recipe != null -> {
                val recipe = uiState.recipe!!

                LazyColumn(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Hero Image with overlay
                    item {
                        HeroImage(
                            imageUrl = recipe.photoUrl,
                            title = recipe.title,
                            onBack = onBack,
                            isBookmarked = uiState.isBookmarked,
                            onBookmarkClick = {
                                if (isLoggedIn) viewModel.toggleBookmark()
                                else onLoginRequired()
                            },
                            canEdit = viewModel.isOwnRecipe(),
                            onEditClick = onEditClick
                        )
                    }

                    // Content
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            // Title
                            Text(
                                text = recipe.title,
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            // Author row
                            AuthorRow(
                                authorName = recipe.authorName,
                                profileTier = recipe.authorProfileTier,
                                onClick = { onChefClick(recipe.authorUsername) }
                            )

                            // Description
                            recipe.description?.let { desc ->
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = desc,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = colors.textMuted
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Meta row (time, difficulty, cuisine)
                            MetaRow(recipe = recipe)

                            Spacer(modifier = Modifier.height(16.dp))

                            // Vote and actions row
                            ActionsRow(
                                recipe = recipe,
                                onVote = { value ->
                                    if (isLoggedIn) viewModel.vote(value)
                                    else onLoginRequired()
                                },
                                onCommentsClick = onCommentsClick
                            )
                        }
                    }

                    // Divider
                    item {
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            color = colors.border
                        )
                    }

                    // Servings adjuster
                    item {
                        ServingsAdjuster(
                            servings = uiState.currentServings,
                            onServingsChange = viewModel::updateServings,
                            modifier = Modifier.padding(16.dp)
                        )
                    }

                    // Ingredients section
                    item {
                        SectionTitle(
                            title = "Ingredients",
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    item {
                        IngredientsCard(
                            ingredients = recipe.ingredients,
                            multiplier = uiState.servingsMultiplier,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }

                    // Steps section
                    item {
                        Spacer(modifier = Modifier.height(24.dp))
                        SectionTitle(
                            title = "Instructions",
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    itemsIndexed(
                        items = recipe.steps,
                        key = { index, _ -> index }
                    ) { index, step ->
                        StepItem(
                            stepNumber = index + 1,
                            instruction = step.instruction,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }

                    // Comments preview
                    item {
                        Spacer(modifier = Modifier.height(24.dp))
                        CommentsPreview(
                            commentCount = recipe.commentCount,
                            onClick = onCommentsClick,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }

                    // Bottom spacing
                    item {
                        Spacer(modifier = Modifier.height(32.dp))
                    }
                }
            }
        }

        // Error snackbar
        uiState.error?.let { error ->
            Snackbar(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp),
                action = {
                    TextButton(onClick = viewModel::clearError) {
                        Text("Dismiss")
                    }
                }
            ) {
                Text(error)
            }
        }
    }
}

@Composable
private fun HeroImage(
    imageUrl: String?,
    title: String,
    onBack: () -> Unit,
    isBookmarked: Boolean,
    onBookmarkClick: () -> Unit,
    canEdit: Boolean,
    onEditClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(16f / 10f)
    ) {
        // Image
        AsyncImage(
            model = imageUrl,
            contentDescription = title,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // Gradient overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.4f),
                            Color.Transparent,
                            Color.Transparent
                        )
                    )
                )
        )

        // Top bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Back button
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        color = Color.Black.copy(alpha = 0.3f),
                        shape = CircleShape
                    )
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }

            Row {
                // Edit button (if own recipe)
                if (canEdit) {
                    IconButton(
                        onClick = onEditClick,
                        modifier = Modifier
                            .size(40.dp)
                            .background(
                                color = Color.Black.copy(alpha = 0.3f),
                                shape = CircleShape
                            )
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Edit,
                            contentDescription = "Edit",
                            tint = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                }

                // Bookmark button
                IconButton(
                    onClick = onBookmarkClick,
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            color = Color.Black.copy(alpha = 0.3f),
                            shape = CircleShape
                        )
                ) {
                    Icon(
                        imageVector = if (isBookmarked) Icons.Outlined.Bookmark else Icons.Outlined.BookmarkBorder,
                        contentDescription = if (isBookmarked) "Remove bookmark" else "Add bookmark",
                        tint = Color.White
                    )
                }
            }
        }
    }
}

@Composable
private fun AuthorRow(
    authorName: String,
    profileTier: ProfileTier,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "by $authorName",
            style = MaterialTheme.typography.bodyMedium,
            color = LocalJustCookColors.current.textMuted
        )

        Spacer(modifier = Modifier.width(8.dp))

        TierBadge(tier = profileTier)
    }
}

@Composable
private fun TierBadge(
    tier: ProfileTier,
    modifier: Modifier = Modifier
) {
    val (text, color) = when (tier) {
        ProfileTier.USER -> "User" to TierUser
        ProfileTier.AUTHOR -> "Author" to TierAuthor
        ProfileTier.CHEF -> "Chef" to TierChef
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

@Composable
private fun MetaRow(
    recipe: RecipeWithDetails,
    modifier: Modifier = Modifier
) {
    val colors = LocalJustCookColors.current

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Time
        if (recipe.totalTime > 0) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Outlined.Schedule,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = colors.textMuted
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = formatTime(recipe.totalTime),
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textMuted
                )
            }
        }

        // Difficulty
        recipe.difficulty?.let { difficulty ->
            DifficultyIndicator(difficulty = difficulty)
        }

        // Cuisine
        recipe.cuisine?.let { cuisine ->
            Text(
                text = cuisine,
                style = MaterialTheme.typography.bodySmall,
                color = colors.textMuted
            )
        }
    }
}

@Composable
private fun DifficultyIndicator(
    difficulty: Difficulty,
    modifier: Modifier = Modifier
) {
    val colors = LocalJustCookColors.current
    val label = when (difficulty) {
        Difficulty.EASY -> "Easy"
        Difficulty.MEDIUM -> "Medium"
        Difficulty.HARD -> "Hard"
    }

    Text(
        text = label,
        style = MaterialTheme.typography.bodySmall,
        color = colors.textMuted,
        modifier = modifier
    )
}

@Composable
private fun ActionsRow(
    recipe: RecipeWithDetails,
    onVote: (Int) -> Unit,
    onCommentsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalJustCookColors.current

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Vote buttons
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Upvote
            IconButton(onClick = { onVote(1) }) {
                Icon(
                    imageVector = Icons.Outlined.ThumbUp,
                    contentDescription = "Upvote",
                    tint = if (recipe.userVote == 1) colors.voteUpActive else colors.voteNeutral
                )
            }

            Text(
                text = "${recipe.upvotes - recipe.downvotes}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            // Downvote
            IconButton(onClick = { onVote(-1) }) {
                Icon(
                    imageVector = Icons.Outlined.ThumbDown,
                    contentDescription = "Downvote",
                    tint = if (recipe.userVote == -1) colors.voteDownActive else colors.voteNeutral
                )
            }
        }

        // Comments button
        Row(
            modifier = Modifier.clickable(onClick = onCommentsClick),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Outlined.ChatBubbleOutline,
                contentDescription = null,
                tint = colors.textMuted,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "${recipe.commentCount}",
                style = MaterialTheme.typography.bodyMedium,
                color = colors.textMuted
            )
        }

        // Share button
        IconButton(onClick = { /* TODO: Share functionality */ }) {
            Icon(
                imageVector = Icons.Outlined.Share,
                contentDescription = "Share",
                tint = colors.textMuted
            )
        }
    }
}

@Composable
private fun ServingsAdjuster(
    servings: Int,
    onServingsChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalJustCookColors.current

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Servings",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Surface(
                onClick = { onServingsChange(servings - 1) },
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surface,
                border = androidx.compose.foundation.BorderStroke(1.dp, colors.border),
                modifier = Modifier.size(32.dp)
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Icon(
                        imageVector = Icons.Filled.Remove,
                        contentDescription = "Decrease servings",
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Text(
                text = "$servings",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.width(32.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            Surface(
                onClick = { onServingsChange(servings + 1) },
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surface,
                border = androidx.compose.foundation.BorderStroke(1.dp, colors.border),
                modifier = Modifier.size(32.dp)
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = "Increase servings",
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun SectionTitle(
    title: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
        modifier = modifier
    )
}

@Composable
private fun IngredientsCard(
    ingredients: List<Ingredient>,
    multiplier: Float,
    modifier: Modifier = Modifier
) {
    val colors = LocalJustCookColors.current

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = JustCookElevation.Low
        ),
        border = BorderStroke(1.dp, colors.border)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            ingredients.forEachIndexed { index, ingredient ->
                IngredientItem(
                    ingredient = ingredient,
                    multiplier = multiplier
                )
                if (index < ingredients.lastIndex) {
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 8.dp),
                        color = colors.border.copy(alpha = 0.5f)
                    )
                }
            }
        }
    }
}

@Composable
private fun IngredientItem(
    ingredient: Ingredient,
    multiplier: Float,
    modifier: Modifier = Modifier
) {
    val colors = LocalJustCookColors.current

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = ingredient.name,
                style = MaterialTheme.typography.bodyMedium
            )
            ingredient.notes?.let { notes ->
                Text(
                    text = notes,
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textMuted
                )
            }
        }

        ingredient.amount?.let { amount ->
            val scaledAmount = amount * multiplier
            val formattedAmount = if (scaledAmount == scaledAmount.toLong().toDouble()) {
                scaledAmount.toLong().toString()
            } else {
                String.format("%.1f", scaledAmount)
            }

            Text(
                text = "$formattedAmount ${ingredient.unit ?: ""}".trim(),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun StepItem(
    stepNumber: Int,
    instruction: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Step number badge
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(28.dp)
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                Text(
                    text = "$stepNumber",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
        }

        Text(
            text = instruction,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun CommentsPreview(
    commentCount: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalJustCookColors.current

    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = JustCookElevation.Low
        ),
        border = BorderStroke(1.dp, colors.border)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Outlined.ChatBubbleOutline,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Comments",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = colors.border
            ) {
                Text(
                    text = "$commentCount",
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}

private fun formatTime(minutes: Int): String {
    return if (minutes >= 60) {
        val hours = minutes / 60
        val mins = minutes % 60
        if (mins > 0) "${hours}h ${mins}m" else "${hours}h"
    } else {
        "${minutes}m"
    }
}
