package com.justcook.domain.repository

import com.justcook.core.common.result.Result
import com.justcook.domain.model.Recipe
import kotlinx.coroutines.flow.Flow

interface BookmarkRepository {
    fun getBookmarkedRecipes(): Flow<Result<List<Recipe>>>
    fun isBookmarked(slug: String): Flow<Boolean>
    suspend fun toggleBookmark(slug: String): Result<Boolean>
    suspend fun syncBookmarks(): Result<Unit>
}
