package com.justcook.data.remote.dto.request

import kotlinx.serialization.Serializable

@Serializable
data class CreateRecipeRequestDto(
    val title: String,
    val description: String,
    val cuisine: String,
    val tag: String,
    val language: String,
    val difficulty: String,
    val prepTime: Int,
    val cookTime: Int,
    val servings: Int,
    val photoUrl: String?,
    val ingredients: List<IngredientInputDto>,
    val steps: List<StepInputDto>
)

@Serializable
data class UpdateRecipeRequestDto(
    val title: String,
    val description: String,
    val cuisine: String,
    val tag: String,
    val language: String,
    val difficulty: String,
    val prepTime: Int,
    val cookTime: Int,
    val servings: Int,
    val photoUrl: String?,
    val ingredients: List<IngredientInputDto>,
    val steps: List<StepInputDto>
)

@Serializable
data class IngredientInputDto(
    val ingredientKey: String,
    val name: String,
    val amount: String,
    val unit: String,
    val notes: String? = null
)

@Serializable
data class StepInputDto(
    val instruction: String
)

@Serializable
data class VoteRequestDto(
    val recipeId: String,
    val value: Int
)

@Serializable
data class BookmarkRequestDto(
    val recipeSlug: String
)

@Serializable
data class FollowRequestDto(
    val userId: String
)

@Serializable
data class CreateCommentRequestDto(
    val recipeId: String,
    val content: String
)

@Serializable
data class DeleteCommentRequestDto(
    val commentId: String
)
