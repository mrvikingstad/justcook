<script lang="ts">
	import { MapPin, Calendar, BadgeCheck, BookOpen, ArrowUp, Users } from 'lucide-svelte';
	import { goto } from '$app/navigation';
	import { page } from '$app/stores';
	import RecipeCard from '$lib/components/recipe/RecipeCard.svelte';
	import { getCountry } from '$lib/data/countries';

	let { data } = $props();

	let isFollowing = $state(data.isFollowing);
	let followerCount = $state(data.chef.stats.followerCount);
	let isLoading = $state(false);
	let sortBy = $state(data.sortBy);

	async function toggleFollow() {
		if (isLoading) return;

		isLoading = true;

		try {
			const method = isFollowing ? 'DELETE' : 'POST';
			const response = await fetch('/api/follow', {
				method,
				headers: { 'Content-Type': 'application/json' },
				body: JSON.stringify({ userId: data.chef.id })
			});

			if (response.ok) {
				isFollowing = !isFollowing;
				followerCount += isFollowing ? 1 : -1;
			}
		} finally {
			isLoading = false;
		}
	}

	function updateSort(newSort: string) {
		sortBy = newSort;
		const params = new URLSearchParams($page.url.searchParams);
		if (newSort === 'latest') {
			params.delete('sort');
		} else {
			params.set('sort', newSort);
		}
		const queryString = params.toString();
		goto(`/chef/${data.chef.username}${queryString ? `?${queryString}` : ''}`, { replaceState: true });
	}

	function formatDate(date: Date | string): string {
		const d = typeof date === 'string' ? new Date(date) : date;
		return d.toLocaleDateString('en-US', { month: 'short', year: 'numeric' });
	}
</script>

<svelte:head>
	<title>{data.chef.fullName} - Chef - Just Cook</title>
</svelte:head>

