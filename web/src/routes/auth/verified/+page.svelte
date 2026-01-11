<script lang="ts">
	import { goto } from '$app/navigation';
	import { page } from '$app/stores';
	import { onMount } from 'svelte';

	// Check for error in URL params
	const errorParam = $derived($page.url.searchParams.get('error'));
	const hasError = $derived(!!errorParam);

	// Auto-redirect to recipes after 3 seconds if no error
	onMount(() => {
		if (!hasError) {
			setTimeout(() => {
				goto('/recipes');
			}, 3000);
		}
	});
</script>

<svelte:head>
	<title>{hasError ? 'Verification Failed' : 'Email Verified'} - Just Cook</title>
</svelte:head>

<div class="page">
	<div class="card">
		<header>
			<a href="/" class="logo">
				<img src="/chef-hat.svg" alt="" class="logo-icon" />
				Just Cook
			</a>
		</header>

		{#if hasError}
			<div class="content error-content">
				<div class="icon error-icon">
					<svg viewBox="0 0 24 24" width="48" height="48" fill="none" stroke="currentColor" stroke-width="2">
						<circle cx="12" cy="12" r="10"/>
						<line x1="15" y1="9" x2="9" y2="15"/>
						<line x1="9" y1="9" x2="15" y2="15"/>
					</svg>
				</div>
				<h1>Verification Failed</h1>
				<p>The verification link is invalid or has expired.</p>
				<a href="/auth/register" class="btn primary">Try again</a>
			</div>
		{:else}
			<div class="content success-content">
				<div class="icon success-icon">
					<svg viewBox="0 0 24 24" width="48" height="48" fill="none" stroke="currentColor" stroke-width="2">
						<circle cx="12" cy="12" r="10"/>
						<polyline points="9 12 12 15 16 10"/>
					</svg>
				</div>
				<h1>Email Verified</h1>
				<p>Your email has been verified successfully.</p>
				<p class="hint">Redirecting you to recipes...</p>
				<a href="/recipes" class="btn primary">Go to recipes</a>
			</div>
		{/if}
	</div>
</div>

<style>
	.page {
		min-height: 100vh;
		display: flex;
		align-items: center;
		justify-content: center;
		padding: 2rem 1.5rem;
	}

	.card {
		width: 100%;
		max-width: 360px;
	}

	header {
		text-align: center;
		margin-bottom: 2rem;
	}

	.logo {
		display: inline-flex;
		align-items: center;
		gap: 0.5rem;
		font-size: 1.25rem;
		font-weight: 600;
		text-decoration: none;
		color: var(--color-text);
	}

	.logo-icon {
		width: 28px;
		height: 28px;
	}

	:global(.dark) .logo-icon {
		filter: invert(1);
	}

	.content {
		text-align: center;
	}

	.icon {
		margin-bottom: 1.5rem;
	}

	.success-icon {
		color: #16a34a;
	}

	.error-icon {
		color: #dc2626;
	}

	:global(.dark) .success-icon {
		color: #4ade80;
	}

	:global(.dark) .error-icon {
		color: #f87171;
	}

	h1 {
		font-size: 1.5rem;
		font-weight: 600;
		margin: 0 0 0.5rem;
	}

	p {
		color: var(--color-text-muted);
		margin: 0;
	}

	.hint {
		font-size: 0.875rem;
		margin-top: 0.5rem;
	}

	.btn {
		display: inline-block;
		margin-top: 1.5rem;
		padding: 0.75rem 1.5rem;
		border-radius: 4px;
		font-weight: 500;
		text-decoration: none;
		transition: opacity 0.15s;
	}

	.primary {
		background: var(--color-text);
		color: var(--color-bg);
	}

	.primary:hover {
		opacity: 0.85;
	}
</style>
