package com.justcook.feature.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.justcook.core.common.result.Result
import com.justcook.domain.model.AuthState
import com.justcook.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _loginUiState = MutableStateFlow(LoginUiState())
    val loginUiState: StateFlow<LoginUiState> = _loginUiState.asStateFlow()

    private val _registerUiState = MutableStateFlow(RegisterUiState())
    val registerUiState: StateFlow<RegisterUiState> = _registerUiState.asStateFlow()

    private val _forgotPasswordUiState = MutableStateFlow(ForgotPasswordUiState())
    val forgotPasswordUiState: StateFlow<ForgotPasswordUiState> = _forgotPasswordUiState.asStateFlow()

    val authState: StateFlow<AuthState> = authRepository.authState

    // Login
    fun updateLoginEmail(email: String) {
        _loginUiState.update { it.copy(email = email, emailError = null) }
    }

    fun updateLoginPassword(password: String) {
        _loginUiState.update { it.copy(password = password, passwordError = null) }
    }

    fun login(onSuccess: () -> Unit) {
        val state = _loginUiState.value

        // Validate
        var hasError = false
        if (state.email.isBlank() || !isValidEmail(state.email)) {
            _loginUiState.update { it.copy(emailError = "Please enter a valid email") }
            hasError = true
        }
        if (state.password.isBlank()) {
            _loginUiState.update { it.copy(passwordError = "Password is required") }
            hasError = true
        }
        if (hasError) return

        viewModelScope.launch {
            _loginUiState.update { it.copy(isLoading = true, error = null) }

            when (val result = authRepository.signInWithEmail(state.email, state.password)) {
                is Result.Success -> {
                    _loginUiState.update { it.copy(isLoading = false) }
                    onSuccess()
                }
                is Result.Error -> {
                    _loginUiState.update {
                        it.copy(
                            isLoading = false,
                            error = result.exception.message ?: "Login failed"
                        )
                    }
                }
            }
        }
    }

    fun clearLoginError() {
        _loginUiState.update { it.copy(error = null) }
    }

    // Register
    fun updateRegisterName(name: String) {
        _registerUiState.update { it.copy(name = name, nameError = null) }
    }

    fun updateRegisterEmail(email: String) {
        _registerUiState.update { it.copy(email = email, emailError = null) }
    }

    fun updateRegisterPassword(password: String) {
        _registerUiState.update { it.copy(password = password, passwordError = null) }
    }

    fun updateRegisterConfirmPassword(confirmPassword: String) {
        _registerUiState.update { it.copy(confirmPassword = confirmPassword, confirmPasswordError = null) }
    }

    fun register(onSuccess: () -> Unit) {
        val state = _registerUiState.value

        // Validate
        var hasError = false
        if (state.name.isBlank()) {
            _registerUiState.update { it.copy(nameError = "Name is required") }
            hasError = true
        }
        if (state.email.isBlank() || !isValidEmail(state.email)) {
            _registerUiState.update { it.copy(emailError = "Please enter a valid email") }
            hasError = true
        }
        if (state.password.length < 8) {
            _registerUiState.update { it.copy(passwordError = "Password must be at least 8 characters") }
            hasError = true
        }
        if (state.password != state.confirmPassword) {
            _registerUiState.update { it.copy(confirmPasswordError = "Passwords do not match") }
            hasError = true
        }
        if (hasError) return

        viewModelScope.launch {
            _registerUiState.update { it.copy(isLoading = true, error = null) }

            when (val result = authRepository.signUpWithEmail(state.email, state.password, state.name)) {
                is Result.Success -> {
                    _registerUiState.update { it.copy(isLoading = false, isSuccess = true) }
                    onSuccess()
                }
                is Result.Error -> {
                    _registerUiState.update {
                        it.copy(
                            isLoading = false,
                            error = result.exception.message ?: "Registration failed"
                        )
                    }
                }
            }
        }
    }

    fun clearRegisterError() {
        _registerUiState.update { it.copy(error = null) }
    }

    // Forgot Password
    fun updateForgotPasswordEmail(email: String) {
        _forgotPasswordUiState.update { it.copy(email = email, emailError = null) }
    }

    fun sendResetLink(onSuccess: () -> Unit) {
        val state = _forgotPasswordUiState.value

        if (state.email.isBlank() || !isValidEmail(state.email)) {
            _forgotPasswordUiState.update { it.copy(emailError = "Please enter a valid email") }
            return
        }

        viewModelScope.launch {
            _forgotPasswordUiState.update { it.copy(isLoading = true, error = null) }

            when (val result = authRepository.resetPassword(state.email)) {
                is Result.Success -> {
                    _forgotPasswordUiState.update { it.copy(isLoading = false, isSuccess = true) }
                    onSuccess()
                }
                is Result.Error -> {
                    _forgotPasswordUiState.update {
                        it.copy(
                            isLoading = false,
                            error = result.exception.message ?: "Failed to send reset link"
                        )
                    }
                }
            }
        }
    }

    fun clearForgotPasswordError() {
        _forgotPasswordUiState.update { it.copy(error = null) }
    }

    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
}

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val emailError: String? = null,
    val passwordError: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

data class RegisterUiState(
    val name: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val nameError: String? = null,
    val emailError: String? = null,
    val passwordError: String? = null,
    val confirmPasswordError: String? = null,
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null
)

data class ForgotPasswordUiState(
    val email: String = "",
    val emailError: String? = null,
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null
)
