package com.justcook.data.repository

import com.justcook.core.common.di.IoDispatcher
import com.justcook.core.common.result.Result
import com.justcook.data.remote.api.RecipeApiService
import com.justcook.data.remote.dto.request.CreateCommentRequestDto
import com.justcook.data.remote.dto.request.DeleteCommentRequestDto
import com.justcook.domain.model.Comment
import com.justcook.domain.repository.CommentRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CommentRepositoryImpl @Inject constructor(
    private val recipeApiService: RecipeApiService,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : CommentRepository {

    override fun getComments(recipeSlug: String): Flow<Result<List<Comment>>> = flow {
        try {
            val response = recipeApiService.getComments(recipeSlug)
            val comments = response.comments.map { dto ->
                Comment(
                    id = dto.id,
                    content = dto.content,
                    createdAt = parseInstant(dto.createdAt),
                    updatedAt = null,
                    authorId = dto.authorId,
                    authorName = dto.authorName,
                    authorPhotoUrl = dto.authorAvatar,
                    isRecipeAuthor = dto.isRecipeAuthor,
                    isOwn = dto.isOwn
                )
            }
            emit(Result.Success(comments))
        } catch (e: Exception) {
            emit(Result.Error(e))
        }
    }.flowOn(ioDispatcher)

    override suspend fun addComment(recipeId: String, content: String): Result<Comment> {
        return withContext(ioDispatcher) {
            try {
                val response = recipeApiService.createComment(
                    CreateCommentRequestDto(recipeId = recipeId, content = content)
                )
                if (response.success && response.comment != null) {
                    val dto = response.comment
                    Result.Success(
                        Comment(
                            id = dto.id,
                            content = dto.content,
                            createdAt = parseInstant(dto.createdAt),
                            updatedAt = null,
                            authorId = dto.authorId,
                            authorName = dto.authorName,
                            authorPhotoUrl = dto.authorAvatar,
                            isRecipeAuthor = dto.isRecipeAuthor,
                            isOwn = dto.isOwn
                        )
                    )
                } else {
                    Result.Error(Exception(response.error ?: "Failed to add comment"))
                }
            } catch (e: Exception) {
                Result.Error(e)
            }
        }
    }

    override suspend fun deleteComment(commentId: String): Result<Unit> {
        return withContext(ioDispatcher) {
            try {
                val response = recipeApiService.deleteComment(
                    DeleteCommentRequestDto(commentId = commentId)
                )
                if (response.success) {
                    Result.Success(Unit)
                } else {
                    Result.Error(Exception(response.error ?: "Failed to delete comment"))
                }
            } catch (e: Exception) {
                Result.Error(e)
            }
        }
    }

    private fun parseInstant(dateString: String): Instant {
        return try {
            Instant.parse(dateString)
        } catch (e: Exception) {
            Instant.now()
        }
    }
}
