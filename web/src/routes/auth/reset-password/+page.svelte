<script lang="ts">
	import { goto } from '$app/navigation';
	import { page } from '$app/stores';
	import { authClient } from '$lib/auth/client';

	let password = $state('');
	let confirmPassword = $state('');
	let isLoading = $state(false);
	let error = $state('');
	let success = $state(false);

	const token = $derived($page.url.searchParams.get('token'));

	async function handleSubmit(e: SubmitEvent) {
		e.preventDefault();
		if (!password || !confirmPassword || !token) return;

		if (password !== confirmPassword) {
			error = 'Passwords do not match.';
			return;
		}

		isLoading = true;
		error = '';

		try {
			await authClient.resetPassword({
				newPassword: password,
				token
			});
			success = true;
		} catch (err: any) {
			error = err?.message || 'Something went wrong. Please try again.';
		} finally {
			isLoading = false;
		}
	}

	const passwordValid = $derived(password.length >= 8);
	const passwordsMatch = $derived(password === confirmPassword);
</script>

<svelte:head>
	<title>Reset password - Just Cook</title>
</svelte:head>

<div class="page">
	<div class="card">
		<header>
			<a href="/" class="logo">
				<img src="/chef-hat.svg" alt="" class="logo-icon" />
				Just Cook
			</a>
		</header>

		{#if success}
			<div class="success">
				<h1>Password reset</h1>
				<p>Your password has been successfully reset.</p>
				<a href="/auth/login" class="primary-link">Sign in</a>
			</div>
		{:else if !token}
			<div class="error-state">
				<h1>Invalid link</h1>
				<p>This password reset link is invalid or has expired.</p>
				<a href="/auth/forgot-password" class="text-btn">Request a new link</a>
			</div>
		{:else}
			<h1>Reset password</h1>
			<p class="subtitle">Enter your new password below.</p>

			{#if error}
				<p class="error">{error}</p>
			{/if}

			<form onsubmit={handleSubmit}>
				<div class="field">
					<label for="password">New password</label>
					<input
						type="password"
						id="password"
						bind:value={password}
						placeholder="At least 8 characters"
						required
						minlength="8"
						disabled={isLoading}
					/>
					{#if password && !passwordValid}
						<p class="field-hint error-text">Password must be at least 8 characters</p>
					{/if}
				</div>

				<div class="field">
					<label for="confirm">Confirm password</label>
					<input
						type="password"
						id="confirm"
						bind:value={confirmPassword}
						placeholder="Confirm your password"
						required
						disabled={isLoading}
					/>
					{#if confirmPassword && !passwordsMatch}
						<p class="field-hint error-text">Passwords do not match</p>
					{/if}
				</div>

				<button type="submit" class="primary" disabled={isLoading || !passwordValid || !passwordsMatch}>
					{isLoading ? 'Resetting...' : 'Reset password'}
				</button>
			</form>
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

	h1 {
		font-size: 1.5rem;
		font-weight: 600;
		margin: 0;
		text-align: center;
	}

	.subtitle {
		text-align: center;
		color: var(--color-text-muted);
		margin: 0.5rem 0 1.5rem;
		font-size: 0.9375rem;
	}

	.error {
		background: #fef2f2;
		color: #b91c1c;
		padding: 0.75rem 1rem;
		border-radius: 4px;
		font-size: 0.875rem;
		margin-bottom: 1rem;
		text-align: center;
	}

	:global(.dark) .error {
		background: #450a0a;
		color: #fca5a5;
	}

	.field {
		margin-bottom: 1rem;
	}

	.field label {
		display: block;
		font-size: 0.875rem;
		font-weight: 500;
		margin-bottom: 0.375rem;
	}

	.field input {
		width: 100%;
		padding: 0.75rem;
		background: var(--color-surface);
		border: 1px solid var(--color-border);
		border-radius: 4px;
		font-family: inherit;
		font-size: 1rem;
		color: var(--color-text);
	}

	.field input:focus {
		outline: none;
		border-color: var(--color-text);
	}

	.field input::placeholder {
		color: var(--color-text-muted);
	}

	.field input:disabled {
		opacity: 0.6;
	}

	.field-hint {
		font-size: 0.8125rem;
		margin: 0.375rem 0 0;
	}

	.error-text {
		color: #b91c1c;
	}

	:global(.dark) .error-text {
		color: #fca5a5;
	}

	.primary {
		width: 100%;
		padding: 0.75rem 1.5rem;
		background: var(--color-text);
		border: none;
		border-radius: 4px;
		color: var(--color-bg);
		font-family: inherit;
		font-size: 1rem;
		font-weight: 500;
		cursor: pointer;
		transition: opacity 0.15s;
	}

	.primary:hover:not(:disabled) {
		opacity: 0.85;
	}

	.primary:disabled {
		opacity: 0.4;
		cursor: not-allowed;
	}

	.success,
	.error-state {
		text-align: center;
	}

	.success h1,
	.error-state h1 {
		margin-bottom: 0.5rem;
	}

	.success p,
	.error-state p {
		margin: 0;
		color: var(--color-text-muted);
	}

	.primary-link {
		display: inline-block;
		margin-top: 1.5rem;
		padding: 0.75rem 2rem;
		background: var(--color-text);
		color: var(--color-bg);
		text-decoration: none;
		border-radius: 4px;
		font-weight: 500;
	}

	.primary-link:hover {
		opacity: 0.85;
	}

	.text-btn {
		display: inline-block;
		color: var(--color-text-muted);
		font-size: 0.875rem;
		text-decoration: underline;
		margin-top: 1.5rem;
	}

	.text-btn:hover {
		color: var(--color-text);
	}
</style>
