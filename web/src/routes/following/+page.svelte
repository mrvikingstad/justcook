<script lang="ts">
	import RecipeCard from '$lib/components/recipe/RecipeCard.svelte';

	let { data } = $props();

	function formatTimestamp(date: Date | string | null): string {
		if (!date) return '';

		const d = typeof date === 'string' ? new Date(date) : date;
		const now = new Date();
		const diffMs = now.getTime() - d.getTime();
		const diffHours = Math.floor(diffMs / (1000 * 60 * 60));
		const diffDays = Math.floor(diffMs / (1000 * 60 * 60 * 24));

		if (diffHours < 1) {
			const diffMins = Math.floor(diffMs / (1000 * 60));
			return diffMins <= 1 ? 'just now' : `${diffMins}m ago`;
		}
		if (diffHours < 24) {
			return `${diffHours}h ago`;
		}
		if (diffDays < 7) {
			return `${diffDays}d ago`;
		}
		// For older posts, show date
		return d.toLocaleDateString('en-GB', { day: 'numeric', month: 'numeric' });
	}
</script>

<svelte:head>
	<title>Following - Just Cook</title>
</svelte:head>

<div class="page container">
	<h1>Following</h1>
	<p class="subtitle">New recipes from chefs you follow</p>

	{#if data.isEmpty}
		<div class="empty">
			<p>You're not following any chefs yet.</p>
			<p class="hint">Discover talented chefs and follow them to see their new recipes here.</p>
			<a href="/recipes" class="browse-btn">Browse recipes</a>
		</div>
	{:else if data.timelineItems.length === 0}
		<div class="empty">
			<p>No new recipes from chefs you follow.</p>
			<p class="hint">Check back later for new content.</p>
		</div>
	{:else}
		<div class="timeline">
			{#each data.timelineItems as item}
				<article class="timeline-item">
					<div class="timeline-header">
						<a href="/chef/{item.author.username}" class="author">
							<div class="author-photo">
								{#if item.author.photoUrl}
									<img src={item.author.photoUrl} alt={item.author.fullName} />
								{:else}
									<span class="photo-fallback">{item.author.fullName[0]}</span>
								{/if}
							</div>
							<span class="author-name">{item.author.fullName}</span>
						</a>
						<span class="action">published a new recipe</span>
						<span class="timestamp">{formatTimestamp(item.publishedAt)}</span>
					</div>
					<div class="recipe-wrapper">
						<RecipeCard {...item.recipe} />
					</div>
				</article>
			{/each}
		</div>
	{/if}
</div>

<style>
	.page {
		padding: 2rem 1.5rem 4rem;
		max-width: 600px;
	}

	h1 {
		font-size: 1.75rem;
		font-weight: 600;
		margin: 0;
	}

	.subtitle {
		color: var(--color-text-muted);
		margin: 0.25rem 0 0;
	}

	.empty {
		text-align: center;
		padding: 4rem 1rem;
	}

	.empty p {
		margin: 0;
		color: var(--color-text);
	}

	.empty .hint {
		color: var(--color-text-muted);
		margin-top: 0.5rem;
		font-size: 0.9375rem;
	}

	.browse-btn {
		display: inline-block;
		margin-top: 1.5rem;
		padding: 0.75rem 1.5rem;
		background: var(--color-text);
		color: var(--color-bg);
		text-decoration: none;
		border-radius: 4px;
		font-weight: 500;
		transition: opacity 0.15s;
	}

	.browse-btn:hover {
		opacity: 0.85;
	}

	.timeline {
		display: flex;
		flex-direction: column;
		gap: 2rem;
		margin-top: 2rem;
	}

	.timeline-item {
		border-bottom: 1px solid var(--color-border);
		padding-bottom: 2rem;
	}

	.timeline-item:last-child {
		border-bottom: none;
	}

	.timeline-header {
		display: flex;
		flex-wrap: wrap;
		align-items: center;
		gap: 0.375rem;
		margin-bottom: 1rem;
		font-size: 0.9375rem;
	}

	.author {
		display: flex;
		align-items: center;
		gap: 0.5rem;
		text-decoration: none;
		color: var(--color-text);
	}

	.author:hover .author-name {
		text-decoration: underline;
	}

	.author-photo {
		width: 28px;
		height: 28px;
		border-radius: 50%;
		overflow: hidden;
		background: var(--color-border);
		display: flex;
		align-items: center;
		justify-content: center;
		flex-shrink: 0;
	}

	.author-photo img {
		width: 100%;
		height: 100%;
		object-fit: cover;
	}

	.photo-fallback {
		font-size: 0.75rem;
		font-weight: 600;
		text-transform: uppercase;
		color: var(--color-text-muted);
	}

	.author-name {
		font-weight: 600;
	}

	.action {
		color: var(--color-text-muted);
	}

	.timestamp {
		color: var(--color-text-muted);
		font-size: 0.875rem;
	}

	.timestamp::before {
		content: '·';
		margin-right: 0.375rem;
	}

	.recipe-wrapper {
		margin: 0 -0.5rem;
	}
</style>
