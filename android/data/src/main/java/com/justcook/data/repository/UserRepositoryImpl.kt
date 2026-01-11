package com.justcook.data.repository

import com.justcook.core.common.di.IoDispatcher
import com.justcook.core.common.result.Result
import com.justcook.data.mapper.UserMapper.toDomain
import com.justcook.data.remote.api.RecipeApiService
import com.justcook.data.remote.dto.request.FollowRequestDto
import com.justcook.domain.model.User
import com.justcook.domain.repository.ChefProfile
import com.justcook.domain.repository.ChefSummary
import com.justcook.domain.repository.UpdateProfileRequest
import com.justcook.domain.repository.UserRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepositoryImpl @Inject constructor(
    private val recipeApiService: RecipeApiService,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : UserRepository {

    private val followingSet = MutableStateFlow<Set<String>>(emptySet())

    override fun getChefProfile(username: String): Flow<Result<ChefProfile>> = flow {
        try {
            val response = recipeApiService.getChefProfile(username)
            val user = response.user.toDomain()
            val profile = ChefProfile(
                user = user,
                recipeCount = response.stats.recipeCount,
                followerCount = response.stats.followerCount,
                followingCount = response.stats.followingCount,
                totalUpvotes = response.stats.totalUpvotes,
                isFollowing = response.isFollowing
            )
            emit(Result.Success(profile))
        } catch (e: Exception) {
            emit(Result.Error(e))
        }
    }.flowOn(ioDispatcher)

    override fun getTrendingChefs(): Flow<Result<List<ChefSummary>>> = flow {
        try {
            val response = recipeApiService.getTrendingChefs()
            val chefs = response.chefs.map { chef ->
                ChefSummary(
                    id = chef.id,
                    name = chef.name,
                    username = chef.username,
                    photoUrl = chef.photoUrl,
                    profileTier = chef.profileTier,
                    followerCount = chef.followerCount
                )
            }
            emit(Result.Success(chefs))
        } catch (e: Exception) {
            emit(Result.Error(e))
        }
    }.flowOn(ioDispatcher)

    override suspend fun updateProfile(request: UpdateProfileRequest): Result<User> {
        return withContext(ioDispatcher) {
            try {
                // TODO: Implement profile update API call
                Result.Error(NotImplementedError("Profile update not yet implemented"))
            } catch (e: Exception) {
                Result.Error(e)
            }
        }
    }

    override suspend fun followUser(userId: String): Result<Unit> {
        return withContext(ioDispatcher) {
            try {
                recipeApiService.followUser(FollowRequestDto(userId))
                followingSet.value = followingSet.value + userId
                Result.Success(Unit)
            } catch (e: Exception) {
                Result.Error(e)
            }
        }
    }

    override suspend fun unfollowUser(userId: String): Result<Unit> {
        return withContext(ioDispatcher) {
            try {
                recipeApiService.unfollowUser(userId)
                followingSet.value = followingSet.value - userId
                Result.Success(Unit)
            } catch (e: Exception) {
                Result.Error(e)
            }
        }
    }

    override fun isFollowing(userId: String): Flow<Boolean> = flow {
        emit(followingSet.value.contains(userId))
    }

    override fun getFollowing(): Flow<Result<List<User>>> = flow {
        try {
            val response = recipeApiService.getFollowing()
            val users = response.users.map { it.toDomain() }
            emit(Result.Success(users))
        } catch (e: Exception) {
            emit(Result.Error(e))
        }
    }.flowOn(ioDispatcher)
}
