<script lang="ts">
	import { Plus, Trash2, GripVertical, Image, Pencil, ChevronDown } from 'lucide-svelte';
	import { goto } from '$app/navigation';
	import { dndzone } from 'svelte-dnd-action';
	import IngredientSearch from '$lib/components/forms/IngredientSearch.svelte';
	import Select from '$lib/components/ui/Select.svelte';
	import { portal } from '$lib/actions/portal';
	import { cuisines } from '$lib/data/cuisines';
	import { languages, getLanguage, searchLanguages } from '$lib/data/languages';

	let { data } = $props();
	const recipe = data.recipe;

	// Form state - initialized with existing recipe data
	let title = $state(recipe.title);
	let description = $state(recipe.description);
	let cuisine = $state(recipe.cuisine);
	let tag = $state(recipe.tag);
	let difficulty = $state<'easy' | 'medium' | 'hard'>(recipe.difficulty || 'medium');
	let prepTime = $state<number | null>(recipe.prepTimeMinutes);
	let cookTime = $state<number | null>(recipe.cookTimeMinutes);
	let servings = $state<number | null>(recipe.servings);
	let photoFile = $state<File | null>(null);
	let photoPreview = $state<string | null>(recipe.photoUrl);

	// Language state
	let language = $state(recipe.language || 'en');
	let showOtherLanguage = $state(recipe.language !== 'en' && recipe.language !== data.userLanguageCode);
	let languageDropdownOpen = $state(false);
	let languageSearch = $state('');
	let languageTriggerEl: HTMLButtonElement;
	let languageSearchEl: HTMLInputElement;
	let languageDropdownX = $state(0);
	let languageDropdownY = $state(0);
	let languageDropdownWidth = $state(0);

	// Submission state
	let isSubmitting = $state(false);
	let error = $state('');

	interface Ingredient {
		id: string;
		name: string;
		ingredientKey: string;
		amount: string;
		unit: string;
		notes: string;
	}

	interface Step {
		id: string;
		instruction: string;
	}

	// Initialize ingredients from recipe data
	let ingredients = $state<Ingredient[]>(
		recipe.ingredients.length > 0
			? recipe.ingredients.map((i) => ({
					id: crypto.randomUUID(),
					name: i.name,
					ingredientKey: i.ingredientKey,
					amount: i.amount ? String(i.amount) : '',
					unit: i.unit,
					notes: i.notes
				}))
			: [{ id: crypto.randomUUID(), name: '', ingredientKey: '', amount: '', unit: '', notes: '' }]
	);

	// Initialize steps from recipe data
	let steps = $state<Step[]>(
		recipe.steps.length > 0
			? recipe.steps.map((instruction) => ({
					id: crypto.randomUUID(),
					instruction
				}))
			: [{ id: crypto.randomUUID(), instruction: '' }]
	);

	const commonUnits = ['g', 'kg', 'ml', 'L', 'tsp', 'tbsp', 'cup', 'oz', 'lb', 'piece', 'clove', 'pinch'];

	const suggestedTags = [
		'curry', 'pasta', 'soup', 'salad', 'stew', 'stir-fry', 'baked', 'grilled',
		'breakfast', 'lunch', 'dinner', 'snack', 'dessert', 'appetizer',
		'vegan', 'vegetarian', 'seafood', 'meat', 'poultry',
		'quick', 'comfort', 'healthy', 'indulgent'
	];

	const difficultyOptions = [
		{ value: 'easy', label: 'Easy' },
		{ value: 'medium', label: 'Medium' },
		{ value: 'hard', label: 'Hard' }
	];

	// Cuisine dropdown state
	let cuisineDropdownOpen = $state(false);
	let cuisineSearch = $state('');
	let cuisineTriggerEl: HTMLButtonElement;
	let cuisineSearchEl: HTMLInputElement;
	let cuisineDropdownX = $state(0);
	let cuisineDropdownY = $state(0);
	let cuisineDropdownWidth = $state(0);

	// Tag dropdown state
	let tagDropdownOpen = $state(false);
	let tagSearch = $state('');
	let tagTriggerEl: HTMLButtonElement;
	let tagSearchEl: HTMLInputElement;
	let tagDropdownX = $state(0);
	let tagDropdownY = $state(0);
	let tagDropdownWidth = $state(0);

	// Unit dropdown state (per ingredient, tracked by id)
	let unitDropdownOpenId = $state<string | null>(null);
	let unitSearch = $state('');
	let unitTriggerEls = $state<Record<string, HTMLButtonElement>>({});
	let unitSearchEl: HTMLInputElement;
	let unitDropdownX = $state(0);
	let unitDropdownY = $state(0);
	let unitDropdownWidth = $state(0);

	// Filtered lists
	const filteredCuisines = $derived(() => {
		const q = cuisineSearch.toLowerCase().trim();
		if (!q) return cuisines;
		return cuisines.filter((c) => c.toLowerCase().includes(q));
	});

	const filteredTags = $derived(() => {
		const q = tagSearch.toLowerCase().trim();
		if (!q) return suggestedTags;
		return suggestedTags.filter((t) => t.toLowerCase().includes(q));
	});

	const filteredUnits = $derived(() => {
		const q = unitSearch.toLowerCase().trim();
		if (!q) return commonUnits;
		return commonUnits.filter((u) => u.toLowerCase().includes(q));
	});

	const filteredLanguages = $derived(() => {
		return searchLanguages(languageSearch, 20);
	});

	// Check if user's language is different from English
	const userLanguageDef = $derived(data.userLanguageCode ? getLanguage(data.userLanguageCode) : null);
	const showUserLanguageOption = $derived(data.userLanguageCode && data.userLanguageCode !== 'en');

	// Dropdown handlers
	function toggleCuisineDropdown() {
		tagDropdownOpen = false;
		unitDropdownOpenId = null;

		if (!cuisineDropdownOpen && cuisineTriggerEl) {
			const rect = cuisineTriggerEl.getBoundingClientRect();
			cuisineDropdownX = rect.left;
			cuisineDropdownY = rect.bottom + 4;
			cuisineDropdownWidth = Math.max(rect.width, 200);
		}
		cuisineDropdownOpen = !cuisineDropdownOpen;
		if (cuisineDropdownOpen) {
			cuisineSearch = '';
			requestAnimationFrame(() => cuisineSearchEl?.focus());
		}
	}

	function selectCuisine(value: string) {
		cuisine = value;
		cuisineDropdownOpen = false;
	}

	function clearCuisine() {
		cuisine = '';
		cuisineDropdownOpen = false;
	}

	function toggleTagDropdown() {
		cuisineDropdownOpen = false;
		unitDropdownOpenId = null;

		if (!tagDropdownOpen && tagTriggerEl) {
			const rect = tagTriggerEl.getBoundingClientRect();
			tagDropdownX = rect.left;
			tagDropdownY = rect.bottom + 4;
			tagDropdownWidth = Math.max(rect.width, 200);
		}
		tagDropdownOpen = !tagDropdownOpen;
		if (tagDropdownOpen) {
			tagSearch = '';
			requestAnimationFrame(() => tagSearchEl?.focus());
		}
	}

	function selectTag(value: string) {
		tag = value;
		tagDropdownOpen = false;
	}

	function clearTag() {
		tag = '';
		tagDropdownOpen = false;
	}

	function toggleUnitDropdown(ingredientId: string) {
		cuisineDropdownOpen = false;
		tagDropdownOpen = false;

		if (unitDropdownOpenId !== ingredientId) {
			const triggerEl = unitTriggerEls[ingredientId];
			if (triggerEl) {
				const rect = triggerEl.getBoundingClientRect();
				unitDropdownX = rect.left;
				unitDropdownY = rect.bottom + 4;
				unitDropdownWidth = Math.max(rect.width, 120);
			}
			unitDropdownOpenId = ingredientId;
			unitSearch = '';
			requestAnimationFrame(() => unitSearchEl?.focus());
		} else {
			unitDropdownOpenId = null;
		}
	}

	function selectUnit(ingredientId: string, value: string) {
		const ing = ingredients.find(i => i.id === ingredientId);
		if (ing) {
			ing.unit = value;
			ingredients = [...ingredients];
		}
		unitDropdownOpenId = null;
	}

	function clearUnit(ingredientId: string) {
		const ing = ingredients.find(i => i.id === ingredientId);
		if (ing) {
			ing.unit = '';
			ingredients = [...ingredients];
		}
		unitDropdownOpenId = null;
	}

	function toggleLanguageDropdown() {
		cuisineDropdownOpen = false;
		tagDropdownOpen = false;
		unitDropdownOpenId = null;

		if (!languageDropdownOpen && languageTriggerEl) {
			const rect = languageTriggerEl.getBoundingClientRect();
			languageDropdownX = rect.left;
			languageDropdownY = rect.bottom + 4;
			languageDropdownWidth = Math.max(rect.width, 200);
		}
		languageDropdownOpen = !languageDropdownOpen;
		if (languageDropdownOpen) {
			languageSearch = '';
			requestAnimationFrame(() => languageSearchEl?.focus());
		}
	}

	function selectLanguage(code: string) {
		language = code;
		showOtherLanguage = code !== 'en' && code !== data.userLanguageCode;
		languageDropdownOpen = false;
	}

	function handleLanguageRadioChange(value: string) {
		if (value === 'other') {
			showOtherLanguage = true;
			toggleLanguageDropdown();
		} else {
			language = value;
			showOtherLanguage = false;
		}
	}

	function handleScroll() {
		if (cuisineDropdownOpen && cuisineTriggerEl) {
			const rect = cuisineTriggerEl.getBoundingClientRect();
			cuisineDropdownX = rect.left;
			cuisineDropdownY = rect.bottom + 4;
		}
		if (tagDropdownOpen && tagTriggerEl) {
			const rect = tagTriggerEl.getBoundingClientRect();
			tagDropdownX = rect.left;
			tagDropdownY = rect.bottom + 4;
		}
		if (unitDropdownOpenId && unitTriggerEls[unitDropdownOpenId]) {
			const rect = unitTriggerEls[unitDropdownOpenId].getBoundingClientRect();
			unitDropdownX = rect.left;
			unitDropdownY = rect.bottom + 4;
		}
		if (languageDropdownOpen && languageTriggerEl) {
			const rect = languageTriggerEl.getBoundingClientRect();
			languageDropdownX = rect.left;
			languageDropdownY = rect.bottom + 4;
		}
	}

	function handleClickOutside(e: MouseEvent) {
		const target = e.target as Node;

		if (cuisineDropdownOpen && cuisineTriggerEl && !cuisineTriggerEl.contains(target)) {
			const dropdown = document.querySelector('.cuisine-dropdown');
			if (dropdown && !dropdown.contains(target)) {
				cuisineDropdownOpen = false;
			}
		}
		if (tagDropdownOpen && tagTriggerEl && !tagTriggerEl.contains(target)) {
			const dropdown = document.querySelector('.tag-dropdown');
			if (dropdown && !dropdown.contains(target)) {
				tagDropdownOpen = false;
			}
		}
		if (unitDropdownOpenId) {
			const triggerEl = unitTriggerEls[unitDropdownOpenId];
			if (triggerEl && !triggerEl.contains(target)) {
				const dropdown = document.querySelector('.unit-dropdown');
				if (dropdown && !dropdown.contains(target)) {
					unitDropdownOpenId = null;
				}
			}
		}
		if (languageDropdownOpen && languageTriggerEl && !languageTriggerEl.contains(target)) {
			const dropdown = document.querySelector('.language-dropdown');
			if (dropdown && !dropdown.contains(target)) {
				languageDropdownOpen = false;
			}
		}
	}

	// DnD handlers
	const flipDurationMs = 150;

	function handleDndConsider(e: CustomEvent<{ items: Ingredient[] }>) {
		ingredients = e.detail.items;
	}

	function handleDndFinalize(e: CustomEvent<{ items: Ingredient[] }>) {
		ingredients = e.detail.items;
	}

	function addIngredient() {
		ingredients = [...ingredients, { id: crypto.randomUUID(), name: '', ingredientKey: '', amount: '', unit: '', notes: '' }];
	}

	function removeIngredient(id: string) {
		if (ingredients.length > 1) {
			ingredients = ingredients.filter((i) => i.id !== id);
		}
	}

	function addStep() {
		steps = [...steps, { id: crypto.randomUUID(), instruction: '' }];
	}

	function removeStep(id: string) {
		if (steps.length > 1) {
			steps = steps.filter((s) => s.id !== id);
		}
	}

	function handlePhotoChange(e: Event) {
		const input = e.target as HTMLInputElement;
		const file = input.files?.[0];
		if (file) {
			photoFile = file;
			const reader = new FileReader();
			reader.onload = () => {
				photoPreview = reader.result as string;
			};
			reader.readAsDataURL(file);
		}
	}

	function removePhoto() {
		photoFile = null;
		photoPreview = null;
	}

	async function handleSubmit(e: SubmitEvent) {
		e.preventDefault();
		if (isSubmitting) return;

		isSubmitting = true;
		error = '';

		// Get valid ingredients and steps
		const validIngredients = ingredients.filter((i) =>
			i.ingredientKey && String(i.amount).trim() && parseFloat(String(i.amount)) > 0 && i.unit.trim()
		);
		const validSteps = steps.filter((s) => s.instruction.trim());

		try {
			// Upload photo if a new one was selected
			let photoUrl: string | null | undefined = undefined;
			if (photoFile) {
				const formData = new FormData();
				formData.append('file', photoFile);
				formData.append('folder', 'recipes');

				const uploadResponse = await fetch('/api/upload', {
					method: 'POST',
					body: formData
				});

				const uploadResult = await uploadResponse.json();

				if (!uploadResponse.ok) {
					error = uploadResult.error || 'Failed to upload photo';
					isSubmitting = false;
					return;
				}

				photoUrl = uploadResult.url;
			} else if (photoPreview !== recipe.photoUrl) {
				// Photo was removed (photoPreview is null but originally had a photo)
				photoUrl = photoPreview;
			}

			const requestBody: Record<string, unknown> = {
				title: title.trim(),
				description: description.trim(),
				cuisine: cuisine.trim(),
				tag: tag.trim(),
				language,
				difficulty,
				prepTime,
				cookTime,
				servings,
				ingredients: validIngredients.map(i => ({
					...i,
					name: i.name.trim(),
					amount: String(i.amount).trim(),
					unit: i.unit.trim(),
					notes: i.notes.trim()
				})),
				steps: validSteps.map(s => ({
					...s,
					instruction: s.instruction.trim()
				}))
			};

			// Only include photoUrl if it changed
			if (photoUrl !== undefined) {
				requestBody.photoUrl = photoUrl;
			}

			const response = await fetch(`/api/recipes/${recipe.id}`, {
				method: 'PUT',
				headers: { 'Content-Type': 'application/json' },
				body: JSON.stringify(requestBody)
			});

			const result = await response.json();

			if (!response.ok) {
				error = result.error || 'Failed to update recipe';
			} else {
				goto(`/recipes/${result.slug}`);
			}
		} catch (err) {
			error = 'Something went wrong. Please try again.';
		} finally {
			isSubmitting = false;
		}
	}

	// Validation - check each requirement
	const hasTitle = $derived(title.trim().length > 0);
	const hasDescription = $derived(description.trim().length > 0);
	const hasCuisine = $derived(cuisine.length > 0);
	const hasTag = $derived(tag.length > 0);
	const hasLanguage = $derived(language.length > 0);
	const hasPrepTime = $derived(prepTime !== null && prepTime > 0);
	const hasCookTime = $derived(cookTime !== null && cookTime > 0);

	// Valid ingredients: must have ingredientKey (from DB), amount > 0, and unit
	const validIngredients = $derived(
		ingredients.filter((i) =>
			i.ingredientKey && String(i.amount).trim() && parseFloat(String(i.amount)) > 0 && i.unit.trim()
		)
	);
	const hasEnoughIngredients = $derived(validIngredients.length >= 2);

	// Valid steps: must have non-empty instruction
	const validSteps = $derived(steps.filter((s) => s.instruction.trim().length > 0));
	const hasEnoughSteps = $derived(validSteps.length >= 1);

	const canSubmit = $derived(
		hasTitle &&
		hasDescription &&
		hasCuisine &&
		hasTag &&
		hasLanguage &&
		hasPrepTime &&
		hasCookTime &&
		hasEnoughIngredients &&
		hasEnoughSteps &&
		!isSubmitting
	);
