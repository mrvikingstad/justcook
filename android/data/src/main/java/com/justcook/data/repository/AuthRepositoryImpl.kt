package com.justcook.data.repository

import com.justcook.core.common.di.IoDispatcher
import com.justcook.core.common.result.Result
import com.justcook.core.datastore.UserPreferencesDataStore
import com.justcook.core.network.session.SessionProvider
import com.justcook.data.mapper.UserMapper.toDomain
import com.justcook.data.remote.api.AuthApiService
import com.justcook.data.remote.dto.request.EmailSignInRequestDto
import com.justcook.data.remote.dto.request.EmailSignUpRequestDto
import com.justcook.data.remote.dto.request.ForgotPasswordRequestDto
import com.justcook.data.remote.dto.request.GoogleCallbackRequestDto
import com.justcook.data.remote.dto.request.MagicLinkRequestDto
import com.justcook.domain.model.AuthState
import com.justcook.domain.model.User
import com.justcook.domain.repository.AuthRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val authApi: AuthApiService,
    private val userPreferencesDataStore: UserPreferencesDataStore,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : AuthRepository, SessionProvider {

    private val scope = CoroutineScope(SupervisorJob() + ioDispatcher)

    private val _authState = MutableStateFlow<AuthState>(AuthState.Loading)
    override val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val _currentUser = MutableStateFlow<User?>(null)
    override val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    override val isLoggedIn: StateFlow<Boolean> = _authState
        .map { it is AuthState.Authenticated }
        .stateIn(scope, SharingStarted.Eagerly, false)

    override val currentUserId: String?
        get() = _currentUser.value?.id

    private var sessionToken: String? = null

    init {
        // Load session from DataStore on init
        scope.launch {
            userPreferencesDataStore.userPreferences.collect { prefs ->
                sessionToken = prefs.sessionToken
                if (prefs.sessionToken != null && prefs.userId != null) {
                    // We have a stored session, try to refresh it
                    refreshSession()
                } else {
                    _authState.value = AuthState.Unauthenticated
                }
            }
        }
    }

    override fun getSessionToken(): String? = sessionToken

    override fun onSessionExpired() {
        scope.launch {
            clearSession()
        }
    }

    override suspend fun signInWithEmail(email: String, password: String): Result<User> {
        return withContext(ioDispatcher) {
            try {
                val response = authApi.signInWithEmail(
                    EmailSignInRequestDto(email, password)
                )

                if (response.user != null && response.session != null) {
                    val user = response.user.toDomain()
                    saveSession(user, response.session.token)
                    Result.Success(user)
                } else {
                    Result.Error(Exception(response.error ?: "Login failed"))
                }
            } catch (e: Exception) {
                Result.Error(e)
            }
        }
    }

    override suspend fun signUpWithEmail(email: String, password: String, name: String): Result<User> {
        return withContext(ioDispatcher) {
            try {
                val response = authApi.signUpWithEmail(
                    EmailSignUpRequestDto(email, password, name)
                )

                if (response.user != null) {
                    val user = response.user.toDomain()
                    // User created but may need email verification
                    if (response.session != null) {
                        saveSession(user, response.session.token)
                    }
                    Result.Success(user)
                } else {
                    Result.Error(Exception(response.error ?: "Registration failed"))
                }
            } catch (e: Exception) {
                Result.Error(e)
            }
        }
    }

    override suspend fun signOut(): Result<Unit> {
        return withContext(ioDispatcher) {
            try {
                authApi.signOut()
            } catch (e: Exception) {
                // Ignore network errors on sign out
            }
            clearSession()
            Result.Success(Unit)
        }
    }

    override suspend fun sendMagicLink(email: String): Result<Unit> {
        return withContext(ioDispatcher) {
            try {
                authApi.sendMagicLink(MagicLinkRequestDto(email))
                Result.Success(Unit)
            } catch (e: Exception) {
                Result.Error(e)
            }
        }
    }

    override suspend fun signInWithGoogle(idToken: String): Result<User> {
        return withContext(ioDispatcher) {
            try {
                val response = authApi.googleCallback(GoogleCallbackRequestDto(idToken))

                if (response.user != null && response.session != null) {
                    val user = response.user.toDomain()
                    saveSession(user, response.session.token)
                    Result.Success(user)
                } else {
                    Result.Error(Exception(response.error ?: "Google sign-in failed"))
                }
            } catch (e: Exception) {
                Result.Error(e)
            }
        }
    }

    override suspend fun resetPassword(email: String): Result<Unit> {
        return withContext(ioDispatcher) {
            try {
                authApi.forgotPassword(ForgotPasswordRequestDto(email))
                Result.Success(Unit)
            } catch (e: Exception) {
                Result.Error(e)
            }
        }
    }

    override suspend fun refreshSession(): Result<Unit> {
        return withContext(ioDispatcher) {
            try {
                val response = authApi.getSession()
                if (response.user != null && response.session != null) {
                    val user = response.user.toDomain()
                    saveSession(user, response.session.token)
                    Result.Success(Unit)
                } else {
                    clearSession()
                    Result.Error(Exception("Session expired"))
                }
            } catch (e: Exception) {
                clearSession()
                Result.Error(e)
            }
        }
    }

    private suspend fun saveSession(user: User, token: String) {
        sessionToken = token
        _currentUser.value = user
        _authState.value = AuthState.Authenticated(user)
        userPreferencesDataStore.updateSession(
            token = token,
            userId = user.id,
            email = user.email,
            name = user.name
        )
    }

    private suspend fun clearSession() {
        sessionToken = null
        _currentUser.value = null
        _authState.value = AuthState.Unauthenticated
        userPreferencesDataStore.clearSession()
    }
}
