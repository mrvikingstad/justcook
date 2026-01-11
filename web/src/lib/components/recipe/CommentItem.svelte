<script lang="ts">
	import { Pencil, Trash2, ArrowUp, ArrowDown } from 'lucide-svelte';
	import type { Snippet } from 'svelte';

	interface Props {
		authorName: string;
		authorAvatar?: string | null;
		content: string;
		createdAt: Date;
		upvotes: number;
		downvotes: number;
		userVote?: 1 | -1 | null;
		isOwn?: boolean;
		isRecipeAuthor?: boolean;
		isReply?: boolean;
		onEdit?: () => void;
		onDelete?: () => void;
		replies?: Snippet;
	}

	let {
		authorName,
		authorAvatar,
		content,
		createdAt,
		upvotes,
		downvotes,
		userVote = null,
		isOwn = false,
		isRecipeAuthor = false,
		isReply = false,
		onEdit,
		onDelete,
		replies
	}: Props = $props();

	const totalVotes = $derived(upvotes + downvotes);
	const ratio = $derived(totalVotes > 0 ? Math.round((upvotes / totalVotes) * 100) : 0);

	function formatDate(date: Date): string {
		const now = new Date();
		const diff = now.getTime() - date.getTime();
		const days = Math.floor(diff / (1000 * 60 * 60 * 24));

		if (days === 0) {
			const hours = Math.floor(diff / (1000 * 60 * 60));
			if (hours === 0) {
				const minutes = Math.floor(diff / (1000 * 60));
				return minutes <= 1 ? 'just now' : `${minutes}m ago`;
			}
			return `${hours}h ago`;
		}
		if (days === 1) return 'yesterday';
		if (days < 7) return `${days}d ago`;

		return date.toLocaleDateString('en-US', { month: 'short', day: 'numeric' });
	}
</script>

<article class="comment" class:reply={isReply}>
	<div class="avatar" class:small={isReply}>
		{#if authorAvatar}
			<img src={authorAvatar} alt={authorName} />
		{:else}
			<span class="avatar-fallback">{authorName[0]}</span>
		{/if}
	</div>

	<div class="body">
		<div class="header">
			<span class="author">{authorName}</span>
			{#if isRecipeAuthor}
				<span class="badge">Author</span>
			{/if}
			<span class="date">{formatDate(createdAt)}</span>
		</div>
		<p class="content">{content}</p>

		<div class="footer">
			<div class="votes">
				<button class:active={userVote === 1} aria-label="Upvote">
					<ArrowUp size={14} />
				</button>
				<span class="count">{upvotes}</span>
				{#if totalVotes > 0}
					<span class="ratio">{ratio}%</span>
				{/if}
				<button class:active={userVote === -1} aria-label="Downvote">
					<ArrowDown size={14} />
				</button>
			</div>

			{#if isOwn}
				<div class="actions">
					<button onclick={onEdit}>
						<Pencil size={14} />
						Edit
					</button>
					<button onclick={onDelete} class="delete">
						<Trash2 size={14} />
						Delete
					</button>
				</div>
			{/if}
		</div>

		{#if replies}
			<div class="replies">
				{@render replies()}
			</div>
		{/if}
	</div>
</article>

<style>
	.comment {
		display: flex;
		gap: 1rem;
		padding: 1.25rem 0;
		border-bottom: 1px solid var(--color-border);
	}

	.comment.reply {
		padding: 1rem 0 0;
		border-bottom: none;
	}

	.comment.reply:last-child {
		padding-bottom: 0;
	}

	.avatar {
		flex-shrink: 0;
		width: 36px;
		height: 36px;
		border-radius: 50%;
		overflow: hidden;
		background: var(--color-border);
		display: flex;
		align-items: center;
		justify-content: center;
	}

	.avatar.small {
		width: 28px;
		height: 28px;
	}

	.avatar.small .avatar-fallback {
		font-size: 0.75rem;
	}

	.avatar img {
		width: 100%;
		height: 100%;
		object-fit: cover;
	}

	.avatar-fallback {
		font-weight: 600;
		font-size: 0.875rem;
		text-transform: uppercase;
		color: var(--color-text-muted);
	}

	.body {
		flex: 1;
		min-width: 0;
	}

	.header {
		display: flex;
		align-items: baseline;
		gap: 0.5rem;
	}

	.author {
		font-weight: 500;
	}

	.badge {
		font-size: 0.6875rem;
		font-weight: 500;
		text-transform: uppercase;
		letter-spacing: 0.025em;
		padding: 0.125rem 0.375rem;
		background: var(--color-text);
		color: var(--color-bg);
		border-radius: 2px;
	}

	.date {
		font-size: 0.8125rem;
		color: var(--color-text-muted);
	}

	.content {
		margin: 0.375rem 0 0;
		line-height: 1.5;
	}

	.footer {
		display: flex;
		align-items: center;
		gap: 1.5rem;
		margin-top: 0.625rem;
	}

	.votes {
		display: flex;
		align-items: center;
		gap: 0.375rem;
		font-size: 0.8125rem;
	}

	.votes button {
		display: flex;
		align-items: center;
		justify-content: center;
		padding: 0.25rem;
		background: none;
		border: none;
		color: var(--color-text-muted);
		cursor: pointer;
		border-radius: 2px;
	}

	.votes button:hover,
	.votes button.active {
		color: var(--color-text);
	}

	.votes .count {
		font-weight: 500;
		color: var(--color-text);
	}

	.votes .ratio {
		color: var(--color-text-muted);
	}

	.actions {
		display: flex;
		gap: 1rem;
	}

	.actions button {
		display: flex;
		align-items: center;
		gap: 0.25rem;
		padding: 0;
		background: none;
		border: none;
		font-size: 0.8125rem;
		color: var(--color-text-muted);
		cursor: pointer;
	}

	.actions button:hover {
		color: var(--color-text);
	}

	.actions button.delete:hover {
		color: #b91c1c;
	}

	.replies {
		margin-top: 0.5rem;
		padding-left: 0.5rem;
		border-left: 2px solid var(--color-border);
	}
</style>
