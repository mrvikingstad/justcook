package com.justcook.data.remote.dto.response

import kotlinx.serialization.Serializable

@Serializable
data class RecipeDetailResponseDto(
    val recipe: RecipeDto,
    val isOwn: Boolean = false
)

@Serializable
data class TrendingResponseDto(
    val trending: List<RecipeDto>,
    val trendingChefs: List<ChefSummaryDto>? = null
)

@Serializable
data class DiscoverResponseDto(
    val recipes: List<RecipeDto>
)

@Serializable
data class RecipeListResponseDto(
    val recipes: List<RecipeDto>,
    val total: Int = 0,
    val page: Int = 1,
    val limit: Int = 20
)

@Serializable
data class RecipeDto(
    val id: String,
    val title: String,
    val slug: String,
    val description: String? = null,
    val photoUrl: String? = null,
    val authorId: String,
    val authorName: String,
    val authorUsername: String,
    val authorProfileTier: String = "user",
    val cuisine: String? = null,
    val tag: String? = null,
    val difficulty: String? = null,
    val prepTimeMinutes: Int? = null,
    val cookTimeMinutes: Int? = null,
    val servings: Int = 4,
    val upvotes: Int = 0,
    val downvotes: Int = 0,
    val userVote: Int? = null,
    val commentCount: Int = 0,
    val publishedAt: String? = null,
    val updatedAt: String? = null,
    val ingredients: List<IngredientDto>? = null,
    val steps: List<StepDto>? = null
)

@Serializable
data class IngredientDto(
    val name: String,
    val ingredientKey: String? = null,
    val amount: Double? = null,
    val unit: String? = null,
    val notes: String? = null,
    val sortOrder: Int = 0
)

@Serializable
data class StepDto(
    val stepNumber: Int = 0,
    val instruction: String
)

@Serializable
data class ChefSummaryDto(
    val id: String,
    val name: String? = null,
    val username: String,
    val photoUrl: String? = null,
    val profileTier: String = "user",
    val followerCount: Int = 0
)

@Serializable
data class CreateRecipeResponseDto(
    val success: Boolean,
    val slug: String? = null,
    val error: String? = null
)

@Serializable
data class VoteResponseDto(
    val success: Boolean,
    val userVote: Int? = null,
    val upvotes: Int = 0,
    val downvotes: Int = 0
)

@Serializable
data class BookmarksResponseDto(
    val bookmarks: List<String> = emptyList()
)

@Serializable
data class BookmarkToggleResponseDto(
    val success: Boolean,
    val bookmarked: Boolean = false
)

@Serializable
data class ChefProfileResponseDto(
    val user: UserDto,
    val stats: ChefStatsDto,
    val isFollowing: Boolean = false
)

@Serializable
data class ChefStatsDto(
    val recipeCount: Int = 0,
    val followerCount: Int = 0,
    val followingCount: Int = 0,
    val totalUpvotes: Int = 0
)

@Serializable
data class TrendingChefsResponseDto(
    val chefs: List<ChefSummaryDto>
)

@Serializable
data class FollowingResponseDto(
    val users: List<UserDto>
)

@Serializable
data class CommentsResponseDto(
    val recipe: CommentRecipeDto,
    val comments: List<CommentDto>,
    val isLoggedIn: Boolean = false,
    val currentUserId: String? = null
)

@Serializable
data class CommentRecipeDto(
    val id: String,
    val title: String,
    val slug: String,
    val authorId: String,
    val authorName: String
)

@Serializable
data class CommentDto(
    val id: String,
    val content: String,
    val createdAt: String,
    val authorId: String,
    val authorName: String? = null,
    val authorAvatar: String? = null,
    val isRecipeAuthor: Boolean = false,
    val isOwn: Boolean = false
)

@Serializable
data class CreateCommentResponseDto(
    val success: Boolean,
    val comment: CommentDto? = null,
    val error: String? = null
)

@Serializable
data class DeleteCommentResponseDto(
    val success: Boolean,
    val error: String? = null
)
