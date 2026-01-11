package com.justcook.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.justcook.core.database.converter.Converters
import com.justcook.core.database.dao.BookmarkDao
import com.justcook.core.database.dao.RecipeDao
import com.justcook.core.database.dao.UserDao
import com.justcook.core.database.entity.BookmarkEntity
import com.justcook.core.database.entity.IngredientEntity
import com.justcook.core.database.entity.RecipeEntity
import com.justcook.core.database.entity.StepEntity
import com.justcook.core.database.entity.UserEntity

@Database(
    entities = [
        RecipeEntity::class,
        IngredientEntity::class,
        StepEntity::class,
        UserEntity::class,
        BookmarkEntity::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class JustCookDatabase : RoomDatabase() {
    abstract fun recipeDao(): RecipeDao
    abstract fun userDao(): UserDao
    abstract fun bookmarkDao(): BookmarkDao
}
