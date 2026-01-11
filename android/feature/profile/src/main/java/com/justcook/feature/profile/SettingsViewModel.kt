package com.justcook.feature.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.justcook.core.datastore.ThemeMode
import com.justcook.core.datastore.UserPreferencesDataStore
import com.justcook.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val userPreferencesDataStore: UserPreferencesDataStore,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    val themeMode: StateFlow<ThemeMode> = userPreferencesDataStore.userPreferences
        .map { it.themeMode }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ThemeMode.SYSTEM)

    val isLoggedIn: StateFlow<Boolean> = authRepository.isLoggedIn
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    init {
        loadUserInfo()
    }

    private fun loadUserInfo() {
        viewModelScope.launch {
            userPreferencesDataStore.userPreferences.collect { prefs ->
                _uiState.update {
                    it.copy(
                        userEmail = prefs.userEmail,
                        userName = prefs.userName
                    )
                }
            }
        }
    }

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch {
            userPreferencesDataStore.updateThemeMode(mode)
        }
    }

    fun signOut() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSigningOut = true) }
            authRepository.signOut()
            _uiState.update { it.copy(isSigningOut = false, signedOut = true) }
        }
    }
}

data class SettingsUiState(
    val userEmail: String? = null,
    val userName: String? = null,
    val isSigningOut: Boolean = false,
    val signedOut: Boolean = false
)
