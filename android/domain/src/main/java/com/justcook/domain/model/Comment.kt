package com.justcook.domain.model

import java.time.Instant

data class Comment(
    val id: String,
    val content: String,
    val createdAt: Instant,
    val updatedAt: Instant?,
    val authorId: String,
    val authorName: String?,
    val authorPhotoUrl: String?,
    val isRecipeAuthor: Boolean,
    val isOwn: Boolean
)
