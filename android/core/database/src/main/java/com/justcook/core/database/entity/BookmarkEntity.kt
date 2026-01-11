package com.justcook.core.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.Instant

@Entity(
    tableName = "bookmarks",
    indices = [Index("recipeSlug", unique = true)]
)
data class BookmarkEntity(
    @PrimaryKey
    val recipeSlug: String,
    val createdAt: Instant = Instant.now()
)
