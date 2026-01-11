package com.justcook.domain.repository

import com.justcook.core.common.result.Result
import com.justcook.domain.model.User
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    fun getChefProfile(username: String): Flow<Result<ChefProfile>>
    fun getTrendingChefs(): Flow<Result<List<ChefSummary>>>
    suspend fun updateProfile(request: UpdateProfileRequest): Result<User>
    suspend fun followUser(userId: String): Result<Unit>
    suspend fun unfollowUser(userId: String): Result<Unit>
    fun isFollowing(userId: String): Flow<Boolean>
    fun getFollowing(): Flow<Result<List<User>>>
}

data class ChefProfile(
    val user: User,
    val recipeCount: Int,
    val followerCount: Int,
    val followingCount: Int,
    val totalUpvotes: Int,
    val isFollowing: Boolean
)

data class ChefSummary(
    val id: String,
    val name: String?,
    val username: String,
    val photoUrl: String?,
    val profileTier: String,
    val followerCount: Int
)

data class UpdateProfileRequest(
    val fullName: String,
    val country: String?,
    val bio: String?,
    val photoUrl: String?
)
