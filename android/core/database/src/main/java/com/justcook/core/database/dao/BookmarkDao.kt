package com.justcook.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.justcook.core.database.entity.BookmarkEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BookmarkDao {

    @Query("SELECT * FROM bookmarks ORDER BY createdAt DESC")
    fun observeAllBookmarks(): Flow<List<BookmarkEntity>>

    @Query("SELECT recipeSlug FROM bookmarks")
    suspend fun getAllBookmarkSlugs(): List<String>

    @Query("SELECT EXISTS(SELECT 1 FROM bookmarks WHERE recipeSlug = :slug)")
    fun observeIsBookmarked(slug: String): Flow<Boolean>

    @Query("SELECT EXISTS(SELECT 1 FROM bookmarks WHERE recipeSlug = :slug)")
    suspend fun isBookmarked(slug: String): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBookmark(bookmark: BookmarkEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBookmarks(bookmarks: List<BookmarkEntity>)

    @Query("DELETE FROM bookmarks WHERE recipeSlug = :slug")
    suspend fun deleteBookmark(slug: String)

    @Query("DELETE FROM bookmarks")
    suspend fun deleteAllBookmarks()
}
