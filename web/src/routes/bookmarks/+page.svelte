<script lang="ts">
	import RecipeCard from '$lib/components/recipe/RecipeCard.svelte';

	let { data } = $props();
</script>

<svelte:head>
	<title>Bookmarks - Just Cook</title>
</svelte:head>

<div class="page container">
	<h1>Bookmarks</h1>

	{#if data.recipes.length > 0}
		<p class="count">{data.recipes.length} saved recipe{data.recipes.length === 1 ? '' : 's'}</p>
		<div class="recipes">
			{#each data.recipes as recipe}
				<RecipeCard {...recipe} />
			{/each}
		</div>
	{:else}
		<div class="empty">
			<p>No bookmarks yet.</p>
			<p class="hint">Save recipes to find them here later.</p>
		</div>
	{/if}
</div>

<style>
	.page {
		padding: 2rem 1.5rem 4rem;
		max-width: 600px;
	}

	h1 {
		font-size: 1.5rem;
		font-weight: 600;
		margin: 0;
	}

	.count {
		color: var(--color-text-muted);
		margin: 0.25rem 0 0;
	}

	.recipes {
		margin-top: 1rem;
	}

	.empty {
		margin-top: 3rem;
		text-align: center;
	}

	.empty p {
		margin: 0;
	}

	.hint {
		color: var(--color-text-muted);
		margin-top: 0.25rem;
	}
</style>
