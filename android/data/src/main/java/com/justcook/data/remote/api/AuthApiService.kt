package com.justcook.data.remote.api

import com.justcook.data.remote.dto.request.EmailSignInRequestDto
import com.justcook.data.remote.dto.request.EmailSignUpRequestDto
import com.justcook.data.remote.dto.request.ForgotPasswordRequestDto
import com.justcook.data.remote.dto.request.GoogleCallbackRequestDto
import com.justcook.data.remote.dto.request.MagicLinkRequestDto
import com.justcook.data.remote.dto.response.AuthResponseDto
import com.justcook.data.remote.dto.response.SessionResponseDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface AuthApiService {

    @POST("api/auth/sign-in/email")
    suspend fun signInWithEmail(@Body request: EmailSignInRequestDto): AuthResponseDto

    @POST("api/auth/sign-up/email")
    suspend fun signUpWithEmail(@Body request: EmailSignUpRequestDto): AuthResponseDto

    @POST("api/auth/sign-out")
    suspend fun signOut()

    @GET("api/auth/get-session")
    suspend fun getSession(): SessionResponseDto

    @POST("api/auth/magic-link/sign-in")
    suspend fun sendMagicLink(@Body request: MagicLinkRequestDto)

    @POST("api/auth/forgot-password")
    suspend fun forgotPassword(@Body request: ForgotPasswordRequestDto)

    @POST("api/auth/callback/google")
    suspend fun googleCallback(@Body request: GoogleCallbackRequestDto): AuthResponseDto
}
