package com.justcook.feature.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.justcook.core.common.result.Result
import com.justcook.domain.model.Recipe
import com.justcook.domain.model.User
import com.justcook.domain.repository.AuthRepository
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
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val recipeRepository: RecipeRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    val isLoggedIn = authRepository.isLoggedIn
    val currentUser = authRepository.currentUser

    init {
        observeUser()
    }

    private fun observeUser() {
        viewModelScope.launch {
            authRepository.currentUser.collect { user ->
                if (user != null) {
                    _uiState.update { it.copy(user = user, isLoading = false) }
                    loadMyRecipes(user.id)
                    loadFollowingCount()
                } else {
                    _uiState.update { it.copy(user = null, recipes = emptyList(), followingCount = 0, isLoading = false) }
                }
            }
        }
    }

    private fun loadFollowingCount() {
        viewModelScope.launch {
            userRepository.getFollowing().collect { result ->
                when (result) {
                    is Result.Success -> {
                        _uiState.update { it.copy(followingCount = result.data.size) }
                    }
                    is Result.Error -> {
                        // Silently fail
                    }
                }
            }
        }
    }

    private fun loadMyRecipes(userId: String) {
        viewModelScope.launch {
            recipeRepository.getRecipesByAuthor(userId).collect { result ->
                when (result) {
                    is Result.Success -> {
                        _uiState.update { it.copy(recipes = result.data) }
                    }
                    is Result.Error -> {
                        // Silently fail, user can still see their profile
                    }
                }
            }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true) }
            val user = _uiState.value.user
            if (user != null) {
                loadMyRecipes(user.id)
            }
            _uiState.update { it.copy(isRefreshing = false) }
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.signOut()
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}

data class ProfileUiState(
    val user: User? = null,
    val recipes: List<Recipe> = emptyList(),
    val recipeCount: Int = 0,
    val followerCount: Int = 0,
    val followingCount: Int = 0,
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val error: String? = null
)
