package com.justcook.domain.model

import java.time.Instant

data class User(
    val id: String,
    val email: String,
    val name: String?,
    val username: String?,
    val displayUsername: String?,
    val fullName: String?,
    val country: String?,
    val bio: String?,
    val photoUrl: String?,
    val profileTier: ProfileTier,
    val emailVerified: Boolean,
    val createdAt: Instant
)

enum class ProfileTier {
    USER, AUTHOR, CHEF;

    companion object {
        fun fromString(value: String): ProfileTier = when (value.lowercase()) {
            "author" -> AUTHOR
            "chef" -> CHEF
            else -> USER
        }
    }
}
