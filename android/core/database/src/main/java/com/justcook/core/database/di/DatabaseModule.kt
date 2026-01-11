package com.justcook.core.database.di

import android.content.Context
import androidx.room.Room
import com.justcook.core.database.JustCookDatabase
import com.justcook.core.database.dao.BookmarkDao
import com.justcook.core.database.dao.RecipeDao
import com.justcook.core.database.dao.UserDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context
    ): JustCookDatabase {
        return Room.databaseBuilder(
            context,
            JustCookDatabase::class.java,
            "justcook.db"
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    fun provideRecipeDao(database: JustCookDatabase): RecipeDao {
        return database.recipeDao()
    }

    @Provides
    fun provideUserDao(database: JustCookDatabase): UserDao {
        return database.userDao()
    }

    @Provides
    fun provideBookmarkDao(database: JustCookDatabase): BookmarkDao {
        return database.bookmarkDao()
    }
}
