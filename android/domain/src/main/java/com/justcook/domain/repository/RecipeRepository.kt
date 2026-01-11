package com.justcook.domain.repository

import com.justcook.core.common.result.Result
import com.justcook.domain.model.Recipe
import com.justcook.domain.model.RecipeWithDetails
import com.justcook.domain.model.VoteResult
import kotlinx.coroutines.flow.Flow

interface RecipeRepository {
    fun getRecipeBySlug(slug: String): Flow<Result<RecipeWithDetails>>
    fun getTrendingRecipes(language: String = "en"): Flow<Result<List<Recipe>>>
    fun getDiscoverRecipes(language: String = "en"): Flow<Result<List<Recipe>>>
    fun searchRecipes(query: String, filters: RecipeFilters? = null): Flow<Result<List<Recipe>>>
    fun getRecipesByAuthor(userId: String): Flow<Result<List<Recipe>>>

    suspend fun createRecipe(request: CreateRecipeRequest): Result<String>
    suspend fun updateRecipe(id: String, request: UpdateRecipeRequest): Result<String>
    suspend fun deleteRecipe(id: String): Result<Unit>

    suspend fun voteOnRecipe(recipeId: String, value: Int): Result<VoteResult>
    suspend fun removeVote(recipeId: String): Result<VoteResult>
}

data class RecipeFilters(
    val cuisine: String? = null,
    val difficulty: String? = null,
    val tag: String? = null,
    val language: String? = null
)

data class CreateRecipeRequest(
    val title: String,
    val description: String,
    val cuisine: String,
    val tag: String,
    val language: String,
    val difficulty: String,
    val prepTimeMinutes: Int,
    val cookTimeMinutes: Int,
    val servings: Int,
    val photoUrl: String?,
    val ingredients: List<IngredientInput>,
    val steps: List<StepInput>
)

data class UpdateRecipeRequest(
    val title: String,
    val description: String,
    val cuisine: String,
    val tag: String,
    val language: String,
    val difficulty: String,
    val prepTimeMinutes: Int,
    val cookTimeMinutes: Int,
    val servings: Int,
    val photoUrl: String?,
    val ingredients: List<IngredientInput>,
    val steps: List<StepInput>
)

data class IngredientInput(
    val ingredientKey: String,
    val name: String,
    val amount: String,
    val unit: String,
    val notes: String? = null
)

data class StepInput(
    val instruction: String
)
