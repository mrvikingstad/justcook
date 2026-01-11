package com.justcook.data.remote.dto.response

import kotlinx.serialization.Serializable

@Serializable
data class AuthResponseDto(
    val user: UserDto? = null,
    val session: SessionDto? = null,
    val error: String? = null
)

@Serializable
data class SessionResponseDto(
    val user: UserDto? = null,
    val session: SessionDto? = null
)

@Serializable
data class UserDto(
    val id: String,
    val email: String,
    val name: String? = null,
    val username: String? = null,
    val displayUsername: String? = null,
    val fullName: String? = null,
    val country: String? = null,
    val bio: String? = null,
    val photoUrl: String? = null,
    val profileTier: String = "user",
    val emailVerified: Boolean = false,
    val createdAt: String
)

@Serializable
data class SessionDto(
    val id: String,
    val token: String,
    val expiresAt: String
)
