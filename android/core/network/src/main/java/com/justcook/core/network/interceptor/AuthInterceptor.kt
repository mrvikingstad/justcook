package com.justcook.core.network.interceptor

import com.justcook.core.network.session.SessionProvider
import dagger.Lazy
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthInterceptor @Inject constructor(
    private val sessionProvider: Lazy<SessionProvider>
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        val token = sessionProvider.get().getSessionToken()

        // Always add X-Requested-With header for CSRF protection bypass
        // This header identifies requests from the mobile app (not a browser)
        val requestBuilder = originalRequest.newBuilder()
            .addHeader("X-Requested-With", "JustCookApp")

        val request = if (token != null) {
            requestBuilder
                .addHeader("Authorization", "Bearer $token")
                .addHeader("Cookie", "better-auth.session_token=$token")
                .build()
        } else {
            requestBuilder.build()
        }

        val response = chain.proceed(request)

        // Handle 401 Unauthorized - session expired
        if (response.code == 401) {
            sessionProvider.get().onSessionExpired()
        }

        return response
    }
}
