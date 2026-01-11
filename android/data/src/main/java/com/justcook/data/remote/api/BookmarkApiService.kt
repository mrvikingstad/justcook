package com.justcook.data.remote.api

import com.justcook.data.remote.dto.request.BookmarkRequestDto
import com.justcook.data.remote.dto.response.BookmarkToggleResponseDto
import com.justcook.data.remote.dto.response.BookmarksResponseDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface BookmarkApiService {

    @GET("api/bookmarks")
    suspend fun getBookmarks(): BookmarksResponseDto

    @POST("api/bookmarks")
    suspend fun toggleBookmark(@Body request: BookmarkRequestDto): BookmarkToggleResponseDto
}
