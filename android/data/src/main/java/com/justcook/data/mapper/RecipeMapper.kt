package com.justcook.data.mapper

import com.justcook.core.database.entity.IngredientEntity
import com.justcook.core.database.entity.RecipeEntity
import com.justcook.core.database.entity.StepEntity
import com.justcook.data.remote.dto.response.IngredientDto
import com.justcook.data.remote.dto.response.RecipeDto
import com.justcook.data.remote.dto.response.StepDto
import com.justcook.domain.model.Difficulty
import com.justcook.domain.model.Ingredient
import com.justcook.domain.model.ProfileTier
import com.justcook.domain.model.Recipe
import com.justcook.domain.model.RecipeWithDetails
import com.justcook.domain.model.Step
import java.time.Instant

object RecipeMapper {

    fun RecipeDto.toDomain(): Recipe = Recipe(
        id = id,
        title = title,
        slug = slug,
        description = description,
        photoUrl = photoUrl,
        authorId = authorId,
        authorName = authorName,
        authorUsername = authorUsername,
        authorProfileTier = ProfileTier.fromString(authorProfileTier),
        cuisine = cuisine,
        tag = tag,
        difficulty = difficulty?.let { Difficulty.fromString(it) },
        prepTimeMinutes = prepTimeMinutes,
        cookTimeMinutes = cookTimeMinutes,
        servings = servings,
        upvotes = upvotes,
        downvotes = downvotes,
        commentCount = commentCount,
        publishedAt = publishedAt?.toInstantOrNull()
    )

    fun RecipeDto.toDomainWithDetails(): RecipeWithDetails = RecipeWithDetails(
        id = id,
        title = title,
        slug = slug,
        description = description,
        photoUrl = photoUrl,
        authorId = authorId,
        authorName = authorName,
        authorUsername = authorUsername,
        authorProfileTier = ProfileTier.fromString(authorProfileTier),
        cuisine = cuisine,
        tag = tag,
        difficulty = difficulty?.let { Difficulty.fromString(it) },
        prepTimeMinutes = prepTimeMinutes,
        cookTimeMinutes = cookTimeMinutes,
        servings = servings,
        upvotes = upvotes,
        downvotes = downvotes,
        userVote = userVote,
        commentCount = commentCount,
        publishedAt = publishedAt?.toInstantOrNull(),
        updatedAt = updatedAt?.toInstantOrNull(),
        ingredients = ingredients?.map { it.toDomain() } ?: emptyList(),
        steps = steps?.mapIndexed { index, step -> step.toDomain(index + 1) } ?: emptyList()
    )

    fun IngredientDto.toDomain(): Ingredient = Ingredient(
        name = name,
        ingredientKey = ingredientKey,
        amount = amount,
        unit = unit,
        notes = notes,
        sortOrder = sortOrder
    )

    fun StepDto.toDomain(index: Int): Step = Step(
        stepNumber = if (stepNumber > 0) stepNumber else index,
        instruction = instruction
    )

    fun RecipeEntity.toDomain(): Recipe = Recipe(
        id = id,
        title = title,
        slug = slug,
        description = description,
        photoUrl = photoUrl,
        authorId = authorId,
        authorName = authorName,
        authorUsername = authorUsername,
        authorProfileTier = ProfileTier.fromString(authorProfileTier),
        cuisine = cuisine,
        tag = tag,
        difficulty = difficulty?.let { Difficulty.fromString(it) },
        prepTimeMinutes = prepTimeMinutes,
        cookTimeMinutes = cookTimeMinutes,
        servings = servings,
        upvotes = upvotes,
        downvotes = downvotes,
        commentCount = commentCount,
        publishedAt = publishedAt
    )

    fun Recipe.toEntity(
        isTrending: Boolean = false,
        isDiscover: Boolean = false
    ): RecipeEntity = RecipeEntity(
        id = id,
        title = title,
        slug = slug,
        description = description,
        photoUrl = photoUrl,
        authorId = authorId,
        authorName = authorName,
        authorUsername = authorUsername,
        authorProfileTier = authorProfileTier.name.lowercase(),
        cuisine = cuisine,
        tag = tag,
        difficulty = difficulty?.name?.lowercase(),
        prepTimeMinutes = prepTimeMinutes,
        cookTimeMinutes = cookTimeMinutes,
        servings = servings,
        upvotes = upvotes,
        downvotes = downvotes,
        commentCount = commentCount,
        publishedAt = publishedAt,
        isTrending = isTrending,
        isDiscover = isDiscover
    )

    fun IngredientEntity.toDomain(): Ingredient = Ingredient(
        name = name,
        ingredientKey = ingredientKey,
        amount = amount,
        unit = unit,
        notes = notes,
        sortOrder = sortOrder
    )

    fun Ingredient.toEntity(recipeId: String): IngredientEntity = IngredientEntity(
        recipeId = recipeId,
        name = name,
        ingredientKey = ingredientKey,
        amount = amount,
        unit = unit,
        notes = notes,
        sortOrder = sortOrder
    )

    fun StepEntity.toDomain(): Step = Step(
        stepNumber = stepNumber,
        instruction = instruction
    )

    fun Step.toEntity(recipeId: String): StepEntity = StepEntity(
        recipeId = recipeId,
        stepNumber = stepNumber,
        instruction = instruction
    )

    private fun String.toInstantOrNull(): Instant? = try {
        Instant.parse(this)
    } catch (e: Exception) {
        null
    }
}
