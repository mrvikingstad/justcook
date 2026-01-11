<script lang="ts">
	import { authClient } from '$lib/auth/client';

	let email = $state('');
	let isLoading = $state(false);
	let emailSent = $state(false);
	let error = $state('');

	async function handleSubmit(e: SubmitEvent) {
		e.preventDefault();
		if (!email.trim()) return;

		isLoading = true;
		error = '';

		try {
			await authClient.forgetPassword({
				email: email.trim(),
				redirectTo: '/auth/reset-password'
			});
			emailSent = true;
		} catch (err) {
			error = 'Something went wrong. Please try again.';
		} finally {
			isLoading = false;
		}
	}
</script>

<svelte:head>
	<title>Forgot password - Just Cook</title>
</svelte:head>

<div class="page">
	<div class="card">
		<header>
			<a href="/" class="logo">
				<img src="/chef-hat.svg" alt="" class="logo-icon" />
				Just Cook
			</a>
		</header>

		{#if emailSent}
			<div class="success">
				<h1>Check your email</h1>
				<p>We sent a password reset link to <strong>{email}</strong></p>
				<p class="hint">The link expires in 1 hour.</p>
				<a href="/auth/login" class="text-btn">Back to sign in</a>
			</div>
		{:else}
			<h1>Forgot password</h1>
			<p class="subtitle">Enter your email and we'll send you a reset link.</p>

			{#if error}
				<p class="error">{error}</p>
			{/if}

			<form onsubmit={handleSubmit}>
				<div class="field">
					<label for="email">Email</label>
					<input
						type="email"
						id="email"
						bind:value={email}
						placeholder="you@example.com"
						required
						disabled={isLoading}
					/>
				</div>

				<button type="submit" class="primary" disabled={isLoading || !email.trim()}>
					{isLoading ? 'Sending...' : 'Send reset link'}
				</button>
			</form>

			<p class="footer-text">
				<a href="/auth/login">Back to sign in</a>
			</p>
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

	.footer-text {
		text-align: center;
		font-size: 0.875rem;
		color: var(--color-text-muted);
		margin-top: 1.5rem;
	}

	.footer-text a {
		color: var(--color-text);
		font-weight: 500;
	}

	.success {
		text-align: center;
	}

	.success h1 {
		margin-bottom: 0.5rem;
	}

	.success p {
		margin: 0;
		color: var(--color-text-muted);
	}

	.success p strong {
		color: var(--color-text);
	}

	.success .hint {
		font-size: 0.8125rem;
		margin-top: 0.5rem;
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
