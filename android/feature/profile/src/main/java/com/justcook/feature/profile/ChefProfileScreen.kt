package com.justcook.feature.profile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.People
import androidx.compose.material.icons.outlined.ThumbUp
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.justcook.core.ui.theme.JustCookElevation
import com.justcook.core.ui.theme.LocalJustCookColors
import com.justcook.core.ui.theme.TierAuthor
import com.justcook.core.ui.theme.TierChef
import com.justcook.core.ui.theme.TierUser
import com.justcook.domain.model.ProfileTier
import com.justcook.domain.model.Recipe
import com.justcook.domain.repository.ChefProfile
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ChefProfileScreen(
    username: String,
    onBack: () -> Unit,
    onRecipeClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ChefProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val colors = LocalJustCookColors.current

    val pullRefreshState = rememberPullRefreshState(
        refreshing = uiState.isRefreshing,
        onRefresh = viewModel::refresh
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            uiState.profile != null -> {
                val profile = uiState.profile!!

                Box(modifier = Modifier.pullRefresh(pullRefreshState)) {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 32.dp)
                    ) {
                        // Back button
                        item {
                            IconButton(
                                onClick = onBack,
                                modifier = Modifier.padding(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back"
                                )
                            }
                        }

                        // Profile header
                        item {
                            ProfileHeader(
                                profile = profile,
                                onFollowClick = viewModel::toggleFollow,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                        }

                        // Bio
                        profile.user.bio?.let { bio ->
                            item {
                                Text(
                                    text = bio,
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)
                                )
                            }
                        }

                        // Stats
                        item {
                            HorizontalDivider(
                                modifier = Modifier.padding(horizontal = 16.dp),
                                color = colors.border
                            )
                            StatsRow(
                                recipeCount = profile.recipeCount,
                                upvotes = profile.totalUpvotes,
                                followers = profile.followerCount,
                                modifier = Modifier.padding(16.dp)
                            )
                            HorizontalDivider(
                                modifier = Modifier.padding(horizontal = 16.dp),
                                color = colors.border
                            )
                        }

                        // Recipes section
                        item {
                            Spacer(modifier = Modifier.height(24.dp))
                            Text(
                                text = "Recipes",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                        }

                        if (uiState.recipes.isEmpty()) {
                            item {
                                Text(
                                    text = "No published recipes yet.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = colors.textMuted,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 24.dp)
                                )
                            }
                        } else {
                            items(
                                items = uiState.recipes,
                                key = { it.id }
                            ) { recipe ->
                                RecipeCard(
                                    recipe = recipe,
                                    onClick = { onRecipeClick(recipe.slug) },
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }

                    PullRefreshIndicator(
                        refreshing = uiState.isRefreshing,
                        state = pullRefreshState,
                        modifier = Modifier.align(Alignment.TopCenter)
                    )
                }
            }

            uiState.error != null -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Failed to load profile",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    TextButton(onClick = viewModel::refresh) {
                        Text("Try again")
                    }
                }
            }
        }

        // Error snackbar
        uiState.error?.let { error ->
            if (uiState.profile != null) {
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
}

@Composable
private fun ProfileHeader(
    profile: ChefProfile,
    onFollowClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalJustCookColors.current
    val user = profile.user

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        // Avatar
        Surface(
            modifier = Modifier.size(72.dp),
            shape = CircleShape,
            color = colors.border
        ) {
            if (user.photoUrl != null) {
                AsyncImage(
                    model = user.photoUrl,
                    contentDescription = user.fullName,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = (user.fullName?.firstOrNull() ?: user.name?.firstOrNull() ?: '?').uppercaseChar().toString(),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.textMuted
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        // Info
        Column(modifier = Modifier.weight(1f)) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = user.fullName ?: user.name ?: "Unknown",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(6.dp))
                TierBadge(tier = user.profileTier)
            }

            Text(
                text = "@${user.username ?: user.displayUsername ?: ""}",
                style = MaterialTheme.typography.bodyMedium,
                color = colors.textMuted
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Meta info
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                user.country?.let { country ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Outlined.LocationOn,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = colors.textMuted
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = country,
                            style = MaterialTheme.typography.labelMedium,
                            color = colors.textMuted
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.CalendarMonth,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = colors.textMuted
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Joined ${formatDate(user.createdAt)}",
                        style = MaterialTheme.typography.labelMedium,
                        color = colors.textMuted
                    )
                }
            }
        }

        // Follow button
        if (profile.isFollowing) {
            OutlinedButton(
                onClick = onFollowClick,
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.onBackground
                ),
                border = BorderStroke(1.dp, colors.border)
            ) {
                Text("Following")
            }
        } else {
            Button(onClick = onFollowClick) {
                Text("Follow")
            }
        }
    }
}

@Composable
private fun TierBadge(
    tier: ProfileTier,
    modifier: Modifier = Modifier
) {
    val (text, color) = when (tier) {
        ProfileTier.USER -> return // Don't show badge for regular users
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
private fun StatsRow(
    recipeCount: Int,
    upvotes: Int,
    followers: Int,
    modifier: Modifier = Modifier
) {
    val colors = LocalJustCookColors.current

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        StatItem(
            icon = Icons.Outlined.MenuBook,
            value = recipeCount,
            label = "recipes"
        )
        StatItem(
            icon = Icons.Outlined.ThumbUp,
            value = upvotes,
            label = "upvotes"
        )
        StatItem(
            icon = Icons.Outlined.People,
            value = followers,
            label = "followers"
        )
    }
}

@Composable
private fun StatItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    value: Int,
    label: String,
    modifier: Modifier = Modifier
) {
    val colors = LocalJustCookColors.current

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(18.dp),
            tint = colors.textMuted
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = "$value",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = colors.textMuted
        )
    }
}

@Composable
private fun RecipeCard(
    recipe: Recipe,
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
        Row(modifier = Modifier.fillMaxWidth()) {
            // Image
            AsyncImage(
                model = recipe.photoUrl,
                contentDescription = recipe.title,
                modifier = Modifier.size(100.dp),
                contentScale = ContentScale.Crop
            )

            // Content
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(12.dp)
            ) {
                Text(
                    text = recipe.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Meta row
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (recipe.totalTime > 0) {
                        Text(
                            text = formatTime(recipe.totalTime),
                            style = MaterialTheme.typography.labelSmall,
                            color = colors.textMuted
                        )
                    }

                    recipe.cuisine?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.labelSmall,
                            color = colors.textMuted
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Outlined.ThumbUp,
                        contentDescription = null,
                        modifier = Modifier.size(12.dp),
                        tint = colors.textMuted
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = "${recipe.upvotes}",
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.textMuted
                    )
                }
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

private fun formatDate(instant: Instant): String {
    val formatter = DateTimeFormatter.ofPattern("MMM yyyy")
    return instant.atZone(ZoneId.systemDefault()).format(formatter)
}
