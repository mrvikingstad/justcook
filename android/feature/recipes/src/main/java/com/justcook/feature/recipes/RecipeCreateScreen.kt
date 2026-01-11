package com.justcook.feature.recipes

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.justcook.core.ui.theme.JustCookElevation
import com.justcook.core.ui.theme.LocalJustCookColors
import com.justcook.domain.model.Difficulty

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeCreateScreen(
    onRecipeCreated: (String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: RecipeCreateViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val colors = LocalJustCookColors.current

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Create Recipe") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .imePadding()
            ) {
                // Step indicator
                StepIndicator(
                    currentStep = uiState.currentStep,
                    stepNames = uiState.stepNames,
                    onStepClick = { viewModel.goToStep(it) },
                    modifier = Modifier.padding(16.dp)
                )

                // Content
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    AnimatedContent(
                        targetState = uiState.currentStep,
                        transitionSpec = {
                            if (targetState > initialState) {
                                slideInHorizontally { it } togetherWith slideOutHorizontally { -it }
                            } else {
                                slideInHorizontally { -it } togetherWith slideOutHorizontally { it }
                            }
                        },
                        label = "step_transition"
                    ) { step ->
                        when (step) {
                            0 -> BasicInfoStep(
                                uiState = uiState,
                                onTitleChange = viewModel::updateTitle,
                                onDescriptionChange = viewModel::updateDescription,
                                onCuisineChange = viewModel::updateCuisine,
                                onTagChange = viewModel::updateTag,
                                onDifficultyChange = viewModel::updateDifficulty,
                                onPrepTimeChange = viewModel::updatePrepTime,
                                onCookTimeChange = viewModel::updateCookTime,
                                onServingsChange = viewModel::updateServings
                            )
                            1 -> IngredientsStep(
                                ingredients = uiState.ingredients,
                                onAddIngredient = viewModel::addIngredient,
                                onUpdateIngredient = viewModel::updateIngredient,
                                onRemoveIngredient = viewModel::removeIngredient,
                                validationErrors = uiState.validationErrors
                            )
                            2 -> StepsStep(
                                steps = uiState.steps,
                                onAddStep = viewModel::addStep,
                                onUpdateStep = viewModel::updateStep,
                                onRemoveStep = viewModel::removeStep,
                                validationErrors = uiState.validationErrors
                            )
                            3 -> ReviewStep(uiState = uiState)
                        }
                    }
                }

                // Navigation buttons
                NavigationButtons(
                    canGoBack = uiState.canGoBack,
                    canGoForward = uiState.canGoForward,
                    isLastStep = uiState.isLastStep,
                    isSubmitting = uiState.isSubmitting,
                    onBack = viewModel::previousStep,
                    onNext = {
                        if (viewModel.validateCurrentStep()) {
                            viewModel.nextStep()
                        }
                    },
                    onSubmit = { viewModel.submitRecipe(onRecipeCreated) },
                    modifier = Modifier.padding(16.dp)
                )
            }

            // Error snackbar
            uiState.error?.let { error ->
                Snackbar(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp),
                    action = {
                        TextButton(onClick = viewModel::clearError) {
                            Text("Dismiss")
                        }
                    }
                ) {
                    Text(error)
                }
            }
        }
    }
}

