package com.justcook.feature.profile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Logout
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.ThumbUp
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.Button
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
import com.justcook.domain.model.ProfileTier
import com.justcook.domain.model.Recipe
import com.justcook.domain.model.User

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun UserProfileScreen(
    onSettingsClick: () -> Unit,
    onRecipeClick: (String) -> Unit,
    onLoginClick: () -> Unit,
    onFollowingClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isLoggedIn by viewModel.isLoggedIn.collectAsStateWithLifecycle()
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
            !isLoggedIn -> {
                // Not logged in state
                NotLoggedInState(
                    onLoginClick = onLoginClick
                )
            }

            uiState.isLoading && uiState.user == null -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            uiState.user != null -> {
                val user = uiState.user!!

                Box(modifier = Modifier.pullRefresh(pullRefreshState)) {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 32.dp)
                    ) {
                        // Header with settings
                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Profile",
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(start = 8.dp)
                                )

                                Row {
                                    IconButton(onClick = onSettingsClick) {
                                        Icon(
                                            imageVector = Icons.Outlined.Settings,
                                            contentDescription = "Settings"
                                        )
                                    }
                                    IconButton(onClick = viewModel::logout) {
                                        Icon(
                                            imageVector = Icons.Outlined.Logout,
                                            contentDescription = "Logout",
                                            tint = MaterialTheme.colorScheme.error
                                        )
                                    }
                                }
                            }
                        }

                        // Profile header
                        item {
                            MyProfileHeader(
                                user = user,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                        }

                        // Bio
                        user.bio?.let { bio ->
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
                            MyStatsRow(
                                recipeCount = uiState.recipes.size,
                                followingCount = uiState.followingCount,
                                onFollowingClick = onFollowingClick,
                                modifier = Modifier.padding(16.dp)
                            )
                            HorizontalDivider(
                                modifier = Modifier.padding(horizontal = 16.dp),
                                color = colors.border
                            )
                        }

                        // My Recipes section
                        item {
                            Spacer(modifier = Modifier.height(24.dp))
                            Text(
                                text = "My Recipes",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                        }

                        if (uiState.recipes.isEmpty()) {
                            item {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 24.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "No recipes yet",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = colors.textMuted
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Create your first recipe to share with the community",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = colors.textMuted
                                    )
                                }
                            }
                        } else {
                            items(
                                items = uiState.recipes,
                                key = { it.id }
                            ) { recipe ->
                                MyRecipeCard(
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
private fun NotLoggedInState(
    onLoginClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalJustCookColors.current

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Outlined.Person,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = colors.textMuted.copy(alpha = 0.5f)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Sign in to view your profile",
                style = MaterialTheme.typography.titleMedium,
                color = colors.textMuted
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Create recipes and save your favorites",
                style = MaterialTheme.typography.bodyMedium,
                color = colors.textMuted.copy(alpha = 0.7f)
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(onClick = onLoginClick) {
                Text("Sign In")
            }
        }
    }
}

@Composable
private fun MyProfileHeader(
    user: User,
    modifier: Modifier = Modifier
) {
    val colors = LocalJustCookColors.current

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
                if (user.profileTier != ProfileTier.USER) {
                    Spacer(modifier = Modifier.width(6.dp))
                    MyTierBadge(tier = user.profileTier)
                }
            }

            user.username?.let { username ->
                Text(
                    text = "@$username",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.textMuted
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = user.email,
                style = MaterialTheme.typography.bodySmall,
                color = colors.textMuted
            )

            user.country?.let { country ->
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = country,
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textMuted
                )
            }
        }
    }
}

@Composable
private fun MyTierBadge(
    tier: ProfileTier,
    modifier: Modifier = Modifier
) {
    val (text, color) = when (tier) {
        ProfileTier.USER -> return
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
private fun MyStatsRow(
    recipeCount: Int,
    followingCount: Int,
    onFollowingClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalJustCookColors.current

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Outlined.MenuBook,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = colors.textMuted
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "$recipeCount",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "recipes",
                style = MaterialTheme.typography.bodyMedium,
                color = colors.textMuted
            )
        }

        TextButton(onClick = onFollowingClick) {
            Text(
                text = "$followingCount",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "following",
                style = MaterialTheme.typography.bodyMedium,
                color = colors.textMuted
            )
        }
    }
}

@Composable
private fun MyRecipeCard(
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
