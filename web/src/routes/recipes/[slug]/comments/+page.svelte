<script lang="ts">
	import { ArrowLeft } from 'lucide-svelte';
	import CommentItem from '$lib/components/recipe/CommentItem.svelte';
	import CommentForm from '$lib/components/recipe/CommentForm.svelte';

	let { data } = $props();

	let comments = $state(data.comments);
	let isSubmitting = $state(false);
	let error = $state<string | null>(null);

	async function handleSubmit(content: string) {
		if (isSubmitting) return;

		isSubmitting = true;
		error = null;

		try {
			const response = await fetch('/api/comments', {
				method: 'POST',
				headers: { 'Content-Type': 'application/json' },
				body: JSON.stringify({
					recipeId: data.recipe.id,
					content
				})
			});

			const result = await response.json();

			if (response.ok) {
				// Add new comment to the top of the list
				comments = [result.comment, ...comments];
			} else {
				error = result.error || 'Failed to post comment';
			}
		} catch (e) {
			error = 'Failed to post comment';
		} finally {
			isSubmitting = false;
		}
	}

	async function handleDelete(commentId: string) {
		if (!confirm('Are you sure you want to delete this comment?')) return;

		try {
			const response = await fetch('/api/comments', {
				method: 'DELETE',
				headers: { 'Content-Type': 'application/json' },
				body: JSON.stringify({ commentId })
			});

			if (response.ok) {
				comments = comments.filter((c) => c.id !== commentId);
			}
		} catch (e) {
			console.error('Failed to delete comment:', e);
		}
	}
</script>

<svelte:head>
	<title>Comments - {data.recipe.title} - Just Cook</title>
</svelte:head>

<div class="page container">
	<a href="/recipes/{data.recipe.slug}" class="back">
		<ArrowLeft size={18} />
		{data.recipe.title}
	</a>

	<h1>Comments</h1>
	<p class="count">{comments.length} {comments.length === 1 ? 'comment' : 'comments'}</p>

	{#if data.isLoggedIn}
		<div class="form-wrapper">
			{#if error}
				<p class="error">{error}</p>
			{/if}
			<CommentForm onSubmit={handleSubmit} />
		</div>
	{:else}
		<p class="login-prompt">
			<a href="/auth/login">Sign in</a> to leave a comment.
		</p>
	{/if}

	<div class="comments">
		{#each comments as comment (comment.id)}
			<CommentItem
				authorName={comment.authorName}
				authorAvatar={comment.authorAvatar}
				content={comment.content}
				createdAt={new Date(comment.createdAt)}
				upvotes={0}
				downvotes={0}
				isOwn={comment.isOwn}
				isRecipeAuthor={comment.isRecipeAuthor}
				onDelete={() => handleDelete(comment.id)}
			/>
		{/each}
	</div>

	{#if comments.length === 0}
		<p class="empty">No comments yet. Be the first to share your thoughts!</p>
	{/if}
</div>

<style>
	.page {
		padding: 2rem 1.5rem 4rem;
		max-width: 600px;
	}

	.back {
		display: inline-flex;
		align-items: center;
		gap: 0.5rem;
		color: var(--color-text-muted);
		text-decoration: none;
		font-size: 0.875rem;
		margin-bottom: 1.5rem;
	}

	.back:hover {
		color: var(--color-text);
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

	.form-wrapper {
		margin-top: 2rem;
		padding-bottom: 1.5rem;
		border-bottom: 1px solid var(--color-border);
	}

	.error {
		color: #b91c1c;
		font-size: 0.875rem;
		margin: 0 0 1rem;
		padding: 0.75rem;
		background: #fef2f2;
		border-radius: 4px;
	}

	.login-prompt {
		margin-top: 2rem;
		padding: 1rem;
		background: var(--color-surface);
		border: 1px solid var(--color-border);
		border-radius: 4px;
		text-align: center;
		color: var(--color-text-muted);
	}

	.login-prompt a {
		color: var(--color-text);
		font-weight: 500;
	}

	.comments {
		margin-top: 1rem;
	}

	.empty {
		margin-top: 2rem;
		text-align: center;
		color: var(--color-text-muted);
	}
</style>
