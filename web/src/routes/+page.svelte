<script lang="ts">
	import { BadgeCheck, TrendingUp, Users, Sparkles } from 'lucide-svelte';
	import { goto } from '$app/navigation';
	import { page } from '$app/stores';
	import SearchBar from '$lib/components/ui/SearchBar.svelte';
	import RecipeCard from '$lib/components/recipe/RecipeCard.svelte';
	import LanguageToggle from '$lib/components/ui/LanguageToggle.svelte';
	import { languageMode, userLanguageCode } from '$lib/stores/languageMode';

	let { data } = $props();

	function handleLanguageToggle(newLang: string) {
		if (newLang === 'en') {
			goto('/', { replaceState: true });
		} else {
			goto(`/?lang=${newLang}`, { replaceState: true });
		}
	}

	// Sync URL with language store on mount/navigation
	$effect(() => {
		// Only sync if we have a user language set
		if (!$userLanguageCode) return;

		const urlLang = $page.url.searchParams.get('lang') || 'en';
		const expectedLang = $languageMode === 'local' ? $userLanguageCode : 'en';

		// If URL doesn't match store preference, update URL
		if (urlLang !== expectedLang) {
			if (expectedLang === 'en') {
				goto('/', { replaceState: true });
			} else {
				goto(`/?lang=${expectedLang}`, { replaceState: true });
			}
		}
	});
</script>

