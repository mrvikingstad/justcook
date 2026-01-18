<script lang="ts">
	import { goto } from '$app/navigation';
	import { Clock, MessageCircle, Bookmark, BadgeCheck, Pencil, Trash2, Calendar } from 'lucide-svelte';
	import DifficultyIndicator from '$lib/components/ui/DifficultyIndicator.svelte';
	import ServingsAdjuster from '$lib/components/recipe/ServingsAdjuster.svelte';
	import IngredientList from '$lib/components/recipe/IngredientList.svelte';
	import StepList from '$lib/components/recipe/StepList.svelte';
	import TipsList from '$lib/components/recipe/TipsList.svelte';
	import EquipmentList from '$lib/components/recipe/EquipmentList.svelte';
	import VoteButtons from '$lib/components/recipe/VoteButtons.svelte';
	import { bookmarks, toggleBookmark } from '$lib/stores/bookmarks';

	let { data } = $props();
	const recipe = data.recipe;

	let servings = $state(recipe.baseServings);
	let isDeleting = $state(false);
	const multiplier = $derived(servings / recipe.baseServings);
	const totalTime = (recipe.prepTimeMinutes || 0) + (recipe.cookTimeMinutes || 0);
	const isBookmarked = $derived($bookmarks.includes(recipe.slug));

	function formatTime(minutes: number): string {
		if (minutes < 60) return `${minutes} min`;
		const h = Math.floor(minutes / 60);
		const m = minutes % 60;
		return m > 0 ? `${h}h ${m}m` : `${h}h`;
	}

	function formatDate(date: Date | string): string {
		const d = typeof date === 'string' ? new Date(date) : date;
		return d.toLocaleDateString('en-US', {
			month: 'short',
			day: 'numeric',
			year: 'numeric'
		});
	}

	async function handleDelete() {
		if (!confirm('Are you sure you want to delete this recipe? This cannot be undone.')) {
			return;
		}

		isDeleting = true;

		try {
			const response = await fetch(`/api/recipes/${recipe.id}`, {
				method: 'DELETE'
			});

			if (response.ok) {
				goto('/recipes');
			} else {
				const data = await response.json();
				alert(data.error || 'Failed to delete recipe');
			}
		} catch (e) {
			alert('Failed to delete recipe');
		} finally {
			isDeleting = false;
		}
	}
</script>

<svelte:head>
	<title>{recipe.title} - Just Cook</title>
</svelte:head>

