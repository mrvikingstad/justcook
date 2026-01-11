package com.justcook.domain.model

data class Ingredient(
    val name: String,
    val ingredientKey: String?,
    val amount: Double?,
    val unit: String?,
    val notes: String?,
    val sortOrder: Int = 0
)