@Composable
private fun StepIndicator(
    currentStep: Int,
    stepNames: List<String>,
    onStepClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalJustCookColors.current

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        stepNames.forEachIndexed { index, name ->
            if (index > 0) {
                HorizontalDivider(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 4.dp),
                    color = if (index <= currentStep) MaterialTheme.colorScheme.primary
                           else colors.border
                )
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.clickable { onStepClick(index) }
            ) {
                Surface(
                    shape = CircleShape,
                    color = if (index <= currentStep) MaterialTheme.colorScheme.primary
                           else colors.border,
                    modifier = Modifier.size(28.dp)
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        if (index < currentStep) {
                            Icon(
                                imageVector = Icons.Filled.Check,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                        } else {
                            Text(
                                text = "${index + 1}",
                                style = MaterialTheme.typography.labelMedium,
                                color = if (index <= currentStep) MaterialTheme.colorScheme.onPrimary
                                       else colors.textMuted
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = name,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (index <= currentStep) MaterialTheme.colorScheme.onBackground
                           else colors.textMuted
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BasicInfoStep(
    uiState: RecipeCreateUiState,
    onTitleChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onCuisineChange: (String) -> Unit,
    onTagChange: (String) -> Unit,
    onDifficultyChange: (Difficulty) -> Unit,
    onPrepTimeChange: (Int) -> Unit,
    onCookTimeChange: (Int) -> Unit,
    onServingsChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalJustCookColors.current

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            OutlinedTextField(
                value = uiState.title,
                onValueChange = onTitleChange,
                label = { Text("Title *") },
                placeholder = { Text("What's cooking?") },
                isError = uiState.validationErrors.containsKey("title"),
                supportingText = uiState.validationErrors["title"]?.let { { Text(it) } },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
        }

        item {
            OutlinedTextField(
                value = uiState.description,
                onValueChange = onDescriptionChange,
                label = { Text("Description *") },
                placeholder = { Text("Tell us about your recipe...") },
                isError = uiState.validationErrors.containsKey("description"),
                supportingText = uiState.validationErrors["description"]?.let { { Text(it) } },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                maxLines = 5
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Cuisine dropdown
                var cuisineExpanded by remember { mutableStateOf(false) }
                val cuisines = listOf("Italian", "Mexican", "Japanese", "Chinese", "Indian", "French", "American", "Thai", "Mediterranean", "Other")

                ExposedDropdownMenuBox(
                    expanded = cuisineExpanded,
                    onExpandedChange = { cuisineExpanded = it },
                    modifier = Modifier.weight(1f)
                ) {
                    OutlinedTextField(
                        value = uiState.cuisine,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Cuisine *") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = cuisineExpanded) },
                        isError = uiState.validationErrors.containsKey("cuisine"),
                        modifier = Modifier.menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = cuisineExpanded,
                        onDismissRequest = { cuisineExpanded = false }
                    ) {
                        cuisines.forEach { cuisine ->
                            DropdownMenuItem(
                                text = { Text(cuisine) },
                                onClick = {
                                    onCuisineChange(cuisine)
                                    cuisineExpanded = false
                                }
                            )
                        }
                    }
                }

                // Tag dropdown
                var tagExpanded by remember { mutableStateOf(false) }
                val tags = listOf("Main", "Appetizer", "Dessert", "Breakfast", "Snack", "Drink", "Side", "Soup", "Salad")

                ExposedDropdownMenuBox(
                    expanded = tagExpanded,
                    onExpandedChange = { tagExpanded = it },
                    modifier = Modifier.weight(1f)
                ) {
                    OutlinedTextField(
                        value = uiState.tag.ifBlank { "Main" },
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Category") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = tagExpanded) },
                        modifier = Modifier.menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = tagExpanded,
                        onDismissRequest = { tagExpanded = false }
                    ) {
                        tags.forEach { tag ->
                            DropdownMenuItem(
                                text = { Text(tag) },
                                onClick = {
                                    onTagChange(tag.lowercase())
                                    tagExpanded = false
                                }
                            )
                        }
                    }
                }
            }
        }

        item {
            // Difficulty selector
            Text(
                text = "Difficulty",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Difficulty.entries.forEach { difficulty ->
                    val selected = uiState.difficulty == difficulty
                    Surface(
                        onClick = { onDifficultyChange(difficulty) },
                        shape = RoundedCornerShape(8.dp),
                        color = if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                               else MaterialTheme.colorScheme.surface,
                        border = BorderStroke(
                            width = 1.dp,
                            color = if (selected) MaterialTheme.colorScheme.primary
                                   else colors.border
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(12.dp)
                        ) {
                            Text(
                                text = when (difficulty) {
                                    Difficulty.EASY -> "Easy"
                                    Difficulty.MEDIUM -> "Medium"
                                    Difficulty.HARD -> "Hard"
                                },
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
                            )
                        }
                    }
                }
            }
        }

        item {
            // Time inputs
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                TimeInput(
                    label = "Prep time",
                    minutes = uiState.prepTimeMinutes,
                    onMinutesChange = onPrepTimeChange,
                    modifier = Modifier.weight(1f)
                )
                TimeInput(
                    label = "Cook time",
                    minutes = uiState.cookTimeMinutes,
                    onMinutesChange = onCookTimeChange,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            // Servings
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Servings",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        onClick = { onServingsChange(uiState.servings - 1) },
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, colors.border),
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                            Icon(
                                imageVector = Icons.Filled.Remove,
                                contentDescription = "Decrease",
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    Text(
                        text = "${uiState.servings}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.width(40.dp),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )

                    Surface(
                        onClick = { onServingsChange(uiState.servings + 1) },
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, colors.border),
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                            Icon(
                                imageVector = Icons.Filled.Add,
                                contentDescription = "Increase",
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TimeInput(
    label: String,
    minutes: Int,
    onMinutesChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalJustCookColors.current

    Column(modifier = modifier) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = colors.textMuted
        )
        Spacer(modifier = Modifier.height(4.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.Schedule,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = colors.textMuted
            )
            OutlinedTextField(
                value = minutes.toString(),
                onValueChange = { it.toIntOrNull()?.let(onMinutesChange) },
                modifier = Modifier.weight(1f),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                suffix = { Text("min") }
            )
        }
    }
}

@Composable
private fun IngredientsStep(
    ingredients: List<EditableIngredient>,
    onAddIngredient: () -> Unit,
    onUpdateIngredient: (String, EditableIngredient) -> Unit,
    onRemoveIngredient: (String) -> Unit,
    validationErrors: Map<String, String>,
    modifier: Modifier = Modifier
) {
    val colors = LocalJustCookColors.current

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Ingredients",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                TextButton(onClick = onAddIngredient) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add")
                }
            }

            if (validationErrors.containsKey("ingredients")) {
                Text(
                    text = validationErrors["ingredients"]!!,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }

        itemsIndexed(
            items = ingredients,
            key = { _, item -> item.id }
        ) { index, ingredient ->
            IngredientEditCard(
                ingredient = ingredient,
                onUpdate = { onUpdateIngredient(ingredient.id, it) },
                onRemove = { onRemoveIngredient(ingredient.id) },
                hasError = validationErrors.containsKey("ingredient_${index}_name")
            )
        }

        if (ingredients.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = colors.border.copy(alpha = 0.3f)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "No ingredients yet",
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.textMuted
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        TextButton(onClick = onAddIngredient) {
                            Icon(
                                imageVector = Icons.Filled.Add,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Add your first ingredient")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun IngredientEditCard(
    ingredient: EditableIngredient,
    onUpdate: (EditableIngredient) -> Unit,
    onRemove: () -> Unit,
    hasError: Boolean,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = JustCookElevation.Low
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = ingredient.name,
                    onValueChange = { onUpdate(ingredient.copy(name = it)) },
                    label = { Text("Name *") },
                    isError = hasError,
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )

                IconButton(onClick = onRemove) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "Remove",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = ingredient.amount,
                    onValueChange = { onUpdate(ingredient.copy(amount = it)) },
                    label = { Text("Amount") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )

                OutlinedTextField(
                    value = ingredient.unit,
                    onValueChange = { onUpdate(ingredient.copy(unit = it)) },
                    label = { Text("Unit") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    placeholder = { Text("cups, tbsp...") }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = ingredient.notes ?: "",
                onValueChange = { onUpdate(ingredient.copy(notes = it.ifBlank { null })) },
                label = { Text("Notes (optional)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                placeholder = { Text("e.g., finely chopped") }
            )
        }
    }
}

@Composable
private fun StepsStep(
    steps: List<EditableStep>,
    onAddStep: () -> Unit,
    onUpdateStep: (String, String) -> Unit,
    onRemoveStep: (String) -> Unit,
    validationErrors: Map<String, String>,
    modifier: Modifier = Modifier
) {
    val colors = LocalJustCookColors.current

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Instructions",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                TextButton(onClick = onAddStep) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Step")
                }
            }

            if (validationErrors.containsKey("steps")) {
                Text(
                    text = validationErrors["steps"]!!,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }

        itemsIndexed(
            items = steps,
            key = { _, item -> item.id }
        ) { index, step ->
            StepEditCard(
                stepNumber = index + 1,
                step = step,
                onUpdate = { onUpdateStep(step.id, it) },
                onRemove = { onRemoveStep(step.id) },
                hasError = validationErrors.containsKey("step_$index")
            )
        }

        if (steps.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = colors.border.copy(alpha = 0.3f)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "No steps yet",
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.textMuted
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        TextButton(onClick = onAddStep) {
                            Icon(
                                imageVector = Icons.Filled.Add,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Add your first step")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StepEditCard(
    stepNumber: Int,
    step: EditableStep,
    onUpdate: (String) -> Unit,
    onRemove: () -> Unit,
    hasError: Boolean,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = JustCookElevation.Low
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Step number
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(28.dp)
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Text(
                        text = "$stepNumber",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }

            OutlinedTextField(
                value = step.instruction,
                onValueChange = onUpdate,
                label = { Text("Instruction *") },
                isError = hasError,
                modifier = Modifier.weight(1f),
                minLines = 2,
                maxLines = 4
            )

            IconButton(onClick = onRemove) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = "Remove",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
private fun ReviewStep(
    uiState: RecipeCreateUiState,
    modifier: Modifier = Modifier
) {
    val colors = LocalJustCookColors.current

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Review Your Recipe",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }

        // Basic info summary
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = JustCookElevation.Low
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = uiState.title.ifBlank { "Untitled Recipe" },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = uiState.description.ifBlank { "No description" },
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.textMuted
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        if (uiState.cuisine.isNotBlank()) {
                            Text(
                                text = uiState.cuisine,
                                style = MaterialTheme.typography.labelMedium,
                                color = colors.textMuted
                            )
                        }
                        Text(
                            text = "${uiState.prepTimeMinutes + uiState.cookTimeMinutes} min",
                            style = MaterialTheme.typography.labelMedium,
                            color = colors.textMuted
                        )
                        Text(
                            text = "${uiState.servings} servings",
                            style = MaterialTheme.typography.labelMedium,
                            color = colors.textMuted
                        )
                    }
                }
            }
        }

        // Ingredients summary
        item {
            Text(
                text = "Ingredients (${uiState.ingredients.size})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    uiState.ingredients.forEachIndexed { index, ingredient ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = ingredient.name,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            if (ingredient.amount.isNotBlank()) {
                                Text(
                                    text = "${ingredient.amount} ${ingredient.unit}".trim(),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = colors.textMuted
                                )
                            }
                        }
                        if (index < uiState.ingredients.lastIndex) {
                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = 8.dp),
                                color = colors.border.copy(alpha = 0.5f)
                            )
                        }
                    }
                }
            }
        }

        // Steps summary
        item {
            Text(
                text = "Instructions (${uiState.steps.size} steps)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        }

        itemsIndexed(uiState.steps) { index, step ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        Text(
                            text = "${index + 1}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }

                Text(
                    text = step.instruction,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun NavigationButtons(
    canGoBack: Boolean,
    canGoForward: Boolean,
    isLastStep: Boolean,
    isSubmitting: Boolean,
    onBack: () -> Unit,
    onNext: () -> Unit,
    onSubmit: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (canGoBack) {
            OutlinedButton(
                onClick = onBack,
                modifier = Modifier.weight(1f),
                enabled = !isSubmitting
            ) {
                Text("Back")
            }
        } else {
            Spacer(modifier = Modifier.weight(1f))
        }

        if (isLastStep) {
            Button(
                onClick = onSubmit,
                modifier = Modifier.weight(1f),
                enabled = !isSubmitting
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(if (isSubmitting) "Publishing..." else "Publish Recipe")
            }
        } else {
            Button(
                onClick = onNext,
                modifier = Modifier.weight(1f)
            ) {
                Text("Next")
            }
        }
    }
}