<div class="page container">
	<div class="profile-header">
		<div class="photo">
			{#if data.chef.photo}
				<img src={data.chef.photo} alt={data.chef.fullName} />
			{:else}
				<span class="photo-fallback">{data.chef.fullName[0]}</span>
			{/if}
		</div>

		<div class="profile-main">
			<div class="profile-top">
				<div class="info">
					<div class="name-row">
						<h1>{data.chef.fullName}</h1>
						{#if data.chef.profileTier === 'author'}
							<span class="badge" title="Verified Author">
								<BadgeCheck size={20} />
							</span>
						{:else if data.chef.profileTier === 'chef'}
							<span class="badge chef-badge" title="Chef">
								<img src="/chef.svg" alt="Chef" class="chef-badge-icon" />
							</span>
						{/if}
					</div>
					<p class="username">@{data.chef.username}</p>
				</div>

				{#if !data.isOwnProfile}
					<button class="follow-btn" class:following={isFollowing} onclick={toggleFollow} disabled={isLoading}>
						{#if isFollowing}
							Following
						{:else}
							Follow
						{/if}
					</button>
				{/if}
			</div>

			<div class="meta">
				{#if data.chef.country}
					<span class="meta-item">
						<MapPin size={14} />
						{getCountry(data.chef.country)?.name ?? data.chef.country}
					</span>
				{/if}
				<span class="meta-item">
					<Calendar size={14} />
					Joined {formatDate(data.chef.chefSince)}
				</span>
			</div>
		</div>
	</div>

	{#if data.chef.bio}
		<p class="bio">{data.chef.bio}</p>
	{/if}

	<div class="stats">
		<div class="stat">
			<BookOpen size={18} strokeWidth={1.5} />
			<span class="stat-value">{data.chef.stats.recipeCount}</span>
			<span class="stat-label">recipes</span>
		</div>
		<div class="stat">
			<ArrowUp size={18} strokeWidth={1.5} />
			<span class="stat-value">{data.chef.stats.totalUpvotes}</span>
			<span class="stat-label">upvotes</span>
		</div>
		<div class="stat">
			<Users size={18} strokeWidth={1.5} />
			<span class="stat-value">{followerCount}</span>
			<span class="stat-label">followers</span>
		</div>
	</div>

	<section class="recipes-section">
		<div class="recipes-header">
			<h2>Recipes</h2>
			{#if data.recipes.length > 0}
				<div class="sort-options">
					<button class:active={sortBy === 'latest'} onclick={() => updateSort('latest')}>
						Latest
					</button>
					<button class:active={sortBy === 'earliest'} onclick={() => updateSort('earliest')}>
						Earliest
					</button>
					<button class:active={sortBy === 'upvotes'} onclick={() => updateSort('upvotes')}>
						Top
					</button>
				</div>
			{/if}
		</div>
		{#if data.recipes.length > 0}
			<div class="recipes-grid">
				{#each data.recipes as recipe}
					<RecipeCard {...recipe} variant="vertical" />
				{/each}
			</div>
		{:else}
			<p class="empty">No published recipes yet.</p>
		{/if}
	</section>
</div>

<style>
	.page {
		padding: 2rem 1.5rem 4rem;
		max-width: 900px;
	}

	.profile-header {
		display: flex;
		gap: 1.25rem;
		align-items: flex-start;
	}

	.photo {
		flex-shrink: 0;
		width: 72px;
		height: 72px;
		border-radius: 50%;
		overflow: hidden;
		background: var(--color-border);
		display: flex;
		align-items: center;
		justify-content: center;
	}

	.photo img {
		width: 100%;
		height: 100%;
		object-fit: cover;
	}

	.photo-fallback {
		font-size: 1.75rem;
		font-weight: 600;
		text-transform: uppercase;
		color: var(--color-text-muted);
	}

	.profile-main {
		flex: 1;
		min-width: 0;
	}

	.profile-top {
		display: flex;
		align-items: flex-start;
		justify-content: space-between;
		gap: 1rem;
	}

	.info {
		min-width: 0;
	}

	.name-row {
		display: flex;
		align-items: center;
		gap: 0.375rem;
	}

	h1 {
		font-size: 1.25rem;
		font-weight: 600;
		margin: 0;
	}

	.badge {
		color: var(--color-text);
		display: flex;
	}

	.chef-badge-icon {
		width: 20px;
		height: 20px;
	}

	:global(.dark) .chef-badge-icon {
		filter: invert(1);
	}

	.username {
		color: var(--color-text-muted);
		font-size: 0.9375rem;
		margin: 0.125rem 0 0;
	}

	.meta {
		display: flex;
		align-items: center;
		gap: 1rem;
		margin-top: 0.5rem;
	}

	.meta-item {
		display: flex;
		align-items: center;
		gap: 0.25rem;
		color: var(--color-text-muted);
		font-size: 0.8125rem;
	}

	.follow-btn {
		flex-shrink: 0;
		padding: 0.5rem 1rem;
		background: var(--color-text);
		border: 1px solid var(--color-text);
		border-radius: 4px;
		color: var(--color-bg);
		font-family: inherit;
		font-size: 0.8125rem;
		font-weight: 500;
		cursor: pointer;
		transition: all 0.15s;
	}

	.follow-btn:hover:not(:disabled) {
		opacity: 0.85;
	}

	.follow-btn.following {
		background: transparent;
		color: var(--color-text);
	}

	.follow-btn.following:hover:not(:disabled) {
		border-color: #b91c1c;
		color: #b91c1c;
	}

	.follow-btn:disabled {
		opacity: 0.6;
		cursor: not-allowed;
	}

	.bio {
		margin: 1.5rem 0 0;
		line-height: 1.6;
	}

	.stats {
		display: flex;
		gap: 1.5rem;
		margin-top: 1.5rem;
		padding-top: 1.5rem;
		border-top: 1px solid var(--color-border);
	}

	.stat {
		display: flex;
		align-items: center;
		gap: 0.375rem;
		color: var(--color-text-muted);
	}

	.stat-value {
		font-weight: 600;
		color: var(--color-text);
	}

	.stat-label {
		font-size: 0.875rem;
	}

	.recipes-section {
		margin-top: 2.5rem;
		padding-top: 2rem;
		border-top: 1px solid var(--color-border);
	}

	.recipes-header {
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

	.recipes-grid {
		display: grid;
		grid-template-columns: repeat(3, 1fr);
		gap: 1.25rem;
	}

	.empty {
		color: var(--color-text-muted);
		text-align: center;
		padding: 2rem 0;
	}

	/* Tablet */
	@media (max-width: 900px) {
		.recipes-grid {
			grid-template-columns: repeat(2, 1fr);
		}
	}

	/* Mobile */
	@media (max-width: 640px) {
		.page {
			padding: 1.5rem 1rem 3rem;
		}

		.profile-header {
			gap: 1rem;
		}

		.photo {
			width: 56px;
			height: 56px;
		}

		.photo-fallback {
			font-size: 1.5rem;
		}

		h1 {
			font-size: 1.125rem;
		}

		.username {
			font-size: 0.875rem;
		}

		.meta {
			flex-wrap: wrap;
			gap: 0.5rem 0.75rem;
			margin-top: 0.375rem;
		}

		.meta-item {
			font-size: 0.75rem;
		}

		.follow-btn {
			padding: 0.4rem 0.875rem;
			font-size: 0.75rem;
		}

		.bio {
			font-size: 0.9375rem;
			margin-top: 1.25rem;
		}

		.stats {
			gap: 1rem;
			margin-top: 1.25rem;
			padding-top: 1.25rem;
		}

		.stat {
			gap: 0.25rem;
			font-size: 0.8125rem;
		}

		.stat-label {
			font-size: 0.8125rem;
		}

		.recipes-section {
			margin-top: 2rem;
			padding-top: 1.5rem;
		}

		.recipes-header {
			flex-direction: column;
			align-items: flex-start;
			gap: 0.75rem;
		}

		h2 {
			font-size: 1.125rem;
		}

		.sort-options button {
			padding: 0.375rem 0.625rem;
			font-size: 0.75rem;
		}

		.recipes-grid {
			grid-template-columns: 1fr;
			gap: 1rem;
		}
	}
</style>
