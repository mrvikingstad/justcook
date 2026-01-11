package com.justcook.data.remote.dto.request

import kotlinx.serialization.Serializable

@Serializable
data class EmailSignInRequestDto(
    val email: String,
    val password: String
)

@Serializable
data class EmailSignUpRequestDto(
    val email: String,
    val password: String,
    val name: String
)

@Serializable
data class MagicLinkRequestDto(
    val email: String
)

@Serializable
data class ForgotPasswordRequestDto(
    val email: String
)

@Serializable
data class GoogleCallbackRequestDto(
    val idToken: String
)
