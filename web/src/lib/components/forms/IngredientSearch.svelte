<script lang="ts">
	import { searchIngredients, type IngredientDef } from '$lib/data/ingredients';

	interface Props {
		value: string;
		ingredientKey: string;
		placeholder?: string;
		onSelect?: (ingredient: IngredientDef) => void;
	}

	let {
		value = $bindable(),
		ingredientKey = $bindable(),
		placeholder = 'Search ingredient...',
		onSelect
	}: Props = $props();

	let query = $state(value);
	let isOpen = $state(false);
	let highlightedIndex = $state(0);
	let inputElement: HTMLInputElement;

	const results = $derived(isOpen && query.length > 0 ? searchIngredients(query, 8) : []);
	const hasValidSelection = $derived(!!ingredientKey && value.length > 0);

	function handleInput(e: Event) {
		const newValue = (e.target as HTMLInputElement).value;
		query = newValue;
		// Clear selection when user types - they must select from dropdown
		value = '';
		ingredientKey = '';
		isOpen = true;
		highlightedIndex = 0;
	}

	function handleSelect(ingredient: IngredientDef) {
		value = ingredient.name;
		ingredientKey = ingredient.key;
		query = ingredient.name;
		isOpen = false;
		onSelect?.(ingredient);
	}

	function handleKeydown(e: KeyboardEvent) {
		if (!isOpen || results.length === 0) {
			if (e.key === 'ArrowDown' && query.length > 0) {
				isOpen = true;
			}
			return;
		}

		switch (e.key) {
			case 'ArrowDown':
				e.preventDefault();
				highlightedIndex = Math.min(highlightedIndex + 1, results.length - 1);
				break;
			case 'ArrowUp':
				e.preventDefault();
				highlightedIndex = Math.max(highlightedIndex - 1, 0);
				break;
			case 'Enter':
				e.preventDefault();
				if (results[highlightedIndex]) {
					handleSelect(results[highlightedIndex]);
				}
				break;
			case 'Escape':
				isOpen = false;
				// Restore query if we have a valid selection
				if (hasValidSelection) {
					query = value;
				}
				break;
		}
	}

	function handleFocus() {
		// If user has a valid selection and clicks, allow them to search again
		if (hasValidSelection) {
			query = '';
			isOpen = true;
		} else if (query.length > 0) {
			isOpen = true;
		}
	}

	function handleBlur() {
		// Delay to allow click on dropdown item
		setTimeout(() => {
			isOpen = false;
			// Restore display value if we have a valid selection
			if (hasValidSelection) {
				query = value;
			} else {
				// Clear invalid input
				query = '';
				value = '';
				ingredientKey = '';
			}
		}, 200);
	}

	function handleOptionClick(e: MouseEvent, ingredient: IngredientDef) {
		e.preventDefault();
		e.stopPropagation();
		handleSelect(ingredient);
	}
</script>

<div class="ingredient-search">
	<input
		bind:this={inputElement}
		type="text"
		{placeholder}
		value={query}
		oninput={handleInput}
		onkeydown={handleKeydown}
		onfocus={handleFocus}
		onblur={handleBlur}
		autocomplete="off"
		role="combobox"
		aria-expanded={isOpen && results.length > 0}
		aria-haspopup="listbox"
		class:has-selection={hasValidSelection}
	/>

	{#if isOpen && results.length > 0}
		<div class="dropdown" role="listbox">
			{#each results as result, i}
				<button
					type="button"
					role="option"
					aria-selected={i === highlightedIndex}
					class="dropdown-item"
					class:highlighted={i === highlightedIndex}
					onmousedown={(e) => e.preventDefault()}
					onclick={(e) => handleOptionClick(e, result)}
					onmouseenter={() => highlightedIndex = i}
				>
					<span class="name">{result.name}</span>
					<span class="category">{result.category}</span>
				</button>
			{/each}
		</div>
	{/if}

	{#if isOpen && query.length > 0 && results.length === 0}
		<div class="dropdown no-results">
			<span>No ingredients found</span>
		</div>
	{/if}
</div>

<style>
	.ingredient-search {
		position: relative;
		flex: 1;
		min-width: 100px;
	}

	input {
		width: 100%;
		padding: 0.5rem 0.625rem;
		background: var(--color-bg);
		border: 1px solid var(--color-border);
		border-radius: 4px;
		font-family: inherit;
		font-size: 0.875rem;
		color: var(--color-text);
	}

	input:focus {
		outline: none;
		border-color: var(--color-text);
	}

	input::placeholder {
		color: var(--color-text-muted);
	}

	input.has-selection {
		background: var(--color-surface);
	}

	.dropdown {
		position: absolute;
		top: 100%;
		left: 0;
		right: 0;
		margin-top: 4px;
		background: var(--color-surface);
		border: 1px solid var(--color-border);
		border-radius: 6px;
		box-shadow: var(--shadow-elevation-medium);
		z-index: 9999;
		max-height: 240px;
		overflow-y: auto;
		padding: 0.25rem;
	}

	.dropdown-item {
		display: flex;
		justify-content: space-between;
		align-items: center;
		width: 100%;
		padding: 0.5rem 0.625rem;
		border: none;
		border-radius: 4px;
		background: none;
		cursor: pointer;
		font-family: inherit;
		text-align: left;
		color: var(--color-text);
		transition: background-color 0.1s;
	}

	.dropdown-item:hover,
	.dropdown-item.highlighted {
		background: var(--color-border);
	}

	.dropdown-item .name {
		font-size: 0.875rem;
	}

	.dropdown-item .category {
		font-size: 0.75rem;
		color: var(--color-text-muted);
		text-transform: capitalize;
	}

	.no-results {
		padding: 0.75rem;
		text-align: center;
		color: var(--color-text-muted);
		font-size: 0.875rem;
	}
</style>
