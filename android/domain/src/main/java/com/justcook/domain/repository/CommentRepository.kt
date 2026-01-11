package com.justcook.domain.repository

import com.justcook.core.common.result.Result
import com.justcook.domain.model.Comment
import kotlinx.coroutines.flow.Flow

interface CommentRepository {
    fun getComments(recipeSlug: String): Flow<Result<List<Comment>>>
    suspend fun addComment(recipeId: String, content: String): Result<Comment>
    suspend fun deleteComment(commentId: String): Result<Unit>
}
