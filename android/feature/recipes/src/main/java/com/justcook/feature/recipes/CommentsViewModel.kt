package com.justcook.feature.recipes

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.justcook.core.common.result.Result
import com.justcook.domain.model.Comment
import com.justcook.domain.repository.AuthRepository
import com.justcook.domain.repository.CommentRepository
import com.justcook.domain.repository.RecipeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CommentsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val commentRepository: CommentRepository,
    private val recipeRepository: RecipeRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val slug: String = checkNotNull(savedStateHandle["slug"])

    private val _uiState = MutableStateFlow(CommentsUiState())
    val uiState: StateFlow<CommentsUiState> = _uiState.asStateFlow()

    val isLoggedIn: StateFlow<Boolean> = authRepository.isLoggedIn
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    init {
        loadRecipeInfo()
        loadComments()
    }

    private fun loadRecipeInfo() {
        viewModelScope.launch {
            recipeRepository.getRecipeBySlug(slug).collect { result ->
                when (result) {
                    is Result.Success -> {
                        _uiState.update {
                            it.copy(
                                recipeId = result.data.id,
                                recipeTitle = result.data.title,
                                recipeAuthorId = result.data.authorId
                            )
                        }
                    }
                    is Result.Error -> {
                        // Recipe info load failure is non-critical, we might still load comments
                    }
                }
            }
        }
    }

    fun loadComments() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            commentRepository.getComments(slug).collect { result ->
                when (result) {
                    is Result.Success -> {
                        _uiState.update {
                            it.copy(
                                comments = result.data,
                                isLoading = false
                            )
                        }
                    }
                    is Result.Error -> {
                        _uiState.update {
                            it.copy(
                                error = result.exception.message ?: "Failed to load comments",
                                isLoading = false
                            )
                        }
                    }
                }
            }
        }
    }

    fun refresh() {
        loadComments()
    }

    fun updateCommentText(text: String) {
        _uiState.update { it.copy(commentText = text) }
    }

    fun addComment() {
        val recipeId = _uiState.value.recipeId ?: return
        val content = _uiState.value.commentText.trim()
        if (content.isBlank()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, error = null) }

            when (val result = commentRepository.addComment(recipeId, content)) {
                is Result.Success -> {
                    // Add new comment to top of list
                    _uiState.update {
                        it.copy(
                            comments = listOf(result.data) + it.comments,
                            commentText = "",
                            isSubmitting = false
                        )
                    }
                }
                is Result.Error -> {
                    _uiState.update {
                        it.copy(
                            error = result.exception.message ?: "Failed to add comment",
                            isSubmitting = false
                        )
                    }
                }
            }
        }
    }

    fun deleteComment(commentId: String) {
        viewModelScope.launch {
            // Optimistic update - remove from list immediately
            val previousComments = _uiState.value.comments
            _uiState.update {
                it.copy(comments = it.comments.filter { c -> c.id != commentId })
            }

            when (val result = commentRepository.deleteComment(commentId)) {
                is Result.Success -> {
                    // Already removed from UI
                }
                is Result.Error -> {
                    // Rollback on error
                    _uiState.update {
                        it.copy(
                            comments = previousComments,
                            error = result.exception.message ?: "Failed to delete comment"
                        )
                    }
                }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}

data class CommentsUiState(
    val recipeId: String? = null,
    val recipeTitle: String = "",
    val recipeAuthorId: String? = null,
    val comments: List<Comment> = emptyList(),
    val commentText: String = "",
    val isLoading: Boolean = true,
    val isSubmitting: Boolean = false,
    val error: String? = null
)
