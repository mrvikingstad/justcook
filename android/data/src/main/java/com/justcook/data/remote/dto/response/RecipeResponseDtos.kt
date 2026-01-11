package com.justcook.data.remote.dto.response

import kotlinx.serialization.Serializable

@Serializable
data class RecipeDetailResponseDto(
    val recipe: RecipeDto,
    val isOwn: Boolean = false
)

@Serializable
data class TrendingResponseDto(
    val recipes: List<RecipeSummaryDto>,
    val chefs: List<ChefSummaryDto>? = null
)

@Serializable
data class DiscoverResponseDto(
    val recipes: List<RecipeSummaryDto>
)

@Serializable
data class RecipeListResponseDto(
    val recipes: List<RecipeSummaryDto>,
    val page: Int = 1,
    val limit: Int = 20
)

// Summary DTO used in listings (trending, discover, search)
@Serializable
data class RecipeSummaryDto(
    val slug: String,
    val title: String,
    val description: String? = null,
    val photoUrl: String? = null,
    val authorName: String? = null,
    val authorUsername: String? = null,
    val cuisine: String? = null,
    val tag: String? = null,
    val difficulty: String? = null,
    val prepTimeMinutes: Int? = null,
    val cookTimeMinutes: Int? = null,
    val upvotes: Int = 0,
    val downvotes: Int = 0,
    val publishedAt: String? = null
)

// Full DTO used in recipe detail
@Serializable
data class RecipeDto(
    val id: String,
    val title: String,
    val slug: String,
    val description: String? = null,
    val photoUrl: String? = null,
    val authorId: String? = null,
    val author: AuthorDto? = null,
    val cuisine: String? = null,
    val tag: String? = null,
    val difficulty: String? = null,
    val prepTimeMinutes: Int? = null,
    val cookTimeMinutes: Int? = null,
    val servings: Int = 4,
    val upvotes: Int = 0,
    val downvotes: Int = 0,
    val userVote: Int? = null,
    val isBookmarked: Boolean = false,
    val commentCount: Int = 0,
    val publishedAt: String? = null,
    val updatedAt: String? = null,
    val ingredients: List<IngredientDto>? = null,
    val steps: List<StepDto>? = null
)

@Serializable
data class AuthorDto(
    val id: String,
    val username: String? = null,
    val fullName: String? = null,
    val photoUrl: String? = null,
    val profileTier: String = "user"
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
    val username: String,
    val fullName: String? = null,
    val photoUrl: String? = null,
    val profileTier: String = "user",
    val newFollowers: Int = 0
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
    val chef: ChefDto,
    val recipes: List<RecipeSummaryDto>,
    val isFollowing: Boolean = false,
    val isOwnProfile: Boolean = false
)

@Serializable
data class ChefDto(
    val id: String,
    val username: String? = null,
    val fullName: String? = null,
    val country: String? = null,
    val bio: String? = null,
    val photoUrl: String? = null,
    val profileTier: String = "user",
    val chefSince: String? = null,
    val stats: ChefStatsDto? = null
)

@Serializable
data class ChefStatsDto(
    val recipeCount: Int = 0,
    val followerCount: Int = 0,
    val totalUpvotes: Int = 0
)

@Serializable
data class FollowingResponseDto(
    val users: List<UserDto>
)

@Serializable
data class CommentsResponseDto(
    val comments: List<CommentDto>
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
