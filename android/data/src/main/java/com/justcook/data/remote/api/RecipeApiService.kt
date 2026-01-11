package com.justcook.data.remote.api

import com.justcook.data.remote.dto.request.CreateCommentRequestDto
import com.justcook.data.remote.dto.request.CreateRecipeRequestDto
import com.justcook.data.remote.dto.request.DeleteCommentRequestDto
import com.justcook.data.remote.dto.request.FollowRequestDto
import com.justcook.data.remote.dto.request.UpdateRecipeRequestDto
import com.justcook.data.remote.dto.request.VoteRequestDto
import com.justcook.data.remote.dto.response.ChefProfileResponseDto
import com.justcook.data.remote.dto.response.CommentsResponseDto
import com.justcook.data.remote.dto.response.CreateCommentResponseDto
import com.justcook.data.remote.dto.response.CreateRecipeResponseDto
import com.justcook.data.remote.dto.response.DeleteCommentResponseDto
import com.justcook.data.remote.dto.response.DiscoverResponseDto
import com.justcook.data.remote.dto.response.FollowingResponseDto
import com.justcook.data.remote.dto.response.RecipeDetailResponseDto
import com.justcook.data.remote.dto.response.RecipeListResponseDto
import com.justcook.data.remote.dto.response.TrendingChefsResponseDto
import com.justcook.data.remote.dto.response.TrendingResponseDto
import com.justcook.data.remote.dto.response.VoteResponseDto
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.HTTP
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface RecipeApiService {

    @GET("recipes/{slug}")
    suspend fun getRecipeBySlug(@Path("slug") slug: String): RecipeDetailResponseDto

    @GET("trending")
    suspend fun getTrendingRecipes(@Query("lang") language: String = "en"): TrendingResponseDto

    @GET("discover")
    suspend fun getDiscoverRecipes(@Query("lang") language: String = "en"): DiscoverResponseDto

    @GET("recipes")
    suspend fun searchRecipes(
        @Query("q") query: String,
        @Query("cuisine") cuisine: String? = null,
        @Query("difficulty") difficulty: String? = null,
        @Query("tag") tag: String? = null,
        @Query("lang") language: String? = null,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20
    ): RecipeListResponseDto

    @POST("api/recipes")
    suspend fun createRecipe(@Body request: CreateRecipeRequestDto): CreateRecipeResponseDto

    @PUT("api/recipes/{id}")
    suspend fun updateRecipe(
        @Path("id") id: String,
        @Body request: UpdateRecipeRequestDto
    ): CreateRecipeResponseDto

    @DELETE("api/recipes/{id}")
    suspend fun deleteRecipe(@Path("id") id: String)

    @POST("api/votes")
    suspend fun vote(@Body request: VoteRequestDto): VoteResponseDto

    @GET("chef/{username}")
    suspend fun getChefProfile(@Path("username") username: String): ChefProfileResponseDto

    @GET("trending/chefs")
    suspend fun getTrendingChefs(): TrendingChefsResponseDto

    @GET("api/recipes/author/{userId}")
    suspend fun getRecipesByAuthor(@Path("userId") userId: String): RecipeListResponseDto

    @POST("api/follow")
    suspend fun followUser(@Body request: FollowRequestDto)

    @DELETE("api/follow/{userId}")
    suspend fun unfollowUser(@Path("userId") userId: String)

    @GET("api/following")
    suspend fun getFollowing(): FollowingResponseDto

    // Comments
    @GET("recipes/{slug}/comments")
    suspend fun getComments(@Path("slug") slug: String): CommentsResponseDto

    @POST("api/comments")
    suspend fun createComment(@Body request: CreateCommentRequestDto): CreateCommentResponseDto

    @HTTP(method = "DELETE", path = "api/comments", hasBody = true)
    suspend fun deleteComment(@Body request: DeleteCommentRequestDto): DeleteCommentResponseDto
}
