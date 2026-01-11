<script lang="ts">
	interface Ingredient {
		name: string;
		amount: number | null;
		unit: string | null;
		notes?: string | null;
	}

	interface Props {
		ingredients: Ingredient[];
		multiplier?: number;
	}

	let { ingredients, multiplier = 1 }: Props = $props();

	function formatAmount(amount: number | null): string {
		if (amount === null) return '';
		const scaled = amount * multiplier;
		if (scaled === Math.floor(scaled)) return scaled.toString();
		return scaled.toFixed(1).replace(/\.0$/, '');
	}
</script>

<ul class="ingredients">
	{#each ingredients as ingredient}
		<li><span class="quantity">{#if ingredient.amount}<span class="amount">{formatAmount(ingredient.amount)}</span>{/if}{#if ingredient.unit} <span class="unit">{ingredient.unit}</span>{/if}</span> <span class="name">{ingredient.name}{#if ingredient.notes}, <span class="notes">{ingredient.notes}</span>{/if}</span></li>
	{/each}
</ul>

<style>
	.ingredients {
		list-style: none;
		padding: 0;
		margin: 0;
	}

	li {
		padding: 0.5rem 0;
		border-bottom: 1px solid var(--color-border);
	}

	li:last-child {
		border-bottom: none;
	}

	.quantity {
		font-weight: 500;
	}

	.unit {
		font-weight: 400;
		color: var(--color-text-muted);
	}

	.notes {
		color: var(--color-text-muted);
		font-style: italic;
	}

	@media (max-width: 640px) {
		li {
			font-size: 0.9375rem;
			padding: 0.625rem 0;
		}
	}
</style>
