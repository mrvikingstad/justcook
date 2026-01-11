package com.justcook.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.justcook.core.common.result.Result
import com.justcook.domain.repository.RecipeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val recipeRepository: RecipeRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadContent()
    }

    fun loadContent() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            // Load trending and discover in parallel
            launch { loadTrendingRecipes() }
            launch { loadDiscoverRecipes() }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true, error = null) }

            launch { loadTrendingRecipes() }
            launch { loadDiscoverRecipes() }
        }
    }

    private suspend fun loadTrendingRecipes() {
        recipeRepository.getTrendingRecipes().collect { result ->
            when (result) {
                is Result.Success -> {
                    _uiState.update {
                        it.copy(
                            trendingRecipes = result.data,
                            isLoading = false,
                            isRefreshing = false
                        )
                    }
                }
                is Result.Error -> {
                    _uiState.update {
                        it.copy(
                            error = result.exception.message ?: "Failed to load trending recipes",
                            isLoading = false,
                            isRefreshing = false
                        )
                    }
                }
            }
        }
    }

    private suspend fun loadDiscoverRecipes() {
        recipeRepository.getDiscoverRecipes().collect { result ->
            when (result) {
                is Result.Success -> {
                    _uiState.update {
                        it.copy(
                            discoverRecipes = result.data,
                            isLoading = false,
                            isRefreshing = false
                        )
                    }
                }
                is Result.Error -> {
                    // Don't override error if trending already set one
                    if (_uiState.value.error == null) {
                        _uiState.update {
                            it.copy(
                                error = result.exception.message ?: "Failed to load recipes",
                                isLoading = false,
                                isRefreshing = false
                            )
                        }
                    }
                }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
