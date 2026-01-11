package com.justcook.data.di

import com.justcook.core.network.session.SessionProvider
import com.justcook.data.repository.AuthRepositoryImpl
import com.justcook.data.repository.BookmarkRepositoryImpl
import com.justcook.data.repository.CommentRepositoryImpl
import com.justcook.data.repository.RecipeRepositoryImpl
import com.justcook.data.repository.UserRepositoryImpl
import com.justcook.domain.repository.AuthRepository
import com.justcook.domain.repository.BookmarkRepository
import com.justcook.domain.repository.CommentRepository
import com.justcook.domain.repository.RecipeRepository
import com.justcook.domain.repository.UserRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindRecipeRepository(impl: RecipeRepositoryImpl): RecipeRepository

    @Binds
    @Singleton
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository

    @Binds
    @Singleton
    abstract fun bindBookmarkRepository(impl: BookmarkRepositoryImpl): BookmarkRepository

    @Binds
    @Singleton
    abstract fun bindSessionProvider(impl: AuthRepositoryImpl): SessionProvider

    @Binds
    @Singleton
    abstract fun bindUserRepository(impl: UserRepositoryImpl): UserRepository

    @Binds
    @Singleton
    abstract fun bindCommentRepository(impl: CommentRepositoryImpl): CommentRepository
}
