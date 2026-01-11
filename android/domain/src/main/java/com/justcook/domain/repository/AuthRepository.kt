package com.justcook.domain.repository

import com.justcook.core.common.result.Result
import com.justcook.domain.model.AuthState
import com.justcook.domain.model.User
import kotlinx.coroutines.flow.StateFlow

interface AuthRepository {
    val authState: StateFlow<AuthState>
    val currentUser: StateFlow<User?>
    val isLoggedIn: StateFlow<Boolean>
    val currentUserId: String?

    suspend fun signInWithEmail(email: String, password: String): Result<User>
    suspend fun signUpWithEmail(email: String, password: String, name: String): Result<User>
    suspend fun signOut(): Result<Unit>
    suspend fun sendMagicLink(email: String): Result<Unit>
    suspend fun signInWithGoogle(idToken: String): Result<User>
    suspend fun resetPassword(email: String): Result<Unit>
    suspend fun refreshSession(): Result<Unit>
}
