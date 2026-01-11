package com.justcook.data.di

import com.justcook.data.remote.api.AuthApiService
import com.justcook.data.remote.api.BookmarkApiService
import com.justcook.data.remote.api.RecipeApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataModule {

    @Provides
    @Singleton
    fun provideRecipeApiService(retrofit: Retrofit): RecipeApiService {
        return retrofit.create(RecipeApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideAuthApiService(retrofit: Retrofit): AuthApiService {
        return retrofit.create(AuthApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideBookmarkApiService(retrofit: Retrofit): BookmarkApiService {
        return retrofit.create(BookmarkApiService::class.java)
    }
}