</script>

<svelte:head>
	<title>Edit Recipe - Just Cook</title>
</svelte:head>

<svelte:window onclick={handleClickOutside} onscroll={handleScroll} />

<div class="page container">
	<header class="page-header">
		<Pencil size={24} />
		<div>
			<h1>Edit Recipe</h1>
			<p class="subtitle">Update your recipe details.</p>
		</div>
	</header>

	{#if error}
		<p class="message error">{error}</p>
	{/if}

	<form onsubmit={handleSubmit}>
		<!-- Photo -->
		<section class="section">
			<div class="section-header">
				<h2>Photo</h2>
				<p class="hint">Show off your own cooking. Landscape orientation works best.</p>
			</div>

			{#if photoPreview}
				<div class="photo-preview">
					<img src={photoPreview} alt="Recipe preview" />
					<button type="button" class="remove-photo" onclick={removePhoto}>
						<Trash2 size={16} />
						Remove
					</button>
				</div>
			{:else}
				<label class="photo-upload">
					<input type="file" accept="image/*" onchange={handlePhotoChange} />
					<Image size={32} />
					<span>Click to upload a photo</span>
					<span class="photo-hint">JPG, PNG or WebP. Max 5MB.</span>
				</label>
			{/if}
		</section>

		<!-- Basics -->
		<section class="section">
			<div class="section-header">
				<h2>Basics</h2>
			</div>

			<div class="field">
				<label for="title">Title <span class="required">*</span></label>
				<input
					type="text"
					id="title"
					bind:value={title}
					placeholder="e.g. Butter Chicken"
					maxlength="100"
				/>
				<p class="hint">Use the dish's real name. If it's your own creation, keep it short and clear.</p>
			</div>

			<div class="field">
				<label for="description">Description <span class="required">*</span></label>
				<textarea
					id="description"
					bind:value={description}
					placeholder="e.g. Creamy, rich tomato-based curry with tender chicken pieces."
					rows="2"
					maxlength="200"
				></textarea>
				<p class="hint">Write it like a menu item. One or two sentences, no more.</p>
			</div>

			<div class="field-row">
				<div class="field">
					<label for="cuisine">Cuisine <span class="required">*</span></label>
					<button
						type="button"
						class="dropdown-trigger"
						onclick={toggleCuisineDropdown}
						bind:this={cuisineTriggerEl}
						aria-haspopup="listbox"
						aria-expanded={cuisineDropdownOpen}
					>
						<span class="dropdown-value" class:placeholder={!cuisine}>
							{cuisine || 'Select cuisine'}
						</span>
						<ChevronDown size={16} class="dropdown-chevron {cuisineDropdownOpen ? 'open' : ''}" />
					</button>
				</div>

				<div class="field">
					<label for="tag">Tag <span class="required">*</span></label>
					<button
						type="button"
						class="dropdown-trigger"
						onclick={toggleTagDropdown}
						bind:this={tagTriggerEl}
						aria-haspopup="listbox"
						aria-expanded={tagDropdownOpen}
					>
						<span class="dropdown-value" class:placeholder={!tag}>
							{tag || 'Select tag'}
						</span>
						<ChevronDown size={16} class="dropdown-chevron {tagDropdownOpen ? 'open' : ''}" />
					</button>
				</div>
			</div>
			<p class="hint">Cuisine is where it's from. Tag is what it is.</p>
		</section>

		{#if cuisineDropdownOpen}
			<div
				class="searchable-dropdown cuisine-dropdown"
				style="left: {cuisineDropdownX}px; top: {cuisineDropdownY}px; width: {cuisineDropdownWidth}px;"
				role="listbox"
				use:portal={'body'}
				onclick={(e) => e.stopPropagation()}
			>
				<div class="dropdown-search-wrapper">
					<input
						type="text"
						class="dropdown-search"
						placeholder="Search cuisines..."
						bind:value={cuisineSearch}
						bind:this={cuisineSearchEl}
					/>
				</div>
				<div class="dropdown-options">
					{#if cuisine}
						<button type="button" class="dropdown-option clear-option" onclick={clearCuisine}>
							Clear selection
						</button>
					{/if}
					{#each filteredCuisines() as c}
						<button
							type="button"
							class="dropdown-option"
							class:selected={c === cuisine}
							onclick={() => selectCuisine(c)}
							role="option"
							aria-selected={c === cuisine}
						>
							{c}
						</button>
					{:else}
						<div class="dropdown-empty">No cuisines found</div>
					{/each}
				</div>
			</div>
		{/if}

		{#if tagDropdownOpen}
			<div
				class="searchable-dropdown tag-dropdown"
				style="left: {tagDropdownX}px; top: {tagDropdownY}px; width: {tagDropdownWidth}px;"
				role="listbox"
				use:portal={'body'}
				onclick={(e) => e.stopPropagation()}
			>
				<div class="dropdown-search-wrapper">
					<input
						type="text"
						class="dropdown-search"
						placeholder="Search tags..."
						bind:value={tagSearch}
						bind:this={tagSearchEl}
					/>
				</div>
				<div class="dropdown-options">
					{#if tag}
						<button type="button" class="dropdown-option clear-option" onclick={clearTag}>
							Clear selection
						</button>
					{/if}
					{#each filteredTags() as t}
						<button
							type="button"
							class="dropdown-option"
							class:selected={t === tag}
							onclick={() => selectTag(t)}
							role="option"
							aria-selected={t === tag}
						>
							{t}
						</button>
					{:else}
						<div class="dropdown-empty">No tags found</div>
					{/each}
				</div>
			</div>
		{/if}

		<!-- Language -->
		<section class="section">
			<div class="section-header">
				<h2>Language</h2>
			</div>

			<div class="field">
				<label>Which language is this recipe written in? <span class="required">*</span></label>
				<div class="language-options">
					<label class="radio-option">
						<input
							type="radio"
							name="language"
							value="en"
							checked={language === 'en'}
							onchange={() => handleLanguageRadioChange('en')}
						/>
						<span>English</span>
					</label>

					{#if showUserLanguageOption && userLanguageDef}
						<label class="radio-option">
							<input
								type="radio"
								name="language"
								value={data.userLanguageCode}
								checked={language === data.userLanguageCode}
								onchange={() => handleLanguageRadioChange(data.userLanguageCode!)}
							/>
							<span>{userLanguageDef.name}</span>
						</label>
					{/if}

					<label class="radio-option">
						<input
							type="radio"
							name="language"
							value="other"
							checked={showOtherLanguage || (language !== 'en' && language !== data.userLanguageCode)}
							onchange={() => handleLanguageRadioChange('other')}
						/>
						<span>Other language...</span>
					</label>
				</div>

				{#if showOtherLanguage || (language !== 'en' && language !== data.userLanguageCode)}
					{@const selectedLang = getLanguage(language)}
					<button
						type="button"
						class="dropdown-trigger language-trigger"
						onclick={toggleLanguageDropdown}
						bind:this={languageTriggerEl}
						aria-haspopup="listbox"
						aria-expanded={languageDropdownOpen}
					>
						<span class="dropdown-value" class:placeholder={!selectedLang}>
							{selectedLang?.name || 'Select a language'}
						</span>
						<ChevronDown size={16} class="dropdown-chevron {languageDropdownOpen ? 'open' : ''}" />
					</button>
				{/if}
			</div>
		</section>

		{#if languageDropdownOpen}
			<div
				class="searchable-dropdown language-dropdown"
				style="left: {languageDropdownX}px; top: {languageDropdownY}px; width: {languageDropdownWidth}px;"
				role="listbox"
				use:portal={'body'}
				onclick={(e) => e.stopPropagation()}
			>
				<div class="dropdown-search-wrapper">
					<input
						type="text"
						class="dropdown-search"
						placeholder="Search languages..."
						bind:value={languageSearch}
						bind:this={languageSearchEl}
					/>
				</div>
				<div class="dropdown-options">
					{#each filteredLanguages() as lang}
						<button
							type="button"
							class="dropdown-option"
							class:selected={lang.code === language}
							onclick={() => selectLanguage(lang.code)}
							role="option"
							aria-selected={lang.code === language}
						>
							{lang.name}
						</button>
					{:else}
						<div class="dropdown-empty">No languages found</div>
					{/each}
				</div>
			</div>
		{/if}

		<!-- Time & Difficulty -->
		<section class="section">
			<div class="section-header">
				<h2>Time & Difficulty</h2>
				<p class="hint">Estimates are fine. Prep is everything before cooking. Cook is time with heat.</p>
			</div>

			<div class="field-row four">
				<div class="field">
					<label for="prep">Prep <span class="required">*</span></label>
					<div class="input-with-suffix">
						<input
							type="number"
							id="prep"
							bind:value={prepTime}
							placeholder="20"
							min="1"
							max="999"
						/>
						<span class="suffix">min</span>
					</div>
				</div>

				<div class="field">
					<label for="cook">Cook <span class="required">*</span></label>
					<div class="input-with-suffix">
						<input
							type="number"
							id="cook"
							bind:value={cookTime}
							placeholder="40"
							min="1"
							max="999"
						/>
						<span class="suffix">min</span>
					</div>
				</div>

				<div class="field">
					<label for="servings">Servings</label>
					<input
						type="number"
						id="servings"
						bind:value={servings}
						placeholder="4"
						min="1"
						max="99"
					/>
				</div>

				<div class="field">
					<label for="difficulty">Difficulty</label>
					<Select options={difficultyOptions} bind:value={difficulty} />
				</div>
			</div>
		</section>

		<!-- Ingredients -->
		<section class="section">
			<div class="section-header">
				<h2>Ingredients</h2>
				<p class="hint">Select ingredients from our database. Amount and unit are required. Drag to reorder.</p>
			</div>

			<div
				class="ingredients-list"
				use:dndzone={{ items: ingredients, flipDurationMs, dropTargetStyle: {} }}
				onconsider={handleDndConsider}
				onfinalize={handleDndFinalize}
			>
				{#each ingredients as ingredient (ingredient.id)}
					<div class="ingredient-row">
						<span class="grip"><GripVertical size={16} /></span>
						<input
							type="number"
							class="amount"
							bind:value={ingredient.amount}
							placeholder="200"
							min="0.01"
							step="any"
						/>
						<button
							type="button"
							class="unit-trigger"
							onclick={() => toggleUnitDropdown(ingredient.id)}
							bind:this={unitTriggerEls[ingredient.id]}
							aria-haspopup="listbox"
							aria-expanded={unitDropdownOpenId === ingredient.id}
						>
							<span class="unit-value" class:placeholder={!ingredient.unit}>
								{ingredient.unit || 'Unit'}
							</span>
							<ChevronDown size={14} class="unit-chevron {unitDropdownOpenId === ingredient.id ? 'open' : ''}" />
						</button>
						<IngredientSearch
							bind:value={ingredient.name}
							bind:ingredientKey={ingredient.ingredientKey}
							placeholder="Search ingredient..."
						/>
						<input
							type="text"
							class="notes"
							bind:value={ingredient.notes}
							placeholder="optional notes"
						/>
						<button
							type="button"
							class="remove"
							onclick={() => removeIngredient(ingredient.id)}
							disabled={ingredients.length === 1}
							aria-label="Remove ingredient"
						>
							<Trash2 size={16} />
						</button>
					</div>
				{/each}
			</div>

			{#if unitDropdownOpenId}
				{@const currentIngredient = ingredients.find(i => i.id === unitDropdownOpenId)}
				<div
					class="searchable-dropdown unit-dropdown"
					style="left: {unitDropdownX}px; top: {unitDropdownY}px; width: {unitDropdownWidth}px;"
					role="listbox"
					use:portal={'body'}
					onclick={(e) => e.stopPropagation()}
				>
					<div class="dropdown-search-wrapper">
						<input
							type="text"
							class="dropdown-search"
							placeholder="Search..."
							bind:value={unitSearch}
							bind:this={unitSearchEl}
						/>
					</div>
					<div class="dropdown-options">
						{#if currentIngredient?.unit}
							<button type="button" class="dropdown-option clear-option" onclick={() => clearUnit(unitDropdownOpenId!)}>
								Clear
							</button>
						{/if}
						{#each filteredUnits() as u}
							<button
								type="button"
								class="dropdown-option"
								class:selected={currentIngredient?.unit === u}
								onclick={() => selectUnit(unitDropdownOpenId!, u)}
								role="option"
								aria-selected={currentIngredient?.unit === u}
							>
								{u}
							</button>
						{:else}
							<div class="dropdown-empty">No units found</div>
						{/each}
					</div>
				</div>
			{/if}

			<button type="button" class="add-button" onclick={addIngredient}>
				<Plus size={16} />
				Add ingredient
			</button>
		</section>

		<!-- Steps -->
		<section class="section">
			<div class="section-header">
				<h2>Instructions</h2>
				<p class="hint">One action per step. Be clear and specific. Assume the reader is a beginner.</p>
			</div>

			<div class="steps-list">
				{#each steps as step, i (step.id)}
					<div class="step-row">
						<span class="step-number">{i + 1}</span>
						<textarea
							bind:value={step.instruction}
							placeholder={i === 0 ? "e.g. Mix yogurt with spices in a large bowl. Add chicken and marinate for at least 1 hour." : "Next step..."}
							rows="2"
						></textarea>
						<button
							type="button"
							class="remove"
							onclick={() => removeStep(step.id)}
							disabled={steps.length === 1}
							aria-label="Remove step"
						>
							<Trash2 size={16} />
						</button>
					</div>
				{/each}
			</div>

			<button type="button" class="add-button" onclick={addStep}>
				<Plus size={16} />
				Add step
			</button>
		</section>

		<!-- Submit -->
		<section class="section submit-section">
			<div class="requirements">
				<p class:met={hasTitle}>
					{hasTitle ? '✓' : '○'} Recipe has a title
				</p>
				<p class:met={hasDescription}>
					{hasDescription ? '✓' : '○'} Description provided
				</p>
				<p class:met={hasCuisine}>
					{hasCuisine ? '✓' : '○'} Cuisine selected
				</p>
				<p class:met={hasTag}>
					{hasTag ? '✓' : '○'} Tag selected
				</p>
				<p class:met={hasLanguage}>
					{hasLanguage ? '✓' : '○'} Language selected
				</p>
				<p class:met={hasPrepTime && hasCookTime}>
					{hasPrepTime && hasCookTime ? '✓' : '○'} Prep & cook time set
				</p>
				<p class:met={hasEnoughIngredients}>
					{hasEnoughIngredients ? '✓' : '○'} At least 2 complete ingredients (with amount & unit)
				</p>
				<p class:met={hasEnoughSteps}>
					{hasEnoughSteps ? '✓' : '○'} At least 1 instruction
				</p>
			</div>

			<div class="submit-buttons">
				<a href="/recipes/{recipe.slug}" class="cancel">Cancel</a>
				<button type="submit" class="submit" disabled={!canSubmit}>
					{isSubmitting ? 'Updating...' : 'Update Recipe'}
				</button>
			</div>
		</section>
	</form>
</div>

<style>
	.page {
		padding: 2rem 1.5rem 4rem;
		max-width: 600px;
	}

	.page-header {
		display: flex;
		align-items: center;
		gap: 1rem;
		margin-bottom: 2.5rem;
	}

	.page-header h1 {
		font-size: 1.5rem;
		font-weight: 600;
		margin: 0;
	}

	.subtitle {
		color: var(--color-text-muted);
		margin: 0.25rem 0 0;
	}

	.message {
		padding: 0.75rem 1rem;
		border-radius: 6px;
		font-size: 0.875rem;
		margin-bottom: 1.5rem;
	}

	.error {
		background: #fef2f2;
		color: #b91c1c;
	}

	:global(.dark) .error {
		background: #450a0a;
		color: #fca5a5;
	}

	.section {
		margin-bottom: 2.5rem;
		padding-bottom: 2.5rem;
		border-bottom: 1px solid var(--color-border);
	}

	.section-header {
		margin-bottom: 1rem;
	}

	.section-header h2 {
		font-size: 1rem;
		font-weight: 600;
		margin: 0;
	}

	.section-header .hint {
		margin: 0.25rem 0 0;
	}

	.hint {
		font-size: 0.8125rem;
		color: var(--color-text-muted);
		margin: 0.375rem 0 0;
	}

	.required {
		color: #b91c1c;
	}

	:global(.dark) .required {
		color: #f87171;
	}

	.field {
		margin-bottom: 1rem;
	}

	.field:last-child {
		margin-bottom: 0;
	}

	.field label {
		display: block;
		font-size: 0.875rem;
		font-weight: 500;
		margin-bottom: 0.375rem;
	}

	.field input,
	.field textarea,
	.field select {
		width: 100%;
		padding: 0.625rem 0.75rem;
		background: var(--color-surface);
		border: 1px solid var(--color-border);
		border-radius: 4px;
		font-family: inherit;
		font-size: 0.9375rem;
		color: var(--color-text);
	}

	.field input:focus,
	.field textarea:focus,
	.field select:focus {
		outline: none;
		border-color: var(--color-text);
	}

	.field input::placeholder,
	.field textarea::placeholder {
		color: var(--color-text-muted);
	}

	.field textarea {
		resize: vertical;
		min-height: 60px;
	}

	.field-row {
		display: grid;
		grid-template-columns: 1fr 1fr;
		gap: 1rem;
	}

	.field-row.four {
		grid-template-columns: repeat(4, 1fr);
	}

	.field-row .field {
		margin-bottom: 0;
	}

	.field-row + .hint {
		margin-top: 0.5rem;
	}

	/* Language options */
	.language-options {
		display: flex;
		flex-direction: column;
		gap: 0.5rem;
		margin-bottom: 0.75rem;
	}

	.radio-option {
		display: flex;
		align-items: center;
		gap: 0.5rem;
		cursor: pointer;
		font-size: 0.9375rem;
	}

	.radio-option input[type="radio"] {
		width: 18px;
		height: 18px;
		margin: 0;
		accent-color: var(--color-text);
		cursor: pointer;
	}

	.language-trigger {
		margin-top: 0.5rem;
	}

	.input-with-suffix {
		display: flex;
		align-items: center;
	}

	.input-with-suffix input {
		border-radius: 4px 0 0 4px;
		border-right: none;
	}

	.input-with-suffix .suffix {
		padding: 0.625rem 0.75rem;
		background: var(--color-border);
		border: 1px solid var(--color-border);
		border-radius: 0 4px 4px 0;
		font-size: 0.8125rem;
		color: var(--color-text-muted);
	}

	/* Dropdown trigger */
	.dropdown-trigger {
		display: flex;
		align-items: center;
		justify-content: space-between;
		width: 100%;
		padding: 0.625rem 0.75rem;
		background: var(--color-surface);
		border: 1px solid var(--color-border);
		border-radius: 4px;
		font-family: inherit;
		font-size: 0.9375rem;
		color: var(--color-text);
		cursor: pointer;
		transition: border-color 0.15s;
	}

	.dropdown-trigger:hover {
		border-color: var(--color-text-muted);
	}

	.dropdown-trigger:focus {
		outline: none;
		border-color: var(--color-text);
	}

	.dropdown-value {
		text-align: left;
	}

	.dropdown-value.placeholder {
		color: var(--color-text-muted);
	}

	:global(.dropdown-chevron) {
		color: var(--color-text-muted);
		transition: transform 0.2s;
		flex-shrink: 0;
	}

	:global(.dropdown-chevron.open) {
		transform: rotate(180deg);
	}

	/* Unit trigger (smaller) */
	.unit-trigger {
		display: flex;
		align-items: center;
		justify-content: space-between;
		gap: 0.25rem;
		width: 70px;
		flex-shrink: 0;
		padding: 0.5rem 0.5rem;
		background: var(--color-bg);
		border: 1px solid var(--color-border);
		border-radius: 4px;
		font-family: inherit;
		font-size: 0.875rem;
		color: var(--color-text);
		cursor: pointer;
		transition: border-color 0.15s;
	}

	.unit-trigger:hover {
		border-color: var(--color-text-muted);
	}

	.unit-trigger:focus {
		outline: none;
		border-color: var(--color-text);
	}

	.unit-value {
		text-align: left;
		overflow: hidden;
		text-overflow: ellipsis;
		white-space: nowrap;
	}

	.unit-value.placeholder {
		color: var(--color-text-muted);
	}

	:global(.unit-chevron) {
		color: var(--color-text-muted);
		transition: transform 0.2s;
		flex-shrink: 0;
	}

	:global(.unit-chevron.open) {
		transform: rotate(180deg);
	}

	/* Searchable dropdown */
	:global(.searchable-dropdown) {
		position: fixed;
		background: var(--color-surface);
		border: 1px solid var(--color-border);
		border-radius: 6px;
		box-shadow: var(--shadow-elevation-medium);
		z-index: 9999;
		overflow: hidden;
		max-height: 280px;
		display: flex;
		flex-direction: column;
	}

	:global(.dropdown-search-wrapper) {
		padding: 0.5rem;
		border-bottom: 1px solid var(--color-border);
	}

	:global(.dropdown-search) {
		width: 100%;
		padding: 0.5rem 0.75rem;
		background: var(--color-bg);
		border: 1px solid var(--color-border);
		border-radius: 4px;
		font-family: inherit;
		font-size: 0.875rem;
		color: var(--color-text);
	}

	:global(.dropdown-search:focus) {
		outline: none;
		border-color: var(--color-text-muted);
	}

	:global(.dropdown-search::placeholder) {
		color: var(--color-text-muted);
	}

	:global(.dropdown-options) {
		overflow-y: auto;
		flex: 1;
	}

	:global(.dropdown-option) {
		display: block;
		width: 100%;
		padding: 0.5rem 0.75rem;
		background: none;
		border: none;
		font-family: inherit;
		font-size: 0.875rem;
		color: var(--color-text);
		text-align: left;
		cursor: pointer;
		transition: background-color 0.1s;
	}

	:global(.dropdown-option:hover) {
		background: var(--color-border);
	}

	:global(.dropdown-option.selected) {
		background: var(--color-border);
		font-weight: 500;
	}

	:global(.dropdown-option.clear-option) {
		color: var(--color-text-muted);
		border-bottom: 1px solid var(--color-border);
	}

	:global(.dropdown-empty) {
		padding: 1rem;
		text-align: center;
		color: var(--color-text-muted);
		font-size: 0.875rem;
	}

	/* Photo */
	.photo-upload {
		display: flex;
		flex-direction: column;
		align-items: center;
		justify-content: center;
		gap: 0.5rem;
		padding: 2.5rem 1.5rem;
		background: var(--color-surface);
		border: 2px dashed var(--color-border);
		border-radius: 8px;
		color: var(--color-text-muted);
		cursor: pointer;
		transition: all 0.15s;
	}

	.photo-upload:hover {
		border-color: var(--color-text-muted);
	}

	.photo-upload input {
		display: none;
	}

	.photo-upload span {
		font-size: 0.9375rem;
	}

	.photo-hint {
		font-size: 0.8125rem !important;
		opacity: 0.7;
	}

	.photo-preview {
		position: relative;
	}

	.photo-preview img {
		width: 100%;
		height: auto;
		aspect-ratio: 16 / 10;
		object-fit: cover;
		border-radius: 8px;
	}

	.remove-photo {
		position: absolute;
		top: 0.75rem;
		right: 0.75rem;
		display: flex;
		align-items: center;
		gap: 0.375rem;
		padding: 0.5rem 0.75rem;
		background: rgba(0, 0, 0, 0.7);
		border: none;
		border-radius: 4px;
		color: white;
		font-size: 0.8125rem;
		cursor: pointer;
	}

	.remove-photo:hover {
		background: rgba(0, 0, 0, 0.85);
	}

	/* Ingredients */
	.ingredients-list {
		display: flex;
		flex-direction: column;
		gap: 0.5rem;
	}

	.ingredient-row {
		display: flex;
		align-items: center;
		gap: 0.5rem;
		background: var(--color-surface);
		padding: 0.25rem;
		border-radius: 4px;
	}

	.ingredient-row .grip {
		color: var(--color-border);
		cursor: grab;
		flex-shrink: 0;
		padding: 0.25rem;
	}

	.ingredient-row .grip:active {
		cursor: grabbing;
	}

	.ingredient-row input {
		padding: 0.5rem 0.625rem;
		background: var(--color-bg);
		border: 1px solid var(--color-border);
		border-radius: 4px;
		font-family: inherit;
		font-size: 0.875rem;
		color: var(--color-text);
	}

	.ingredient-row input:focus {
		outline: none;
		border-color: var(--color-text);
	}

	.ingredient-row input::placeholder {
		color: var(--color-text-muted);
	}

	.ingredient-row .amount {
		width: 60px;
		flex-shrink: 0;
	}

	.ingredient-row .notes {
		flex: 1;
		min-width: 80px;
	}

	.ingredient-row .remove,
	.step-row .remove {
		flex-shrink: 0;
		display: flex;
		align-items: center;
		justify-content: center;
		padding: 0.5rem;
		background: none;
		border: none;
		color: var(--color-text-muted);
		cursor: pointer;
		border-radius: 4px;
	}

	.ingredient-row .remove:hover:not(:disabled),
	.step-row .remove:hover:not(:disabled) {
		color: #b91c1c;
		background: rgba(185, 28, 28, 0.1);
	}

	.ingredient-row .remove:disabled,
	.step-row .remove:disabled {
		opacity: 0.3;
		cursor: not-allowed;
	}

	.add-button {
		display: flex;
		align-items: center;
		gap: 0.5rem;
		margin-top: 0.75rem;
		padding: 0.5rem 0.75rem;
		background: none;
		border: 1px dashed var(--color-border);
		border-radius: 4px;
		color: var(--color-text-muted);
		font-size: 0.875rem;
		cursor: pointer;
		transition: all 0.15s;
	}

	.add-button:hover {
		border-color: var(--color-text-muted);
		color: var(--color-text);
	}

	/* Steps */
	.steps-list {
		display: flex;
		flex-direction: column;
		gap: 0.75rem;
	}

	.step-row {
		display: flex;
		align-items: flex-start;
		gap: 0.75rem;
	}

	.step-number {
		flex-shrink: 0;
		width: 28px;
		height: 28px;
		display: flex;
		align-items: center;
		justify-content: center;
		background: var(--color-text);
		color: var(--color-bg);
		border-radius: 50%;
		font-size: 0.8125rem;
		font-weight: 600;
		margin-top: 0.375rem;
	}

	.step-row textarea {
		flex: 1;
		padding: 0.625rem 0.75rem;
		background: var(--color-surface);
		border: 1px solid var(--color-border);
		border-radius: 4px;
		font-family: inherit;
		font-size: 0.9375rem;
		color: var(--color-text);
		resize: vertical;
		min-height: 60px;
	}

	.step-row textarea:focus {
		outline: none;
		border-color: var(--color-text);
	}

	.step-row textarea::placeholder {
		color: var(--color-text-muted);
	}

	/* Submit */
	.submit-section {
		border-bottom: none;
		padding-bottom: 0;
	}

	.requirements {
		margin-bottom: 1.5rem;
	}

	.requirements p {
		margin: 0 0 0.375rem;
		font-size: 0.875rem;
		color: var(--color-text-muted);
	}

	.requirements p.met {
		color: var(--color-text);
	}

	.submit-buttons {
		display: flex;
		gap: 1rem;
	}

	.cancel {
		flex: 1;
		padding: 0.875rem 1.5rem;
		background: var(--color-surface);
		border: 1px solid var(--color-border);
		border-radius: 4px;
		color: var(--color-text);
		font-family: inherit;
		font-size: 1rem;
		font-weight: 500;
		text-align: center;
		text-decoration: none;
		cursor: pointer;
		transition: border-color 0.15s;
	}

	.cancel:hover {
		border-color: var(--color-text);
	}

	.submit {
		flex: 2;
		padding: 0.875rem 1.5rem;
		background: var(--color-text);
		border: none;
		border-radius: 4px;
		color: var(--color-bg);
		font-family: inherit;
		font-size: 1rem;
		font-weight: 500;
		cursor: pointer;
		transition: opacity 0.15s;
	}

	.submit:hover:not(:disabled) {
		opacity: 0.85;
	}

	.submit:disabled {
		opacity: 0.4;
		cursor: not-allowed;
	}

	/* Responsive */
	@media (max-width: 500px) {
		.field-row.four {
			grid-template-columns: 1fr 1fr;
		}

		.ingredient-row {
			flex-wrap: wrap;
		}

		.ingredient-row .grip {
			display: none;
		}

		.ingredient-row .notes {
			flex: 1 1 100%;
		}

		.submit-buttons {
			flex-direction: column;
		}

		.cancel, .submit {
			flex: none;
		}
	}
</style>
