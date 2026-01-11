package com.justcook.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.justcook.core.database.entity.IngredientEntity
import com.justcook.core.database.entity.RecipeEntity
import com.justcook.core.database.entity.StepEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RecipeDao {

    @Query("SELECT * FROM recipes WHERE slug = :slug")
    suspend fun getRecipeBySlug(slug: String): RecipeEntity?

    @Query("SELECT * FROM recipes WHERE slug = :slug")
    fun observeRecipeBySlug(slug: String): Flow<RecipeEntity?>

    @Query("SELECT * FROM recipes WHERE isTrending = 1 ORDER BY upvotes DESC LIMIT :limit")
    suspend fun getTrendingRecipes(limit: Int = 10): List<RecipeEntity>

    @Query("SELECT * FROM recipes WHERE isDiscover = 1 ORDER BY upvotes DESC LIMIT :limit")
    suspend fun getDiscoverRecipes(limit: Int = 10): List<RecipeEntity>

    @Query("SELECT * FROM recipes WHERE authorId = :userId ORDER BY publishedAt DESC")
    suspend fun getRecipesByAuthor(userId: String): List<RecipeEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecipe(recipe: RecipeEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecipes(recipes: List<RecipeEntity>)

    @Query("DELETE FROM recipes WHERE slug = :slug")
    suspend fun deleteRecipeBySlug(slug: String)

    @Query("UPDATE recipes SET isTrending = 0")
    suspend fun clearTrendingFlag()

    @Query("UPDATE recipes SET isDiscover = 0")
    suspend fun clearDiscoverFlag()

    @Query("SELECT * FROM ingredients WHERE recipeId = :recipeId ORDER BY sortOrder ASC")
    suspend fun getIngredientsByRecipeId(recipeId: String): List<IngredientEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIngredients(ingredients: List<IngredientEntity>)

    @Query("DELETE FROM ingredients WHERE recipeId = :recipeId")
    suspend fun deleteIngredientsByRecipeId(recipeId: String)

    @Query("SELECT * FROM steps WHERE recipeId = :recipeId ORDER BY stepNumber ASC")
    suspend fun getStepsByRecipeId(recipeId: String): List<StepEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSteps(steps: List<StepEntity>)

    @Query("DELETE FROM steps WHERE recipeId = :recipeId")
    suspend fun deleteStepsByRecipeId(recipeId: String)

    @Transaction
    suspend fun insertRecipeWithDetails(
        recipe: RecipeEntity,
        ingredients: List<IngredientEntity>,
        steps: List<StepEntity>
    ) {
        insertRecipe(recipe)
        deleteIngredientsByRecipeId(recipe.id)
        deleteStepsByRecipeId(recipe.id)
        insertIngredients(ingredients)
        insertSteps(steps)
    }
}
