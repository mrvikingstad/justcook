<script lang="ts">
	import { searchEquipment, type EquipmentDef } from '$lib/data/equipment';

	interface Props {
		value: string;
		equipmentKey: string;
		placeholder?: string;
		onSelect?: (equipment: EquipmentDef | null) => void;
	}

	let {
		value = $bindable(),
		equipmentKey = $bindable(),
		placeholder = 'Search or add equipment...',
		onSelect
	}: Props = $props();

	let query = $state(value);
	let isOpen = $state(false);
	let highlightedIndex = $state(0);
	let inputElement: HTMLInputElement;

	const results = $derived(isOpen && query.length > 0 ? searchEquipment(query, 8) : []);
	const hasValidSelection = $derived(value.length > 0);
	const isCustomEntry = $derived(hasValidSelection && !equipmentKey);
	const showAddCustomOption = $derived(
		isOpen && query.length > 0 && !results.some((r) => r.name.toLowerCase() === query.toLowerCase())
	);

	function handleInput(e: Event) {
		const newValue = (e.target as HTMLInputElement).value;
		query = newValue;
		// Clear selection when user types
		value = '';
		equipmentKey = '';
		isOpen = true;
		highlightedIndex = 0;
	}

	function handleSelect(equipment: EquipmentDef) {
		value = equipment.name;
		equipmentKey = equipment.key;
		query = equipment.name;
		isOpen = false;
		onSelect?.(equipment);
	}

	function handleCustomEntry() {
		if (query.trim()) {
			value = query.trim();
			equipmentKey = ''; // Empty key indicates custom entry
			isOpen = false;
			onSelect?.(null);
		}
	}

	function handleKeydown(e: KeyboardEvent) {
		if (!isOpen) {
			if (e.key === 'ArrowDown' && query.length > 0) {
				isOpen = true;
			}
			return;
		}

		const totalOptions = results.length + (showAddCustomOption ? 1 : 0);

		switch (e.key) {
			case 'ArrowDown':
				e.preventDefault();
				highlightedIndex = Math.min(highlightedIndex + 1, totalOptions - 1);
				break;
			case 'ArrowUp':
				e.preventDefault();
				highlightedIndex = Math.max(highlightedIndex - 1, 0);
				break;
			case 'Enter':
				e.preventDefault();
				if (highlightedIndex < results.length && results[highlightedIndex]) {
					handleSelect(results[highlightedIndex]);
				} else if (showAddCustomOption && highlightedIndex === results.length) {
					handleCustomEntry();
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
			if (hasValidSelection) {
				// Restore display value if we have a valid selection
				query = value;
			} else if (query.trim()) {
				// Accept custom entry on blur
				value = query.trim();
				equipmentKey = '';
				onSelect?.(null);
			} else {
				// Clear empty input
				query = '';
				value = '';
				equipmentKey = '';
			}
		}, 200);
	}

	function handleOptionClick(e: MouseEvent, equipment: EquipmentDef) {
		e.preventDefault();
		e.stopPropagation();
		handleSelect(equipment);
	}

	function handleCustomClick(e: MouseEvent) {
		e.preventDefault();
		e.stopPropagation();
		handleCustomEntry();
	}
</script>

<div class="equipment-search">
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
		aria-expanded={isOpen && (results.length > 0 || showAddCustomOption)}
		aria-haspopup="listbox"
		class:has-selection={hasValidSelection}
		class:is-custom={isCustomEntry}
	/>

	{#if isOpen && (results.length > 0 || showAddCustomOption)}
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
					onmouseenter={() => (highlightedIndex = i)}
				>
					<span class="name">{result.name}</span>
					<span class="category">{result.category}</span>
				</button>
			{/each}

			{#if showAddCustomOption}
				<button
					type="button"
					role="option"
					aria-selected={highlightedIndex === results.length}
					class="dropdown-item add-custom"
					class:highlighted={highlightedIndex === results.length}
					onmousedown={(e) => e.preventDefault()}
					onclick={handleCustomClick}
					onmouseenter={() => (highlightedIndex = results.length)}
				>
					<span class="name">Add "{query}"</span>
					<span class="category">custom</span>
				</button>
			{/if}
		</div>
	{/if}
</div>

<style>
	.equipment-search {
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

	input.is-custom {
		border-style: dashed;
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

	.dropdown-item.add-custom {
		border-top: 1px solid var(--color-border);
		margin-top: 0.25rem;
		padding-top: 0.625rem;
	}

	.dropdown-item.add-custom .name {
		font-style: italic;
	}

	.dropdown-item.add-custom .category {
		color: var(--color-text-tertiary);
	}
</style>
