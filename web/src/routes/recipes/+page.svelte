<script lang="ts">
	import { Clock, ChevronLeft, ChevronRight } from 'lucide-svelte';
	import { goto } from '$app/navigation';
	import { page } from '$app/stores';
	import RecipeCard from '$lib/components/recipe/RecipeCard.svelte';
	import Select from '$lib/components/ui/Select.svelte';
	import SearchBar from '$lib/components/ui/SearchBar.svelte';
	import LanguageToggle from '$lib/components/ui/LanguageToggle.svelte';
	import { languageMode, userLanguageCode } from '$lib/stores/languageMode';

	let { data } = $props();

	let sortBy = $state(data.filters.sortBy);
	let difficulty = $state(data.filters.difficulty);
	let maxTime = $state(data.filters.maxTime);
	let selectedTag = $state<string | null>(data.filters.tag);
	let searchQuery = $state(data.filters.q || '');

	// Derive current language from mode
	const currentLang = $derived(
		$languageMode === 'local' && $userLanguageCode ? $userLanguageCode : 'en'
	);

	function updateFilters(lang?: string) {
		const params = new URLSearchParams();
		if (searchQuery) params.set('q', searchQuery);
		if (sortBy !== 'total') params.set('sort', sortBy);
		if (difficulty !== 'all') params.set('difficulty', difficulty);
		if (maxTime !== 'all') params.set('maxTime', maxTime);
		if (selectedTag) params.set('tag', selectedTag);
		// Use provided lang or current derived lang
		const effectiveLang = lang ?? currentLang;
		if (effectiveLang !== 'en') params.set('lang', effectiveLang);
		params.set('page', '1');

		const queryString = params.toString();
		goto(`/recipes${queryString ? `?${queryString}` : ''}`, { replaceState: true });
	}

	function handleLanguageToggle(newLang: string) {
		updateFilters(newLang);
	}

	function handleSearch(query: string) {
		searchQuery = query;
		updateFilters();
	}

	function goToPage(pageNum: number) {
		const params = new URLSearchParams($page.url.searchParams);
		params.set('page', pageNum.toString());
		goto(`/recipes?${params.toString()}`);
	}

	function clearFilters() {
		sortBy = 'total';
		difficulty = 'all';
		maxTime = 'all';
		selectedTag = null;
		searchQuery = '';
		goto('/recipes', { replaceState: true });
	}

	function toggleTag(tag: string) {
		selectedTag = selectedTag === tag ? null : tag;
		updateFilters();
	}

	const hasActiveFilters = $derived(
		difficulty !== 'all' || maxTime !== 'all' || selectedTag !== null || searchQuery !== ''
	);

	// Sync URL with language store on mount/navigation
	$effect(() => {
		// Only sync if we have a user language set
		if (!$userLanguageCode) return;

		const urlLang = $page.url.searchParams.get('lang') || 'en';
		const expectedLang = $languageMode === 'local' ? $userLanguageCode : 'en';

		// If URL doesn't match store preference, update URL
		if (urlLang !== expectedLang) {
			const params = new URLSearchParams($page.url.searchParams);
			if (expectedLang === 'en') {
				params.delete('lang');
			} else {
				params.set('lang', expectedLang);
			}
			const queryString = params.toString();
			goto(`/recipes${queryString ? `?${queryString}` : ''}`, { replaceState: true });
		}
	});
</script>

<svelte:head>
	<title>Recipes - Just Cook</title>
</svelte:head>