<div class="page container">
	<div class="hero">
		<div class="search-row">
			<SearchBar autofocus />
			<LanguageToggle onToggle={handleLanguageToggle} />
		</div>
		<p class="hint">Use hashtags for keywords. Examples: #indian #curry, or #pizza #vegan</p>
	</div>

	<div class="columns">
		<aside class="sidebar left">
			<section class="trending-chefs">
				<h2>
					<TrendingUp size={14} />
					Trending Chefs
				</h2>
				<p class="section-subtitle">Followers gained past week</p>
				{#if data.trendingChefs.length > 0}
					<ul class="chef-list">
						{#each data.trendingChefs as chef}
							<li>
								<a href="/chef/{chef.username}" class="chef-item">
									<div class="chef-photo">
										{#if chef.photoUrl}
											<img src={chef.photoUrl} alt={chef.fullName} />
										{:else}
											<span class="photo-fallback">{chef.fullName[0]}</span>
										{/if}
									</div>
									<div class="chef-info">
										<span class="chef-name">
											{chef.fullName}
											{#if chef.profileTier === 'author'}
												<BadgeCheck size={14} />
											{:else if chef.profileTier === 'chef'}
												<img src="/chef.svg" alt="Chef" class="chef-badge-icon" />
											{/if}
										</span>
										<span class="chef-stat"><Users size={12} /> +{chef.newFollowers}</span>
									</div>
								</a>
							</li>
						{/each}
					</ul>
				{:else}
					<p class="empty">No trending chefs yet.</p>
				{/if}
			</section>
		</aside>

		<main class="main">
			<section class="trending">
				<h2>Trending</h2>
				{#if data.trending.length > 0}
					<div class="recipes">
						{#each data.trending as recipe}
							<RecipeCard {...recipe} />
						{/each}
					</div>
				{:else}
					<p class="empty">No trending recipes yet. Be the first to publish!</p>
				{/if}
			</section>
		</main>

		<aside class="sidebar right">
			<section class="discover">
				<h2>
					<Sparkles size={14} />
					Discover
				</h2>
				{#if data.discover.length > 0}
					<ul class="discover-list">
						{#each data.discover as recipe}
							<li>
								<a href="/recipes/{recipe.slug}" class="discover-item">
									<div class="discover-image">
										{#if recipe.image}
											<img src={recipe.image} alt={recipe.title} />
										{:else}
											<span class="image-fallback">No image</span>
										{/if}
									</div>
									<div class="discover-info">
										<span class="discover-title">{recipe.title}</span>
										<span class="discover-author">by {recipe.authorName}</span>
									</div>
								</a>
							</li>
						{/each}
					</ul>
				{:else}
					<p class="empty">Check back soon for hidden gems.</p>
				{/if}
			</section>
		</aside>
	</div>
</div>

<style>
	.page {
		padding: 4rem 1.5rem;
		max-width: 1200px;
	}

	.hero {
		max-width: 600px;
		margin: 0 auto 3rem;
		text-align: center;
	}

	.search-row {
		display: flex;
		align-items: center;
		gap: 0.75rem;
	}

	.search-row :global(form.search) {
		flex: 1;
	}

	.hint {
		color: var(--color-text-muted);
		font-size: 0.8125rem;
		margin: 0.75rem 0 0;
	}

	.columns {
		display: grid;
		grid-template-columns: 220px 1fr 220px;
		gap: 3rem;
		align-items: start;
	}

	.sidebar {
		position: sticky;
		top: 2rem;
	}

	.main {
		max-width: 600px;
		justify-self: center;
		width: 100%;
	}

	h2 {
		display: flex;
		align-items: center;
		gap: 0.375rem;
		font-size: 0.75rem;
		font-weight: 600;
		color: var(--color-text-muted);
		text-transform: uppercase;
		letter-spacing: 0.05em;
		margin: 0;
	}

	.section-subtitle {
		font-size: 0.6875rem;
		color: var(--color-text-muted);
		margin: 0.25rem 0 1rem;
		opacity: 0.8;
	}

	.recipes {
		display: flex;
		flex-direction: column;
		gap: 1.25rem;
	}

	.empty {
		color: var(--color-text-muted);
		font-size: 0.875rem;
		text-align: center;
		padding: 2rem 0;
	}

	/* Trending Chefs */
	.chef-list {
		list-style: none;
		padding: 0;
		margin: 0;
		display: flex;
		flex-direction: column;
		gap: 0.25rem;
	}

	.chef-item {
		display: flex;
		align-items: center;
		gap: 0.75rem;
		padding: 0.5rem;
		border-radius: 6px;
		text-decoration: none;
		color: var(--color-text);
		transition: background-color 0.15s;
	}

	.chef-item:hover {
		background: var(--color-surface);
	}

	.chef-photo {
		width: 36px;
		height: 36px;
		border-radius: 50%;
		overflow: hidden;
		background: var(--color-border);
		display: flex;
		align-items: center;
		justify-content: center;
		flex-shrink: 0;
	}

	.chef-photo img {
		width: 100%;
		height: 100%;
		object-fit: cover;
	}

	.photo-fallback {
		font-size: 0.875rem;
		font-weight: 600;
		text-transform: uppercase;
		color: var(--color-text-muted);
	}

	.chef-info {
		display: flex;
		flex-direction: column;
		min-width: 0;
	}

	.chef-name {
		display: flex;
		align-items: center;
		gap: 0.25rem;
		font-size: 0.875rem;
		font-weight: 500;
		white-space: nowrap;
		overflow: hidden;
		text-overflow: ellipsis;
	}

	.chef-badge-icon {
		width: 14px;
		height: 14px;
		flex-shrink: 0;
	}

	:global(.dark) .chef-badge-icon {
		filter: invert(1);
	}

	.chef-stat {
		display: flex;
		align-items: center;
		gap: 0.25rem;
		font-size: 0.75rem;
		color: var(--color-text-muted);
	}

	/* Discover */
	.discover-list {
		list-style: none;
		padding: 0;
		margin: 0;
		display: flex;
		flex-direction: column;
		gap: 0.5rem;
	}

	.discover-item {
		display: flex;
		gap: 0.75rem;
		padding: 0.5rem;
		border-radius: 6px;
		text-decoration: none;
		color: var(--color-text);
		transition: background-color 0.15s;
	}

	.discover-item:hover {
		background: var(--color-surface);
	}

	.discover-image {
		width: 48px;
		height: 48px;
		border-radius: 4px;
		overflow: hidden;
		background: var(--color-border);
		display: flex;
		align-items: center;
		justify-content: center;
		flex-shrink: 0;
	}

	.discover-image img {
		width: 100%;
		height: 100%;
		object-fit: cover;
	}

	.image-fallback {
		font-size: 0.625rem;
		color: var(--color-text-muted);
		text-align: center;
	}

	.discover-info {
		display: flex;
		flex-direction: column;
		justify-content: center;
		min-width: 0;
	}

	.discover-title {
		font-size: 0.875rem;
		font-weight: 500;
		white-space: nowrap;
		overflow: hidden;
		text-overflow: ellipsis;
	}

	.discover-author {
		font-size: 0.75rem;
		color: var(--color-text-muted);
	}

	/* Tablet */
	@media (max-width: 1024px) {
		.columns {
			grid-template-columns: 1fr;
			gap: 2rem;
		}

		.sidebar {
			position: static;
		}

		.sidebar.left {
			order: 2;
		}

		.main {
			order: 1;
		}

		.sidebar.right {
			order: 3;
		}

		.chef-list,
		.discover-list {
			display: grid;
			grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
			gap: 0.5rem;
		}
	}

	/* Mobile */
	@media (max-width: 640px) {
		.page {
			padding: 2rem 1rem;
		}

		.hero {
			margin-bottom: 2rem;
		}

		.hint {
			font-size: 0.75rem;
		}

		.columns {
			gap: 2.5rem;
		}

		.trending-chefs,
		.discover {
			padding: 0 0.25rem;
		}

		.chef-list,
		.discover-list {
			grid-template-columns: 1fr;
		}

		.recipes {
			gap: 1rem;
		}
	}
</style>
