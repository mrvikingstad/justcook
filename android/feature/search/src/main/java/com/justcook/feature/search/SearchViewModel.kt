package com.justcook.feature.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.justcook.core.common.result.Result
import com.justcook.domain.model.Difficulty
import com.justcook.domain.model.Recipe
import com.justcook.domain.repository.RecipeFilters
import com.justcook.domain.repository.RecipeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val recipeRepository: RecipeRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null

    fun updateQuery(query: String) {
        _uiState.update { it.copy(query = query) }

        // Debounce search
        searchJob?.cancel()
        if (query.isNotBlank()) {
            searchJob = viewModelScope.launch {
                delay(300) // Debounce 300ms
                performSearch()
            }
        } else {
            _uiState.update { it.copy(results = emptyList(), hasSearched = false) }
        }
    }

    fun selectCuisine(cuisine: String?) {
        _uiState.update {
            it.copy(selectedCuisine = if (it.selectedCuisine == cuisine) null else cuisine)
        }
        if (_uiState.value.query.isNotBlank()) {
            performSearch()
        }
    }

    fun selectDifficulty(difficulty: Difficulty?) {
        _uiState.update {
            it.copy(selectedDifficulty = if (it.selectedDifficulty == difficulty) null else difficulty)
        }
        if (_uiState.value.query.isNotBlank()) {
            performSearch()
        }
    }

    fun clearFilters() {
        _uiState.update { it.copy(selectedCuisine = null, selectedDifficulty = null) }
        if (_uiState.value.query.isNotBlank()) {
            performSearch()
        }
    }

    fun search() {
        if (_uiState.value.query.isNotBlank()) {
            performSearch()
        }
    }

    private fun performSearch() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            val filters = RecipeFilters(
                cuisine = _uiState.value.selectedCuisine,
                difficulty = _uiState.value.selectedDifficulty?.name?.lowercase()
            )

            recipeRepository.searchRecipes(_uiState.value.query, filters).collect { result ->
                when (result) {
                    is Result.Success -> {
                        _uiState.update {
                            it.copy(
                                results = result.data,
                                isLoading = false,
                                hasSearched = true
                            )
                        }
                    }
                    is Result.Error -> {
                        _uiState.update {
                            it.copy(
                                error = result.exception.message ?: "Search failed",
                                isLoading = false,
                                hasSearched = true
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

data class SearchUiState(
    val query: String = "",
    val selectedCuisine: String? = null,
    val selectedDifficulty: Difficulty? = null,
    val results: List<Recipe> = emptyList(),
    val isLoading: Boolean = false,
    val hasSearched: Boolean = false,
    val error: String? = null
) {
    val hasActiveFilters: Boolean
        get() = selectedCuisine != null || selectedDifficulty != null

    companion object {
        val cuisines = listOf(
            "Italian", "Mexican", "Japanese", "Chinese", "Indian",
            "French", "American", "Thai", "Mediterranean", "Korean"
        )
    }
}
