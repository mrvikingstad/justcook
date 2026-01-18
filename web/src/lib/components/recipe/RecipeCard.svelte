<script lang="ts">
	import { Clock, ArrowUp } from 'lucide-svelte';
	import DifficultyIndicator from '$lib/components/ui/DifficultyIndicator.svelte';

	interface Props {
		slug: string;
		title: string;
		description?: string | null;
		image?: string | null;
		authorName: string;
		authorUsername?: string | null;
		cuisine?: string | null;
		tag?: string | null;
		difficulty?: 'easy' | 'medium' | 'hard' | null;
		prepTimeMinutes?: number | null;
		cookTimeMinutes?: number | null;
		upvotes: number;
		downvotes: number;
		publishedAt?: Date | string | null;
		variant?: 'horizontal' | 'vertical';
	}

	let {
		slug,
		title,
		description,
		image,
		authorName,
		authorUsername,
		cuisine,
		tag,
		difficulty,
		prepTimeMinutes,
		cookTimeMinutes,
		upvotes,
		downvotes,
		publishedAt,
		variant = 'horizontal'
	}: Props = $props();

	const totalVotes = $derived(upvotes + downvotes);
	const score = $derived(upvotes - downvotes);
	const ratio = $derived(totalVotes > 0 ? Math.round((upvotes / totalVotes) * 100) : 0);
	const totalTime = $derived((prepTimeMinutes || 0) + (cookTimeMinutes || 0));

	function formatTime(minutes: number): string {
		if (minutes < 60) return `${minutes}m`;
		const h = Math.floor(minutes / 60);
		const m = minutes % 60;
		return m > 0 ? `${h}h ${m}m` : `${h}h`;
	}

	function formatDate(date: Date | string): string {
		const d = typeof date === 'string' ? new Date(date) : date;
		const now = new Date();
		const diffMs = now.getTime() - d.getTime();
		const diffDays = Math.floor(diffMs / (1000 * 60 * 60 * 24));

		if (diffDays < 1) return 'Today';
		if (diffDays === 1) return 'Yesterday';
		if (diffDays < 7) return `${diffDays}d ago`;
		if (diffDays < 30) return `${Math.floor(diffDays / 7)}w ago`;
		if (diffDays < 365) return d.toLocaleDateString('en-US', { month: 'short', day: 'numeric' });
		return d.toLocaleDateString('en-US', { month: 'short', year: 'numeric' });
	}
</script>

<article class="card" class:vertical={variant === 'vertical'}>
	{#if image}
		<a href="/recipes/{slug}" class="image-link" tabindex="-1">
			<img src={image} alt={title} class="image" loading="lazy" decoding="async" />
		</a>
	{/if}

	<div class="content">
		<div class="content-header">
			<h3 class="title">
				<a href="/recipes/{slug}">{title}</a>
			</h3>
			{#if publishedAt}
				<span class="date">{formatDate(publishedAt)}</span>
			{/if}
		</div>

		<div class="meta">
			{#if totalTime > 0}
				<span class="meta-item">
					<Clock size={12} />
					{formatTime(totalTime)}
				</span>
			{/if}
			{#if difficulty}
				<DifficultyIndicator level={difficulty} />
			{/if}
			{#if cuisine}
				<span class="meta-item">{cuisine}</span>
			{/if}
			{#if tag}
				<span class="meta-item tag">{tag}</span>
			{/if}
		</div>

		{#if description}
			<p class="description">{description}</p>
		{/if}

		<div class="footer">
			{#if authorUsername}
				<a href="/chef/{authorUsername}" class="author">{authorName}</a>
			{:else}
				<span class="author">{authorName}</span>
			{/if}
			<span class="votes">
				<ArrowUp size={12} />
				{upvotes}
				{#if totalVotes > 0}
					<span class="ratio">{ratio}%</span>
				{/if}
			</span>
		</div>
	</div>
</article>

<style>
	.card {
		display: flex;
		flex-direction: row;
		background: var(--color-surface);
		border: 1px solid var(--color-border);
		border-radius: 8px;
		overflow: hidden;
		box-shadow: var(--shadow-elevation-low);
		transition: box-shadow 0.2s, transform 0.2s;
	}

	.card:hover {
		box-shadow: var(--shadow-elevation-medium);
		transform: translateY(-2px);
	}

	.image-link {
		display: block;
		width: 160px;
		aspect-ratio: 1;
		flex-shrink: 0;
		overflow: hidden;
	}

	.image {
		width: 100%;
		height: 100%;
		object-fit: cover;
	}

	.content {
		flex: 1;
		padding: 1rem 1.25rem;
		display: flex;
		flex-direction: column;
		min-width: 0;
	}

	.content-header {
		display: flex;
		justify-content: space-between;
		align-items: flex-start;
		gap: 0.5rem;
	}

	.date {
		flex-shrink: 0;
		font-size: 0.6875rem;
		color: var(--color-text-muted);
		opacity: 0.7;
	}

	.title {
		font-size: 1.0625rem;
		font-weight: 600;
		margin: 0;
		line-height: 1.3;
	}

	.title a {
		text-decoration: none;
	}

	.title a:hover {
		text-decoration: underline;
	}

	.meta {
		display: flex;
		align-items: center;
		gap: 1rem;
		margin-top: 0.5rem;
		font-size: 0.75rem;
		color: var(--color-text-muted);
	}

	.meta-item {
		display: flex;
		align-items: center;
		gap: 0.3rem;
		text-transform: capitalize;
	}

	.description {
		margin: 0.75rem 0 0;
		color: var(--color-text-muted);
		font-size: 0.8125rem;
		line-height: 1.5;
		display: -webkit-box;
		-webkit-line-clamp: 2;
		-webkit-box-orient: vertical;
		overflow: hidden;
	}

	.footer {
		display: flex;
		align-items: center;
		justify-content: space-between;
		gap: 0.75rem;
		margin-top: auto;
		padding-top: 0.625rem;
		font-size: 0.75rem;
		color: var(--color-text-muted);
	}

	.votes {
		display: flex;
		align-items: center;
		gap: 0.25rem;
	}

	.ratio {
		margin-left: 0.125rem;
		opacity: 0.7;
	}

	a.author {
		text-decoration: none;
		font-weight: 500;
	}

	a.author:hover {
		text-decoration: underline;
		color: var(--color-text);
	}

	/* Vertical variant for grid layouts */
	.card.vertical {
		flex-direction: column;
	}

	.card.vertical .image-link {
		width: 100%;
		aspect-ratio: 16 / 10;
	}

	.card.vertical .content {
		padding: 1rem;
	}

	.card.vertical .meta {
		flex-wrap: wrap;
		gap: 0.5rem 1rem;
	}

	/* Mobile - stack horizontally-oriented cards */
	@media (max-width: 480px) {
		.card:not(.vertical) {
			flex-direction: column;
		}

		.card:not(.vertical) .image-link {
			width: 100%;
			aspect-ratio: 16 / 9;
		}

		.card:not(.vertical) .content {
			padding: 1rem;
		}

		.card:not(.vertical) .meta {
			flex-wrap: wrap;
			gap: 0.5rem 0.75rem;
		}
	}
</style>
