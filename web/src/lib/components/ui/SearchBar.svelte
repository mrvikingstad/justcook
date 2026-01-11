<script lang="ts">
	import { goto } from '$app/navigation';

	interface Props {
		placeholder?: string;
		value?: string;
		autofocus?: boolean;
		onSearch?: (query: string) => void;
	}

	let { placeholder = 'What do you want to cook?', value = $bindable(''), autofocus = false, onSearch }: Props = $props();

	function handleSubmit(e: SubmitEvent) {
		e.preventDefault();
		const trimmed = value.trim();

		if (onSearch) {
			// Use callback if provided (controlled mode)
			onSearch(trimmed);
		} else if (trimmed) {
			// Default navigation behavior
			goto(`/recipes?q=${encodeURIComponent(trimmed)}`);
		}
	}

	function handleKeydown(e: KeyboardEvent) {
		if (e.key === 'Escape') {
			value = '';
		}
	}
</script>

<form class="search" onsubmit={handleSubmit}>
	<input
		type="text"
		bind:value
		{placeholder}
		{autofocus}
		onkeydown={handleKeydown}
		aria-label="Search recipes"
	/>
</form>

<style>
	.search {
		width: 100%;
	}

	input {
		width: 100%;
		padding: 1rem 0;
		font-family: inherit;
		font-size: 1.25rem;
		background: transparent;
		border: none;
		border-bottom: 1px solid var(--color-border);
		color: var(--color-text);
		outline: none;
		transition: border-color 0.2s;
	}

	input::placeholder {
		color: var(--color-text-muted);
	}

	input:focus {
		border-bottom-color: var(--color-text);
	}

	@media (max-width: 480px) {
		input {
			font-size: 1rem;
			padding: 0.875rem 0;
		}
	}
</style>
