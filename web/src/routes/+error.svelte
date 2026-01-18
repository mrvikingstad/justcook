<script lang="ts">
	import { page } from '$app/stores';
</script>

<svelte:head>
	<title>Error {$page.status} - Just Cook</title>
</svelte:head>

<div class="error-page">
	<div class="error-content">
		<h1 class="error-code">{$page.status}</h1>

		{#if $page.status === 404}
			<h2>Page not found</h2>
			<p>The page you're looking for doesn't exist or has been moved.</p>
		{:else if $page.status === 403}
			<h2>Access denied</h2>
			<p>You don't have permission to access this page.</p>
		{:else if $page.status === 401}
			<h2>Not authenticated</h2>
			<p>Please log in to access this page.</p>
		{:else if $page.status >= 500}
			<h2>Something went wrong</h2>
			<p>We're having some technical difficulties. Please try again later.</p>
		{:else}
			<h2>Error</h2>
			<p>{$page.error?.message || 'An unexpected error occurred.'}</p>
		{/if}

		<div class="error-actions">
			<a href="/" class="btn-primary">Go home</a>
			<button onclick={() => history.back()} class="btn-secondary">Go back</button>
		</div>
	</div>
</div>

<style>
	.error-page {
		display: flex;
		align-items: center;
		justify-content: center;
		min-height: 60vh;
		padding: 2rem;
	}

	.error-content {
		text-align: center;
		max-width: 500px;
	}

	.error-code {
		font-size: 6rem;
		font-weight: 700;
		color: var(--color-primary, #e74c3c);
		margin: 0;
		line-height: 1;
	}

	h2 {
		font-size: 1.5rem;
		margin: 1rem 0 0.5rem;
		color: var(--color-text, #333);
	}

	p {
		color: var(--color-text-secondary, #666);
		margin: 0 0 2rem;
	}

	.error-actions {
		display: flex;
		gap: 1rem;
		justify-content: center;
		flex-wrap: wrap;
	}

	.btn-primary,
	.btn-secondary {
		padding: 0.75rem 1.5rem;
		border-radius: 8px;
		font-size: 1rem;
		font-weight: 500;
		cursor: pointer;
		text-decoration: none;
		transition: all 0.2s ease;
	}

	.btn-primary {
		background-color: var(--color-primary, #e74c3c);
		color: white;
		border: none;
	}

	.btn-primary:hover {
		background-color: var(--color-primary-dark, #c0392b);
	}

	.btn-secondary {
		background-color: transparent;
		color: var(--color-text, #333);
		border: 1px solid var(--color-border, #ddd);
	}

	.btn-secondary:hover {
		background-color: var(--color-bg-hover, #f5f5f5);
	}
</style>
