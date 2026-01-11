package com.justcook.feature.recipes

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.justcook.core.common.result.Result
import com.justcook.domain.model.Difficulty
import com.justcook.domain.repository.CreateRecipeRequest
import com.justcook.domain.repository.IngredientInput
import com.justcook.domain.repository.RecipeRepository
import com.justcook.domain.repository.StepInput
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class RecipeCreateViewModel @Inject constructor(
    private val recipeRepository: RecipeRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RecipeCreateUiState())
    val uiState: StateFlow<RecipeCreateUiState> = _uiState.asStateFlow()

    // Basic Info
    fun updateTitle(title: String) {
        _uiState.update { it.copy(title = title) }
    }

    fun updateDescription(description: String) {
        _uiState.update { it.copy(description = description) }
    }

    fun updateCuisine(cuisine: String) {
        _uiState.update { it.copy(cuisine = cuisine) }
    }

    fun updateTag(tag: String) {
        _uiState.update { it.copy(tag = tag) }
    }

    fun updateLanguage(language: String) {
        _uiState.update { it.copy(language = language) }
    }

    fun updateDifficulty(difficulty: Difficulty) {
        _uiState.update { it.copy(difficulty = difficulty) }
    }

    fun updatePrepTime(minutes: Int) {
        _uiState.update { it.copy(prepTimeMinutes = minutes) }
    }

    fun updateCookTime(minutes: Int) {
        _uiState.update { it.copy(cookTimeMinutes = minutes) }
    }

    fun updateServings(servings: Int) {
        if (servings >= 1) {
            _uiState.update { it.copy(servings = servings) }
        }
    }

    fun updatePhotoUri(uri: Uri?) {
        _uiState.update { it.copy(photoUri = uri) }
    }

    // Ingredients
    fun addIngredient() {
        val newIngredient = EditableIngredient(
            id = UUID.randomUUID().toString(),
            name = "",
            amount = "",
            unit = "",
            notes = null
        )
        _uiState.update {
            it.copy(ingredients = it.ingredients + newIngredient)
        }
    }

    fun updateIngredient(id: String, updated: EditableIngredient) {
        _uiState.update { state ->
            state.copy(
                ingredients = state.ingredients.map {
                    if (it.id == id) updated else it
                }
            )
        }
    }

    fun removeIngredient(id: String) {
        _uiState.update { state ->
            state.copy(ingredients = state.ingredients.filter { it.id != id })
        }
    }

    fun moveIngredient(fromIndex: Int, toIndex: Int) {
        _uiState.update { state ->
            val mutableList = state.ingredients.toMutableList()
            val item = mutableList.removeAt(fromIndex)
            mutableList.add(toIndex, item)
            state.copy(ingredients = mutableList)
        }
    }

    // Steps
    fun addStep() {
        val newStep = EditableStep(
            id = UUID.randomUUID().toString(),
            instruction = ""
        )
        _uiState.update {
            it.copy(steps = it.steps + newStep)
        }
    }

    fun updateStep(id: String, instruction: String) {
        _uiState.update { state ->
            state.copy(
                steps = state.steps.map {
                    if (it.id == id) it.copy(instruction = instruction) else it
                }
            )
        }
    }

    fun removeStep(id: String) {
        _uiState.update { state ->
            state.copy(steps = state.steps.filter { it.id != id })
        }
    }

    fun moveStep(fromIndex: Int, toIndex: Int) {
        _uiState.update { state ->
            val mutableList = state.steps.toMutableList()
            val item = mutableList.removeAt(fromIndex)
            mutableList.add(toIndex, item)
            state.copy(steps = mutableList)
        }
    }

    // Navigation
    fun nextStep() {
        _uiState.update { state ->
            val nextStep = (state.currentStep + 1).coerceAtMost(3)
            state.copy(currentStep = nextStep)
        }
    }

    fun previousStep() {
        _uiState.update { state ->
            val prevStep = (state.currentStep - 1).coerceAtLeast(0)
            state.copy(currentStep = prevStep)
        }
    }

    fun goToStep(step: Int) {
        _uiState.update { it.copy(currentStep = step.coerceIn(0, 3)) }
    }

    // Validation
    fun validateCurrentStep(): Boolean {
        val state = _uiState.value
        return when (state.currentStep) {
            0 -> validateBasicInfo()
            1 -> validateIngredients()
            2 -> validateSteps()
            3 -> validateAll()
            else -> false
        }
    }

    private fun validateBasicInfo(): Boolean {
        val state = _uiState.value
        val errors = mutableMapOf<String, String>()

        if (state.title.isBlank()) {
            errors["title"] = "Title is required"
        }
        if (state.description.isBlank()) {
            errors["description"] = "Description is required"
        }
        if (state.cuisine.isBlank()) {
            errors["cuisine"] = "Cuisine is required"
        }
        if (state.servings < 1) {
            errors["servings"] = "Servings must be at least 1"
        }

        _uiState.update { it.copy(validationErrors = errors) }
        return errors.isEmpty()
    }

    private fun validateIngredients(): Boolean {
        val state = _uiState.value
        val errors = mutableMapOf<String, String>()

        if (state.ingredients.isEmpty()) {
            errors["ingredients"] = "At least one ingredient is required"
        } else {
            state.ingredients.forEachIndexed { index, ingredient ->
                if (ingredient.name.isBlank()) {
                    errors["ingredient_${index}_name"] = "Ingredient name is required"
                }
            }
        }

        _uiState.update { it.copy(validationErrors = errors) }
        return errors.isEmpty()
    }

    private fun validateSteps(): Boolean {
        val state = _uiState.value
        val errors = mutableMapOf<String, String>()

        if (state.steps.isEmpty()) {
            errors["steps"] = "At least one step is required"
        } else {
            state.steps.forEachIndexed { index, step ->
                if (step.instruction.isBlank()) {
                    errors["step_${index}"] = "Step instruction is required"
                }
            }
        }

        _uiState.update { it.copy(validationErrors = errors) }
        return errors.isEmpty()
    }

    private fun validateAll(): Boolean {
        return validateBasicInfo() && validateIngredients() && validateSteps()
    }

    // Submit
    fun submitRecipe(onSuccess: (String) -> Unit) {
        if (!validateAll()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, error = null) }

            val state = _uiState.value

            // TODO: Upload photo and get URL
            val photoUrl: String? = null

            val request = CreateRecipeRequest(
                title = state.title,
                description = state.description,
                cuisine = state.cuisine,
                tag = state.tag.ifBlank { "main" },
                language = state.language,
                difficulty = state.difficulty.name.lowercase(),
                prepTimeMinutes = state.prepTimeMinutes,
                cookTimeMinutes = state.cookTimeMinutes,
                servings = state.servings,
                photoUrl = photoUrl,
                ingredients = state.ingredients.mapIndexed { index, ing ->
                    IngredientInput(
                        ingredientKey = ing.name.lowercase().replace(" ", "-"),
                        name = ing.name,
                        amount = ing.amount,
                        unit = ing.unit,
                        notes = ing.notes
                    )
                },
                steps = state.steps.map { step ->
                    StepInput(instruction = step.instruction)
                }
            )

            when (val result = recipeRepository.createRecipe(request)) {
                is Result.Success -> {
                    _uiState.update { it.copy(isSubmitting = false) }
                    onSuccess(result.data) // slug
                }
                is Result.Error -> {
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            error = result.exception.message ?: "Failed to create recipe"
                        )
                    }
                }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}

data class RecipeCreateUiState(
    val currentStep: Int = 0,
    val title: String = "",
    val description: String = "",
    val cuisine: String = "",
    val tag: String = "",
    val language: String = "en",
    val difficulty: Difficulty = Difficulty.MEDIUM,
    val prepTimeMinutes: Int = 15,
    val cookTimeMinutes: Int = 30,
    val servings: Int = 4,
    val photoUri: Uri? = null,
    val ingredients: List<EditableIngredient> = emptyList(),
    val steps: List<EditableStep> = emptyList(),
    val validationErrors: Map<String, String> = emptyMap(),
    val isSubmitting: Boolean = false,
    val error: String? = null
) {
    val stepNames = listOf("Basics", "Ingredients", "Steps", "Review")
    val canGoBack get() = currentStep > 0
    val canGoForward get() = currentStep < 3
    val isLastStep get() = currentStep == 3
}

data class EditableIngredient(
    val id: String,
    val name: String,
    val amount: String,
    val unit: String,
    val notes: String?
)

data class EditableStep(
    val id: String,
    val instruction: String
)
