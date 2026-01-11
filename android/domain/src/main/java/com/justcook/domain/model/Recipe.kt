package com.justcook.domain.model

import java.time.Instant

data class Recipe(
    val id: String,
    val title: String,
    val slug: String,
    val description: String?,
    val photoUrl: String?,
    val authorId: String,
    val authorName: String,
    val authorUsername: String,
    val authorProfileTier: ProfileTier,
    val cuisine: String?,
    val tag: String?,
    val difficulty: Difficulty?,
    val prepTimeMinutes: Int?,
    val cookTimeMinutes: Int?,
    val servings: Int,
    val upvotes: Int,
    val downvotes: Int,
    val commentCount: Int,
    val publishedAt: Instant?
) {
    val totalTime: Int get() = (prepTimeMinutes ?: 0) + (cookTimeMinutes ?: 0)
    val score: Int get() = upvotes - downvotes
    val voteRatio: Float get() = if (upvotes + downvotes > 0) {
        upvotes.toFloat() / (upvotes + downvotes)
    } else 0f
}

data class RecipeWithDetails(
    val id: String,
    val title: String,
    val slug: String,
    val description: String?,
    val photoUrl: String?,
    val authorId: String,
    val authorName: String,
    val authorUsername: String,
    val authorProfileTier: ProfileTier,
    val cuisine: String?,
    val tag: String?,
    val difficulty: Difficulty?,
    val prepTimeMinutes: Int?,
    val cookTimeMinutes: Int?,
    val servings: Int,
    val upvotes: Int,
    val downvotes: Int,
    val userVote: Int?,
    val commentCount: Int,
    val publishedAt: Instant?,
    val updatedAt: Instant?,
    val ingredients: List<Ingredient>,
    val steps: List<Step>
) {
    val totalTime: Int get() = (prepTimeMinutes ?: 0) + (cookTimeMinutes ?: 0)
}

enum class Difficulty {
    EASY, MEDIUM, HARD;

    companion object {
        fun fromString(value: String): Difficulty = when (value.lowercase()) {
            "easy" -> EASY
            "hard" -> HARD
            else -> MEDIUM
        }
    }
}