<article class="recipe container">
	{#if recipe.image}
		<img src={recipe.image} alt={recipe.title} class="hero-image" />
	{/if}

	<header class="header">
		<h1>{recipe.title}</h1>
		<p class="author">
			by <a href="/chef/{recipe.authorUsername}">{recipe.authorName}</a>
			{#if recipe.authorProfileTier === 'author'}
				<span class="badge" title="Verified Author"><BadgeCheck size={16} /></span>
			{:else if recipe.authorProfileTier === 'chef'}
				<span class="badge" title="Chef"><img src="/chef.svg" alt="Chef" class="author-badge-icon" /></span>
			{/if}
		</p>

		<div class="meta">
			{#if totalTime > 0}
				<span class="meta-item">
					<Clock size={16} />
					{formatTime(totalTime)}
				</span>
			{/if}
			{#if recipe.difficulty}
				<DifficultyIndicator level={recipe.difficulty} size="md" />
			{/if}
			{#if recipe.cuisine}
				<span class="meta-item">{recipe.cuisine}</span>
			{/if}
			{#if recipe.tag}
				<span class="meta-item">{recipe.tag}</span>
			{/if}
		</div>

		<div class="timestamps">
			{#if recipe.publishedAt}
				<span class="timestamp">
					<Calendar size={14} />
					Published {formatDate(recipe.publishedAt)}
				</span>
			{/if}
			{#if recipe.updatedAt}
				<span class="timestamp edited">
					(Edited {formatDate(recipe.updatedAt)})
				</span>
			{/if}
		</div>

		{#if recipe.description}
			<p class="description">{recipe.description}</p>
		{/if}

		<div class="actions">
			<VoteButtons
				recipeId={recipe.id}
				upvotes={recipe.upvotes}
				downvotes={recipe.downvotes}
				userVote={recipe.userVote}
			/>
			<a href="/recipes/{recipe.slug}/comments" class="action-link">
				<MessageCircle size={18} />
				{recipe.commentCount} comments
			</a>
			<button class="action-link" class:saved={isBookmarked} onclick={() => toggleBookmark(recipe.slug)}>
				<Bookmark size={18} fill={isBookmarked ? 'currentColor' : 'none'} />
				{isBookmarked ? 'Saved' : 'Save'}
			</button>
		</div>

		{#if data.isOwn}
			<div class="owner-actions">
				<a href="/recipes/{recipe.slug}/edit" class="owner-btn edit">
					<Pencil size={16} />
					Edit Recipe
				</a>
				<button class="owner-btn delete" onclick={handleDelete} disabled={isDeleting}>
					<Trash2 size={16} />
					{isDeleting ? 'Deleting...' : 'Delete'}
				</button>
			</div>
		{/if}
	</header>

	<section class="ingredients-section">
		<div class="section-header">
			<h2>Ingredients</h2>
			<div class="servings">
				<span class="servings-label">Servings</span>
				<ServingsAdjuster bind:servings />
			</div>
		</div>
		<IngredientList ingredients={recipe.ingredients} {multiplier} />
	</section>

	{#if recipe.equipment.length > 0}
		<section class="equipment-section">
			<h2>Equipment Needed</h2>
			<EquipmentList equipment={recipe.equipment} />
		</section>
	{/if}

	<section class="steps-section">
		<h2>Instructions</h2>
		<StepList steps={recipe.steps} />
	</section>

	{#if recipe.tips.length > 0}
		<section class="tips-section">
			<h2>Tips</h2>
			<TipsList tips={recipe.tips} />
		</section>
	{/if}
</article>

<style>
	.recipe {
		padding: 2rem 1.5rem 4rem;
		max-width: 700px;
	}

	.hero-image {
		width: 100%;
		height: auto;
		aspect-ratio: 16 / 10;
		object-fit: cover;
		object-position: center;
		border-radius: 8px;
		margin-bottom: 2rem;
	}

	.header {
		margin-bottom: 3rem;
	}

	h1 {
		font-size: 2rem;
		font-weight: 700;
		margin: 0;
	}

	.author {
		display: flex;
		align-items: center;
		gap: 0.375rem;
		color: var(--color-text-muted);
		margin: 0.25rem 0 0;
	}

	.author a {
		color: inherit;
		text-decoration: none;
	}

	.author a:hover {
		text-decoration: underline;
	}

	.badge {
		display: flex;
		color: var(--color-text);
	}

	.author-badge-icon {
		width: 16px;
		height: 16px;
	}

	:global(.dark) .author-badge-icon {
		filter: invert(1);
	}

	.meta {
		display: flex;
		align-items: center;
		gap: 1rem;
		margin-top: 1rem;
		font-size: 0.875rem;
		color: var(--color-text-muted);
	}

	.meta-item {
		display: flex;
		align-items: center;
		gap: 0.25rem;
	}

	.timestamps {
		display: flex;
		align-items: center;
		gap: 0.5rem;
		margin-top: 0.75rem;
		font-size: 0.8125rem;
		color: var(--color-text-muted);
	}

	.timestamp {
		display: flex;
		align-items: center;
		gap: 0.25rem;
	}

	.timestamp.edited {
		font-style: italic;
	}

	.description {
		margin: 1.5rem 0 0;
		line-height: 1.6;
	}

	.actions {
		display: flex;
		align-items: center;
		gap: 1.5rem;
		margin-top: 1.5rem;
		padding-top: 1.5rem;
		border-top: 1px solid var(--color-border);
	}

	.action-link {
		display: flex;
		align-items: center;
		gap: 0.5rem;
		color: var(--color-text-muted);
		text-decoration: none;
		font-size: 0.875rem;
		background: none;
		border: none;
		cursor: pointer;
		padding: 0;
	}

	.action-link:hover {
		color: var(--color-text);
	}

	.action-link.saved {
		color: var(--color-text);
	}

	.owner-actions {
		display: flex;
		gap: 1rem;
		margin-top: 1.25rem;
		padding-top: 1.25rem;
		border-top: 1px solid var(--color-border);
	}

	.owner-btn {
		display: flex;
		align-items: center;
		gap: 0.5rem;
		padding: 0.5rem 1rem;
		font-family: inherit;
		font-size: 0.875rem;
		border-radius: 4px;
		cursor: pointer;
		transition: all 0.15s;
		text-decoration: none;
	}

	.owner-btn.edit {
		background: var(--color-surface);
		border: 1px solid var(--color-border);
		color: var(--color-text);
	}

	.owner-btn.edit:hover {
		border-color: var(--color-text);
	}

	.owner-btn.delete {
		background: none;
		border: 1px solid #b91c1c;
		color: #b91c1c;
	}

	.owner-btn.delete:hover:not(:disabled) {
		background: #b91c1c;
		color: white;
	}

	.owner-btn.delete:disabled {
		opacity: 0.5;
		cursor: not-allowed;
	}

	.ingredients-section,
	.equipment-section,
	.steps-section,
	.tips-section {
		margin-bottom: 3rem;
	}

	.section-header {
		display: flex;
		align-items: center;
		justify-content: space-between;
		margin-bottom: 1rem;
	}

	h2 {
		font-size: 1.25rem;
		font-weight: 600;
		margin: 0;
	}

	.servings {
		display: flex;
		align-items: center;
		gap: 0.75rem;
	}

	.servings-label {
		font-size: 0.875rem;
		color: var(--color-text-muted);
	}

	.steps-section h2,
	.equipment-section h2,
	.tips-section h2 {
		margin-bottom: 1rem;
	}

	/* Mobile */
	@media (max-width: 640px) {
		.recipe {
			padding: 1rem 1rem 3rem;
		}

		.hero-image {
			display: block;
			border-radius: 0;
			margin-left: -1rem;
			margin-right: -1rem;
			margin-bottom: 1.5rem;
			width: calc(100% + 2rem);
			max-width: none;
		}

		.header {
			margin-bottom: 2rem;
		}

		h1 {
			font-size: 1.5rem;
		}

		.author {
			font-size: 0.9375rem;
		}

		.meta {
			flex-wrap: wrap;
			gap: 0.625rem 1rem;
			font-size: 0.8125rem;
		}

		.timestamps {
			flex-wrap: wrap;
			font-size: 0.75rem;
		}

		.description {
			font-size: 0.9375rem;
			margin-top: 1rem;
		}

		.actions {
			flex-wrap: wrap;
			gap: 1rem 1.25rem;
			margin-top: 1.25rem;
			padding-top: 1.25rem;
		}

		.owner-actions {
			flex-wrap: wrap;
		}

		.owner-btn {
			font-size: 0.8125rem;
			padding: 0.5rem 0.875rem;
		}

		.ingredients-section,
		.equipment-section,
		.steps-section,
		.tips-section {
			margin-bottom: 2rem;
		}

		.section-header {
			flex-direction: column;
			align-items: flex-start;
			gap: 0.75rem;
		}

		h2 {
			font-size: 1.125rem;
		}

		.servings {
			width: 100%;
			justify-content: space-between;
		}
	}
</style>
