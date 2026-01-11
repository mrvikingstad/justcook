<script lang="ts">
	import { Minus, Plus } from 'lucide-svelte';

	interface Props {
		servings: number;
		min?: number;
		max?: number;
	}

	let { servings = $bindable(), min = 1, max = 24 }: Props = $props();

	function decrease() {
		if (servings > min) servings--;
	}

	function increase() {
		if (servings < max) servings++;
	}
</script>

<div class="adjuster">
	<button onclick={decrease} disabled={servings <= min} aria-label="Decrease servings">
		<Minus size={16} />
	</button>
	<span class="value">{servings}</span>
	<button onclick={increase} disabled={servings >= max} aria-label="Increase servings">
		<Plus size={16} />
	</button>
</div>

<style>
	.adjuster {
		display: inline-flex;
		align-items: center;
		gap: 0.75rem;
	}

	button {
		display: flex;
		align-items: center;
		justify-content: center;
		width: 32px;
		height: 32px;
		background: transparent;
		border: 1px solid var(--color-border);
		border-radius: 4px;
		color: var(--color-text);
		transition: border-color 0.15s;
	}

	button:hover:not(:disabled) {
		border-color: var(--color-text);
	}

	button:disabled {
		opacity: 0.3;
		cursor: not-allowed;
	}

	.value {
		min-width: 2rem;
		text-align: center;
		font-weight: 500;
	}
</style>
