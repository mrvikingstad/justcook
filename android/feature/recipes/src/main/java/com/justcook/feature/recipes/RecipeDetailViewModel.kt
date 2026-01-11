package com.justcook.feature.recipes

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.justcook.core.common.result.Result
import com.justcook.domain.model.RecipeWithDetails
import com.justcook.domain.repository.AuthRepository
import com.justcook.domain.repository.BookmarkRepository
import com.justcook.domain.repository.RecipeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RecipeDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val recipeRepository: RecipeRepository,
    private val bookmarkRepository: BookmarkRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val slug: String = checkNotNull(savedStateHandle["slug"])

    private val _uiState = MutableStateFlow(RecipeDetailUiState())
    val uiState: StateFlow<RecipeDetailUiState> = _uiState.asStateFlow()

    val isLoggedIn: StateFlow<Boolean> = authRepository.isLoggedIn
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    init {
        loadRecipe()
        observeBookmarkState()
    }

    fun loadRecipe() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            recipeRepository.getRecipeBySlug(slug).collect { result ->
                when (result) {
                    is Result.Success -> {
                        val recipe = result.data
                        _uiState.update {
                            it.copy(
                                recipe = recipe,
                                currentServings = recipe.servings,
                                isLoading = false
                            )
                        }
                    }
                    is Result.Error -> {
                        _uiState.update {
                            it.copy(
                                error = result.exception.message ?: "Failed to load recipe",
                                isLoading = false
                            )
                        }
                    }
                }
            }
        }
    }

    private fun observeBookmarkState() {
        viewModelScope.launch {
            bookmarkRepository.isBookmarked(slug).collect { isBookmarked ->
                _uiState.update { it.copy(isBookmarked = isBookmarked) }
            }
        }
    }

    fun updateServings(newServings: Int) {
        if (newServings >= 1) {
            _uiState.update { it.copy(currentServings = newServings) }
        }
    }

    fun vote(value: Int) {
        val recipe = _uiState.value.recipe ?: return

        viewModelScope.launch {
            // Optimistic update
            val previousVote = recipe.userVote
            val previousUpvotes = recipe.upvotes
            val previousDownvotes = recipe.downvotes

            val (newUpvotes, newDownvotes) = when {
                previousVote == value -> {
                    // Removing vote
                    if (value == 1) Pair(previousUpvotes - 1, previousDownvotes)
                    else Pair(previousUpvotes, previousDownvotes - 1)
                }
                value == 1 -> {
                    // Upvoting
                    val up = previousUpvotes + 1
                    val down = if (previousVote == -1) previousDownvotes - 1 else previousDownvotes
                    Pair(up, down)
                }
                else -> {
                    // Downvoting
                    val up = if (previousVote == 1) previousUpvotes - 1 else previousUpvotes
                    val down = previousDownvotes + 1
                    Pair(up, down)
                }
            }

            val newVote = if (previousVote == value) null else value

            _uiState.update { state ->
                state.copy(
                    recipe = state.recipe?.copy(
                        userVote = newVote,
                        upvotes = newUpvotes,
                        downvotes = newDownvotes
                    )
                )
            }

            // API call
            val result = if (previousVote == value) {
                recipeRepository.removeVote(recipe.id)
            } else {
                recipeRepository.voteOnRecipe(recipe.id, value)
            }

            when (result) {
                is Result.Success -> {
                    _uiState.update { state ->
                        state.copy(
                            recipe = state.recipe?.copy(
                                userVote = result.data.userVote,
                                upvotes = result.data.upvotes,
                                downvotes = result.data.downvotes
                            )
                        )
                    }
                }
                is Result.Error -> {
                    // Rollback on error
                    _uiState.update { state ->
                        state.copy(
                            recipe = state.recipe?.copy(
                                userVote = previousVote,
                                upvotes = previousUpvotes,
                                downvotes = previousDownvotes
                            ),
                            error = "Failed to vote"
                        )
                    }
                }
            }
        }
    }

    fun toggleBookmark() {
        viewModelScope.launch {
            when (val result = bookmarkRepository.toggleBookmark(slug)) {
                is Result.Success -> {
                    // State will be updated via the flow observer
                }
                is Result.Error -> {
                    _uiState.update {
                        it.copy(error = "Failed to update bookmark")
                    }
                }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    fun isOwnRecipe(): Boolean {
        val currentUserId = authRepository.currentUserId
        return currentUserId != null && _uiState.value.recipe?.authorId == currentUserId
    }
}

data class RecipeDetailUiState(
    val recipe: RecipeWithDetails? = null,
    val currentServings: Int = 4,
    val isLoading: Boolean = true,
    val isBookmarked: Boolean = false,
    val error: String? = null
) {
    val servingsMultiplier: Float
        get() = recipe?.let { currentServings.toFloat() / it.servings } ?: 1f
}
