package com.justcook.feature.bookmarks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.justcook.core.common.result.Result
import com.justcook.domain.model.Recipe
import com.justcook.domain.repository.AuthRepository
import com.justcook.domain.repository.BookmarkRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BookmarksViewModel @Inject constructor(
    private val bookmarkRepository: BookmarkRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(BookmarksUiState())
    val uiState: StateFlow<BookmarksUiState> = _uiState.asStateFlow()

    val isLoggedIn = authRepository.isLoggedIn

    init {
        loadBookmarks()
    }

    fun loadBookmarks() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            bookmarkRepository.getBookmarkedRecipes().collect { result ->
                when (result) {
                    is Result.Success -> {
                        _uiState.update {
                            it.copy(
                                bookmarks = result.data,
                                isLoading = false,
                                isRefreshing = false
                            )
                        }
                    }
                    is Result.Error -> {
                        _uiState.update {
                            it.copy(
                                error = result.exception.message ?: "Failed to load bookmarks",
                                isLoading = false,
                                isRefreshing = false
                            )
                        }
                    }
                }
            }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true) }
            bookmarkRepository.syncBookmarks()
            loadBookmarks()
        }
    }

    fun removeBookmark(slug: String) {
        viewModelScope.launch {
            // Optimistic removal from UI
            val previousBookmarks = _uiState.value.bookmarks
            _uiState.update {
                it.copy(bookmarks = it.bookmarks.filter { recipe -> recipe.slug != slug })
            }

            when (bookmarkRepository.toggleBookmark(slug)) {
                is Result.Success -> {
                    // Bookmark was removed successfully
                }
                is Result.Error -> {
                    // Rollback on error
                    _uiState.update {
                        it.copy(
                            bookmarks = previousBookmarks,
                            error = "Failed to remove bookmark"
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

data class BookmarksUiState(
    val bookmarks: List<Recipe> = emptyList(),
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val error: String? = null
) {
    val isEmpty: Boolean get() = bookmarks.isEmpty() && !isLoading
}
