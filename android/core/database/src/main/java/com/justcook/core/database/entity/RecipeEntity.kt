package com.justcook.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.Instant

@Entity(tableName = "recipes")
data class RecipeEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val slug: String,
    val description: String?,
    val photoUrl: String?,
    val authorId: String,
    val authorName: String,
    val authorUsername: String,
    val authorProfileTier: String,
    val cuisine: String?,
    val tag: String?,
    val difficulty: String?,
    val prepTimeMinutes: Int?,
    val cookTimeMinutes: Int?,
    val servings: Int,
    val upvotes: Int,
    val downvotes: Int,
    val commentCount: Int,
    val publishedAt: Instant?,
    val cachedAt: Instant = Instant.now(),
    val isTrending: Boolean = false,
    val isDiscover: Boolean = false
)
