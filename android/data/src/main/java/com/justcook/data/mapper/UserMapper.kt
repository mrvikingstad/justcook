package com.justcook.data.mapper

import com.justcook.core.database.entity.UserEntity
import com.justcook.data.remote.dto.response.ChefSummaryDto
import com.justcook.data.remote.dto.response.UserDto
import com.justcook.domain.model.ProfileTier
import com.justcook.domain.model.User
import com.justcook.domain.repository.ChefSummary
import java.time.Instant

object UserMapper {

    fun UserDto.toDomain(): User = User(
        id = id,
        email = email,
        name = name,
        username = username,
        displayUsername = displayUsername,
        fullName = fullName,
        country = country,
        bio = bio,
        photoUrl = photoUrl,
        profileTier = ProfileTier.fromString(profileTier),
        emailVerified = emailVerified,
        createdAt = createdAt.toInstantOrNull() ?: Instant.now()
    )

    fun UserEntity.toDomain(): User = User(
        id = id,
        email = email,
        name = name,
        username = username,
        displayUsername = displayUsername,
        fullName = fullName,
        country = country,
        bio = bio,
        photoUrl = photoUrl,
        profileTier = ProfileTier.fromString(profileTier),
        emailVerified = emailVerified,
        createdAt = createdAt
    )

    fun User.toEntity(): UserEntity = UserEntity(
        id = id,
        email = email,
        name = name,
        username = username,
        displayUsername = displayUsername,
        fullName = fullName,
        country = country,
        bio = bio,
        photoUrl = photoUrl,
        profileTier = profileTier.name.lowercase(),
        emailVerified = emailVerified,
        createdAt = createdAt
    )

    fun ChefSummaryDto.toDomain(): ChefSummary = ChefSummary(
        id = id,
        name = name,
        username = username,
        photoUrl = photoUrl,
        profileTier = profileTier,
        followerCount = followerCount
    )

    private fun String.toInstantOrNull(): Instant? = try {
        Instant.parse(this)
    } catch (e: Exception) {
        null
    }
}
