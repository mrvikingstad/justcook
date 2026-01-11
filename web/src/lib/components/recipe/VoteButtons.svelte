<script lang="ts">
	import { ArrowUp, ArrowDown } from 'lucide-svelte';

	interface Props {
		recipeId: string;
		upvotes: number;
		downvotes: number;
		userVote?: 1 | -1 | null;
	}

	let { recipeId, upvotes, downvotes, userVote = null }: Props = $props();

	let currentUpvotes = $state(upvotes);
	let currentDownvotes = $state(downvotes);
	let currentUserVote = $state(userVote);
	let isLoading = $state(false);

	const totalVotes = $derived(currentUpvotes + currentDownvotes);
	const score = $derived(currentUpvotes - currentDownvotes);
	const ratio = $derived(totalVotes > 0 ? Math.round((currentUpvotes / totalVotes) * 100) : 0);

	async function handleVote(value: 1 | -1) {
		if (isLoading) return;

		isLoading = true;

		try {
			const response = await fetch('/api/votes', {
				method: 'POST',
				headers: { 'Content-Type': 'application/json' },
				body: JSON.stringify({ recipeId, value })
			});

			const data = await response.json();

			if (response.ok) {
				currentUpvotes = data.upvotes;
				currentDownvotes = data.downvotes;
				currentUserVote = data.userVote;
			} else if (response.status === 401) {
				// User not logged in - redirect to login
				window.location.href = '/auth/login';
			} else {
				console.error('Vote failed:', data.error);
			}
		} catch (error) {
			console.error('Vote request failed:', error);
		} finally {
			isLoading = false;
		}
	}
</script>

<div class="votes">
	<button
		class="up"
		class:active={currentUserVote === 1}
		disabled={isLoading}
		aria-label="Upvote"
		onclick={() => handleVote(1)}
	>
		<ArrowUp size={18} />
	</button>
	<span class="count">{currentUpvotes}</span>
	{#if totalVotes > 0}
		<span class="ratio">{ratio}%</span>
	{/if}
	<button
		class="down"
		class:active={currentUserVote === -1}
		disabled={isLoading}
		aria-label="Downvote"
		onclick={() => handleVote(-1)}
	>
		<ArrowDown size={18} />
	</button>
</div>

<style>
	.votes {
		display: flex;
		align-items: center;
		gap: 0.5rem;
		font-size: 0.9375rem;
	}

	button {
		display: flex;
		align-items: center;
		justify-content: center;
		padding: 0.375rem;
		background: none;
		border: none;
		color: var(--color-text-muted);
		cursor: pointer;
		border-radius: 4px;
		transition: color 0.15s, opacity 0.15s;
	}

	button:hover:not(:disabled),
	button.active {
		color: var(--color-text);
	}

	button:disabled {
		opacity: 0.5;
		cursor: not-allowed;
	}

	button.active.up {
		color: #22c55e;
	}

	button.active.down {
		color: #ef4444;
	}

	.count {
		font-weight: 500;
	}

	.ratio {
		color: var(--color-text-muted);
	}
</style>
