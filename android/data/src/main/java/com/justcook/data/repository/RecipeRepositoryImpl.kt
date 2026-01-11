package com.justcook.data.repository

import com.justcook.core.common.di.IoDispatcher
import com.justcook.core.common.result.Result
import com.justcook.core.database.dao.RecipeDao
import com.justcook.data.mapper.RecipeMapper.toDomain
import com.justcook.data.mapper.RecipeMapper.toDomainWithDetails
import com.justcook.data.mapper.RecipeMapper.toEntity
import com.justcook.data.remote.api.RecipeApiService
import com.justcook.data.remote.dto.request.CreateRecipeRequestDto
import com.justcook.data.remote.dto.request.IngredientInputDto
import com.justcook.data.remote.dto.request.StepInputDto
import com.justcook.data.remote.dto.request.UpdateRecipeRequestDto
import com.justcook.data.remote.dto.request.VoteRequestDto
import com.justcook.domain.model.Recipe
import com.justcook.domain.model.RecipeWithDetails
import com.justcook.domain.model.VoteResult
import com.justcook.domain.repository.CreateRecipeRequest
import com.justcook.domain.repository.RecipeFilters
import com.justcook.domain.repository.RecipeRepository
import com.justcook.domain.repository.UpdateRecipeRequest
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RecipeRepositoryImpl @Inject constructor(
    private val recipeApi: RecipeApiService,
    private val recipeDao: RecipeDao,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : RecipeRepository {

    override fun getRecipeBySlug(slug: String): Flow<Result<RecipeWithDetails>> = flow {
        // First emit cached data if available
        val cached = recipeDao.getRecipeBySlug(slug)
        if (cached != null) {
            val ingredients = recipeDao.getIngredientsByRecipeId(cached.id)
            val steps = recipeDao.getStepsByRecipeId(cached.id)
            // For now emit basic recipe, will enhance with full details
        }

        // Fetch from network
        try {
            val response = recipeApi.getRecipeBySlug(slug)
            val recipe = response.recipe.toDomainWithDetails()
            emit(Result.Success(recipe))

            // Cache the result
            val recipeEntity = Recipe(
                id = recipe.id,
                title = recipe.title,
                slug = recipe.slug,
                description = recipe.description,
                photoUrl = recipe.photoUrl,
                authorId = recipe.authorId,
                authorName = recipe.authorName,
                authorUsername = recipe.authorUsername,
                authorProfileTier = recipe.authorProfileTier,
                cuisine = recipe.cuisine,
                tag = recipe.tag,
                difficulty = recipe.difficulty,
                prepTimeMinutes = recipe.prepTimeMinutes,
                cookTimeMinutes = recipe.cookTimeMinutes,
                servings = recipe.servings,
                upvotes = recipe.upvotes,
                downvotes = recipe.downvotes,
                commentCount = recipe.commentCount,
                publishedAt = recipe.publishedAt
            ).toEntity()

            recipeDao.insertRecipeWithDetails(
                recipe = recipeEntity,
                ingredients = recipe.ingredients.map {
                    com.justcook.data.mapper.RecipeMapper.run { it.toEntity(recipe.id) }
                },
                steps = recipe.steps.map {
                    com.justcook.data.mapper.RecipeMapper.run { it.toEntity(recipe.id) }
                }
            )
        } catch (e: Exception) {
            if (cached == null) {
                emit(Result.Error(e))
            }
        }
    }.flowOn(ioDispatcher)

    override fun getTrendingRecipes(language: String): Flow<Result<List<Recipe>>> = flow {
        // First emit cached trending
        val cached = recipeDao.getTrendingRecipes()
        if (cached.isNotEmpty()) {
            emit(Result.Success(cached.map { it.toDomain() }))
        }

        // Fetch from network
        try {
            val response = recipeApi.getTrendingRecipes(language)
            val recipes = response.trending.map { it.toDomain() }
            emit(Result.Success(recipes))

            // Update cache
            recipeDao.clearTrendingFlag()
            recipeDao.insertRecipes(recipes.map { it.toEntity(isTrending = true) })
        } catch (e: Exception) {
            if (cached.isEmpty()) {
                emit(Result.Error(e))
            }
        }
    }.flowOn(ioDispatcher)

    override fun getDiscoverRecipes(language: String): Flow<Result<List<Recipe>>> = flow {
        // First emit cached discover
        val cached = recipeDao.getDiscoverRecipes()
        if (cached.isNotEmpty()) {
            emit(Result.Success(cached.map { it.toDomain() }))
        }

        // Fetch from network
        try {
            val response = recipeApi.getDiscoverRecipes(language)
            val recipes = response.recipes.map { it.toDomain() }
            emit(Result.Success(recipes))

            // Update cache
            recipeDao.clearDiscoverFlag()
            recipeDao.insertRecipes(recipes.map { it.toEntity(isDiscover = true) })
        } catch (e: Exception) {
            if (cached.isEmpty()) {
                emit(Result.Error(e))
            }
        }
    }.flowOn(ioDispatcher)

    override fun searchRecipes(query: String, filters: RecipeFilters?): Flow<Result<List<Recipe>>> = flow {
        try {
            val response = recipeApi.searchRecipes(
                query = query,
                cuisine = filters?.cuisine,
                difficulty = filters?.difficulty,
                tag = filters?.tag,
                language = filters?.language
            )
            val recipes = response.recipes.map { it.toDomain() }
            emit(Result.Success(recipes))
        } catch (e: Exception) {
            emit(Result.Error(e))
        }
    }.flowOn(ioDispatcher)

    override fun getRecipesByAuthor(userId: String): Flow<Result<List<Recipe>>> = flow {
        // First emit cached
        val cached = recipeDao.getRecipesByAuthor(userId)
        if (cached.isNotEmpty()) {
            emit(Result.Success(cached.map { it.toDomain() }))
        }

        // Fetch from network
        try {
            val response = recipeApi.getRecipesByAuthor(userId)
            val recipes = response.recipes.map { it.toDomain() }
            emit(Result.Success(recipes))
        } catch (e: Exception) {
            if (cached.isEmpty()) {
                emit(Result.Success(emptyList()))
            }
        }
    }.flowOn(ioDispatcher)

    override suspend fun createRecipe(request: CreateRecipeRequest): Result<String> {
        return withContext(ioDispatcher) {
            try {
                val response = recipeApi.createRecipe(
                    CreateRecipeRequestDto(
                        title = request.title,
                        description = request.description,
                        cuisine = request.cuisine,
                        tag = request.tag,
                        language = request.language,
                        difficulty = request.difficulty,
                        prepTime = request.prepTimeMinutes,
                        cookTime = request.cookTimeMinutes,
                        servings = request.servings,
                        photoUrl = request.photoUrl,
                        ingredients = request.ingredients.map {
                            IngredientInputDto(
                                ingredientKey = it.ingredientKey,
                                name = it.name,
                                amount = it.amount,
                                unit = it.unit,
                                notes = it.notes
                            )
                        },
                        steps = request.steps.map { StepInputDto(it.instruction) }
                    )
                )
                if (response.success && response.slug != null) {
                    Result.Success(response.slug)
                } else {
                    Result.Error(Exception(response.error ?: "Failed to create recipe"))
                }
            } catch (e: Exception) {
                Result.Error(e)
            }
        }
    }

    override suspend fun updateRecipe(id: String, request: UpdateRecipeRequest): Result<String> {
        return withContext(ioDispatcher) {
            try {
                val response = recipeApi.updateRecipe(
                    id = id,
                    request = UpdateRecipeRequestDto(
                        title = request.title,
                        description = request.description,
                        cuisine = request.cuisine,
                        tag = request.tag,
                        language = request.language,
                        difficulty = request.difficulty,
                        prepTime = request.prepTimeMinutes,
                        cookTime = request.cookTimeMinutes,
                        servings = request.servings,
                        photoUrl = request.photoUrl,
                        ingredients = request.ingredients.map {
                            IngredientInputDto(
                                ingredientKey = it.ingredientKey,
                                name = it.name,
                                amount = it.amount,
                                unit = it.unit,
                                notes = it.notes
                            )
                        },
                        steps = request.steps.map { StepInputDto(it.instruction) }
                    )
                )
                if (response.success && response.slug != null) {
                    Result.Success(response.slug)
                } else {
                    Result.Error(Exception(response.error ?: "Failed to update recipe"))
                }
            } catch (e: Exception) {
                Result.Error(e)
            }
        }
    }

    override suspend fun deleteRecipe(id: String): Result<Unit> {
        return withContext(ioDispatcher) {
            try {
                recipeApi.deleteRecipe(id)
                Result.Success(Unit)
            } catch (e: Exception) {
                Result.Error(e)
            }
        }
    }

    override suspend fun voteOnRecipe(recipeId: String, value: Int): Result<VoteResult> {
        return withContext(ioDispatcher) {
            try {
                val response = recipeApi.vote(VoteRequestDto(recipeId, value))
                if (response.success) {
                    Result.Success(
                        VoteResult(
                            userVote = response.userVote,
                            upvotes = response.upvotes,
                            downvotes = response.downvotes
                        )
                    )
                } else {
                    Result.Error(Exception("Vote failed"))
                }
            } catch (e: Exception) {
                Result.Error(e)
            }
        }
    }

    override suspend fun removeVote(recipeId: String): Result<VoteResult> {
        // The API toggles votes, so calling vote with 0 or the same value removes it
        return voteOnRecipe(recipeId, 0)
    }
}
