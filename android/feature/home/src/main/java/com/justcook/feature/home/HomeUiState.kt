package com.justcook.feature.home

import com.justcook.domain.model.Recipe
import com.justcook.domain.repository.ChefSummary

data class HomeUiState(
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val trendingRecipes: List<Recipe> = emptyList(),
    val trendingChefs: List<ChefSummary> = emptyList(),
    val discoverRecipes: List<Recipe> = emptyList(),
    val error: String? = null
)
