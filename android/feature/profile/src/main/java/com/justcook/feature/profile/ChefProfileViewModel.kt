package com.justcook.feature.profile

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.justcook.core.common.result.Result
import com.justcook.domain.model.Recipe
import com.justcook.domain.repository.ChefProfile
import com.justcook.domain.repository.RecipeRepository
import com.justcook.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChefProfileViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val userRepository: UserRepository,
    private val recipeRepository: RecipeRepository
) : ViewModel() {

    private val username: String = savedStateHandle.get<String>("username") ?: ""

    private val _uiState = MutableStateFlow(ChefProfileUiState())
    val uiState: StateFlow<ChefProfileUiState> = _uiState.asStateFlow()

    init {
        loadChefProfile()
    }

    private fun loadChefProfile() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            userRepository.getChefProfile(username).collect { result ->
                when (result) {
                    is Result.Success -> {
                        _uiState.update {
                            it.copy(
                                profile = result.data,
                                isLoading = false
                            )
                        }
                        // Load chef's recipes
                        loadRecipes(result.data.user.id)
                    }
                    is Result.Error -> {
                        _uiState.update {
                            it.copy(
                                error = result.exception.message ?: "Failed to load profile",
                                isLoading = false
                            )
                        }
                    }
                }
            }
        }
    }

    private fun loadRecipes(userId: String) {
        viewModelScope.launch {
            recipeRepository.getRecipesByAuthor(userId).collect { result ->
                when (result) {
                    is Result.Success -> {
                        _uiState.update { it.copy(recipes = result.data) }
                    }
                    is Result.Error -> {
                        // Silently fail for recipes, profile is already loaded
                    }
                }
            }
        }
    }

    fun toggleFollow() {
        val profile = _uiState.value.profile ?: return

        viewModelScope.launch {
            // Optimistic update
            val wasFollowing = profile.isFollowing
            _uiState.update {
                it.copy(
                    profile = profile.copy(
                        isFollowing = !wasFollowing,
                        followerCount = profile.followerCount + if (wasFollowing) -1 else 1
                    )
                )
            }

            val result = if (wasFollowing) {
                userRepository.unfollowUser(profile.user.id)
            } else {
                userRepository.followUser(profile.user.id)
            }

            if (result is Result.Error) {
                // Rollback on error
                _uiState.update { it.copy(profile = profile) }
            }
        }
    }

    fun refresh() {
        loadChefProfile()
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}

data class ChefProfileUiState(
    val profile: ChefProfile? = null,
    val recipes: List<Recipe> = emptyList(),
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val error: String? = null
)
