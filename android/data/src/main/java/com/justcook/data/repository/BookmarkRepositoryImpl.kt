package com.justcook.data.repository

import com.justcook.core.common.di.IoDispatcher
import com.justcook.core.common.result.Result
import com.justcook.core.database.dao.BookmarkDao
import com.justcook.core.database.dao.RecipeDao
import com.justcook.core.database.entity.BookmarkEntity
import com.justcook.data.mapper.RecipeMapper.toDomain
import com.justcook.data.remote.api.BookmarkApiService
import com.justcook.data.remote.dto.request.BookmarkRequestDto
import com.justcook.domain.model.Recipe
import com.justcook.domain.repository.BookmarkRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BookmarkRepositoryImpl @Inject constructor(
    private val bookmarkApi: BookmarkApiService,
    private val bookmarkDao: BookmarkDao,
    private val recipeDao: RecipeDao,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : BookmarkRepository {

    override fun getBookmarkedRecipes(): Flow<Result<List<Recipe>>> = flow {
        // Emit cached bookmarked recipes
        val bookmarkSlugs = bookmarkDao.getAllBookmarkSlugs()
        val cachedRecipes = bookmarkSlugs.mapNotNull { slug ->
            recipeDao.getRecipeBySlug(slug)?.toDomain()
        }

        if (cachedRecipes.isNotEmpty()) {
            emit(Result.Success(cachedRecipes))
        }

        // Sync with server
        try {
            val response = bookmarkApi.getBookmarks()
            val serverSlugs = response.bookmarks.toSet()

            // Update local bookmarks to match server
            bookmarkDao.deleteAllBookmarks()
            bookmarkDao.insertBookmarks(serverSlugs.map { BookmarkEntity(it) })

            // Get recipes for the bookmarks we have cached
            val recipes = serverSlugs.mapNotNull { slug ->
                recipeDao.getRecipeBySlug(slug)?.toDomain()
            }
            emit(Result.Success(recipes))
        } catch (e: Exception) {
            if (cachedRecipes.isEmpty()) {
                emit(Result.Error(e))
            }
        }
    }.flowOn(ioDispatcher)

    override fun isBookmarked(slug: String): Flow<Boolean> {
        return bookmarkDao.observeIsBookmarked(slug)
    }

    override suspend fun toggleBookmark(slug: String): Result<Boolean> {
        return withContext(ioDispatcher) {
            try {
                // Optimistic update
                val wasBookmarked = bookmarkDao.isBookmarked(slug)
                if (wasBookmarked) {
                    bookmarkDao.deleteBookmark(slug)
                } else {
                    bookmarkDao.insertBookmark(BookmarkEntity(slug))
                }

                // Sync with server
                val response = bookmarkApi.toggleBookmark(BookmarkRequestDto(slug))

                // Correct local state if server disagrees
                if (response.bookmarked != !wasBookmarked) {
                    if (response.bookmarked) {
                        bookmarkDao.insertBookmark(BookmarkEntity(slug))
                    } else {
                        bookmarkDao.deleteBookmark(slug)
                    }
                }

                Result.Success(response.bookmarked)
            } catch (e: Exception) {
                // Rollback optimistic update
                val wasBookmarked = bookmarkDao.isBookmarked(slug)
                if (wasBookmarked) {
                    bookmarkDao.deleteBookmark(slug)
                } else {
                    bookmarkDao.insertBookmark(BookmarkEntity(slug))
                }
                Result.Error(e)
            }
        }
    }

    override suspend fun syncBookmarks(): Result<Unit> {
        return withContext(ioDispatcher) {
            try {
                val response = bookmarkApi.getBookmarks()
                bookmarkDao.deleteAllBookmarks()
                bookmarkDao.insertBookmarks(response.bookmarks.map { BookmarkEntity(it) })
                Result.Success(Unit)
            } catch (e: Exception) {
                Result.Error(e)
            }
        }
    }
}