<div class="page container">
	<div class="page-header">
		<h1>Recipes</h1>
		<a href="/recipes/new" class="publish-btn">Publish</a>
	</div>

	<section class="search-section">
		<div class="search-row">
			<SearchBar
				bind:value={searchQuery}
				placeholder="Search recipes... (use # for cuisine/tags)"
				onSearch={handleSearch}
			/>
			<LanguageToggle onToggle={handleLanguageToggle} />
		</div>
		{#if searchQuery}
			<p class="search-info">
				Showing results for "{searchQuery}"
			</p>
		{/if}
	</section>

	<section class="tags">
		<h2>Popular tags</h2>
		<div class="tag-list">
			{#each data.popularTags as tag}
				<button
					class="tag"
					class:active={selectedTag === tag}
					onclick={() => toggleTag(tag)}
				>
					{tag}
				</button>
			{/each}
		</div>
	</section>

	<div class="controls">
		<div class="sort">
			<span class="label">Sort by</span>
			<div class="sort-options">
				<button class:active={sortBy === 'total'} onclick={() => { sortBy = 'total'; updateFilters(); }}>
					Top
				</button>
				<button class:active={sortBy === 'month'} onclick={() => { sortBy = 'month'; updateFilters(); }}>
					This month
				</button>
				<button class:active={sortBy === 'week'} onclick={() => { sortBy = 'week'; updateFilters(); }}>
					This week
				</button>
				<button class:active={sortBy === 'latest'} onclick={() => { sortBy = 'latest'; updateFilters(); }}>
					Latest
				</button>
			</div>
		</div>

		<div class="filters">
			<div class="filter">
				<img src="/chef.svg" alt="" class="filter-icon chef-icon" />
				<Select
					value={difficulty}
					onchange={(val) => { difficulty = val; updateFilters(); }}
					options={[
						{ value: 'all', label: 'Any difficulty' },
						{ value: 'easy', label: 'Easy' },
						{ value: 'medium', label: 'Medium' },
						{ value: 'hard', label: 'Hard' }
					]}
				/>
			</div>

			<div class="filter">
				<Clock size={14} />
				<Select
					value={maxTime}
					onchange={(val) => { maxTime = val; updateFilters(); }}
					options={[
						{ value: 'all', label: 'Any time' },
						{ value: '15', label: 'Under 15 min' },
						{ value: '30', label: 'Under 30 min' },
						{ value: '60', label: 'Under 1 hour' }
					]}
				/>
			</div>

			{#if hasActiveFilters}
				<button class="clear" onclick={clearFilters}>Clear filters</button>
			{/if}
		</div>
	</div>

	<div class="results">
		<p class="count">{data.pagination.totalCount} recipe{data.pagination.totalCount === 1 ? '' : 's'}</p>

		{#if data.recipes.length > 0}
			<div class="recipes">
				{#each data.recipes as recipe}
					<RecipeCard {...recipe} />
				{/each}
			</div>

			{#if data.pagination.totalPages > 1}
				<nav class="pagination">
					<button
						class="page-btn"
						disabled={!data.pagination.hasPrev}
						onclick={() => goToPage(data.pagination.page - 1)}
					>
						<ChevronLeft size={16} />
						<span class="btn-text">Previous</span>
					</button>

					<span class="page-info">
						Page {data.pagination.page} of {data.pagination.totalPages}
					</span>

					<button
						class="page-btn"
						disabled={!data.pagination.hasNext}
						onclick={() => goToPage(data.pagination.page + 1)}
					>
						<span class="btn-text">Next</span>
						<ChevronRight size={16} />
					</button>
				</nav>
			{/if}
		{:else}
			<p class="empty">No recipes match your filters.</p>
		{/if}
	</div>
</div>

<style>
	.page {
		padding: 2rem 1.5rem 4rem;
		max-width: 1200px;
	}

	.page-header {
		display: flex;
		align-items: center;
		justify-content: space-between;
	}

	h1 {
		font-size: 1.5rem;
		font-weight: 600;
		margin: 0;
	}

	.publish-btn {
		padding: 0.5rem 1rem;
		background: var(--color-text);
		color: var(--color-bg);
		text-decoration: none;
		font-size: 0.875rem;
		font-weight: 500;
		border-radius: 4px;
		transition: opacity 0.15s;
	}

	.publish-btn:hover {
		opacity: 0.85;
	}

	.search-section {
		margin-top: 1.5rem;
	}

	.search-row {
		display: flex;
		align-items: center;
		gap: 1rem;
	}

	.search-row :global(form.search) {
		flex: 1;
	}

	.search-info {
		font-size: 0.875rem;
		color: var(--color-text-muted);
		margin: 0.5rem 0 0;
	}

	.tags {
		margin-top: 1.5rem;
	}

	.tags h2 {
		font-size: 0.75rem;
		font-weight: 500;
		color: var(--color-text-muted);
		text-transform: uppercase;
		letter-spacing: 0.05em;
		margin: 0 0 0.75rem;
	}

	.tag-list {
		display: flex;
		flex-wrap: wrap;
		gap: 0.5rem;
	}

	.tag {
		padding: 0.375rem 0.75rem;
		background: var(--color-surface);
		border: 1px solid var(--color-border);
		border-radius: 100px;
		font-size: 0.8125rem;
		color: var(--color-text-muted);
		cursor: pointer;
		transition: all 0.15s;
		text-transform: capitalize;
	}

	.tag:hover {
		border-color: var(--color-text-muted);
	}

	.tag.active {
		background: var(--color-text);
		border-color: var(--color-text);
		color: var(--color-bg);
	}

	.controls {
		margin-top: 2rem;
		padding-top: 1.5rem;
		border-top: 1px solid var(--color-border);
	}

	.sort {
		display: flex;
		align-items: center;
		gap: 1rem;
	}

	.label {
		font-size: 0.8125rem;
		color: var(--color-text-muted);
	}

	.sort-options {
		display: flex;
		gap: 0;
	}

	.sort-options button {
		padding: 0.375rem 0.75rem;
		background: none;
		border: 1px solid var(--color-border);
		font-size: 0.8125rem;
		color: var(--color-text-muted);
		cursor: pointer;
		transition: all 0.15s;
	}

	.sort-options button:first-child {
		border-radius: 4px 0 0 4px;
	}

	.sort-options button:last-child {
		border-radius: 0 4px 4px 0;
	}

	.sort-options button:not(:first-child) {
		margin-left: -1px;
	}

	.sort-options button:hover {
		color: var(--color-text);
	}

	.sort-options button.active {
		background: var(--color-text);
		border-color: var(--color-text);
		color: var(--color-bg);
		position: relative;
		z-index: 1;
	}

	.filters {
		display: flex;
		align-items: center;
		gap: 1rem;
		margin-top: 1rem;
		flex-wrap: wrap;
	}

	.filter {
		display: flex;
		align-items: center;
		gap: 0.5rem;
		color: var(--color-text-muted);
	}

	.filter-icon {
		width: 14px;
		height: 14px;
	}

	.chef-icon {
		opacity: 0.6;
	}

	:global(.dark) .chef-icon {
		filter: invert(1);
	}

	.clear {
		padding: 0.375rem 0.75rem;
		background: none;
		border: none;
		font-size: 0.8125rem;
		color: var(--color-text-muted);
		cursor: pointer;
		text-decoration: underline;
	}

	.clear:hover {
		color: var(--color-text);
	}

	.results {
		margin-top: 1.5rem;
	}

	.count {
		font-size: 0.875rem;
		color: var(--color-text-muted);
		margin: 0;
	}

	.recipes {
		display: grid;
		grid-template-columns: repeat(2, 1fr);
		gap: 1.25rem;
		margin-top: 1rem;
	}

	@media (max-width: 768px) {
		.recipes {
			grid-template-columns: 1fr;
		}
	}

	.empty {
		margin-top: 2rem;
		text-align: center;
		color: var(--color-text-muted);
	}

	.pagination {
		display: flex;
		align-items: center;
		justify-content: center;
		gap: 1.5rem;
		margin-top: 2rem;
		padding-top: 2rem;
		border-top: 1px solid var(--color-border);
	}

	.page-btn {
		display: flex;
		align-items: center;
		gap: 0.375rem;
		padding: 0.5rem 1rem;
		background: var(--color-surface);
		border: 1px solid var(--color-border);
		border-radius: 6px;
		font-size: 0.875rem;
		color: var(--color-text);
		cursor: pointer;
		transition: all 0.15s;
	}

	.page-btn:hover:not(:disabled) {
		border-color: var(--color-text-muted);
	}

	.page-btn:disabled {
		opacity: 0.4;
		cursor: not-allowed;
	}

	.page-info {
		font-size: 0.875rem;
		color: var(--color-text-muted);
	}

	/* Mobile */
	@media (max-width: 640px) {
		.page {
			padding: 1.5rem 1rem 3rem;
		}

		h1 {
			font-size: 1.25rem;
		}

		.search-section {
			margin-top: 1rem;
		}

		.search-row {
			flex-wrap: wrap;
		}

		.tags {
			margin-top: 1rem;
		}

		.tag-list {
			gap: 0.375rem;
		}

		.tag {
			padding: 0.3rem 0.625rem;
			font-size: 0.75rem;
		}

		.controls {
			margin-top: 1.5rem;
			padding-top: 1.25rem;
		}

		.sort {
			flex-direction: column;
			align-items: flex-start;
			gap: 0.5rem;
		}

		.sort-options {
			width: 100%;
		}

		.sort-options button {
			flex: 1;
			text-align: center;
			padding: 0.5rem 0.5rem;
			font-size: 0.75rem;
		}

		.filters {
			gap: 0.75rem;
		}

		.pagination {
			gap: 0.75rem;
		}

		.page-btn {
			padding: 0.5rem 0.75rem;
			font-size: 0.8125rem;
		}

		.btn-text {
			display: none;
		}

		.page-info {
			font-size: 0.8125rem;
		}
	}
</style>
