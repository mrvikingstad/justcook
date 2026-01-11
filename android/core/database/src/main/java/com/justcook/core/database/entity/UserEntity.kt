package com.justcook.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.Instant

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey
    val id: String,
    val email: String,
    val name: String?,
    val username: String?,
    val displayUsername: String?,
    val fullName: String?,
    val country: String?,
    val bio: String?,
    val photoUrl: String?,
    val profileTier: String,
    val emailVerified: Boolean,
    val createdAt: Instant,
    val cachedAt: Instant = Instant.now()
)
