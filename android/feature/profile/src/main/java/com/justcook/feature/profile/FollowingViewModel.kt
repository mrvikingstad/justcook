package com.justcook.feature.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.justcook.core.common.result.Result
import com.justcook.domain.model.User
import com.justcook.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FollowingViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(FollowingUiState())
    val uiState: StateFlow<FollowingUiState> = _uiState.asStateFlow()

    init {
        loadFollowing()
    }

    fun loadFollowing() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            userRepository.getFollowing().collect { result ->
                when (result) {
                    is Result.Success -> {
                        _uiState.update {
                            it.copy(
                                following = result.data,
                                isLoading = false
                            )
                        }
                    }
                    is Result.Error -> {
                        _uiState.update {
                            it.copy(
                                error = result.exception.message ?: "Failed to load following",
                                isLoading = false
                            )
                        }
                    }
                }
            }
        }
    }

    fun refresh() {
        loadFollowing()
    }

    fun unfollowUser(userId: String) {
        viewModelScope.launch {
            // Optimistic update
            val previousFollowing = _uiState.value.following
            _uiState.update {
                it.copy(following = it.following.filter { user -> user.id != userId })
            }

            when (val result = userRepository.unfollowUser(userId)) {
                is Result.Success -> {
                    // Already updated optimistically
                }
                is Result.Error -> {
                    // Rollback on error
                    _uiState.update {
                        it.copy(
                            following = previousFollowing,
                            error = result.exception.message ?: "Failed to unfollow"
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

data class FollowingUiState(
    val following: List<User> = emptyList(),
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val error: String? = null
) {
    val isEmpty: Boolean get() = following.isEmpty() && !isLoading
}
