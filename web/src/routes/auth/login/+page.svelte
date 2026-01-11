<script lang="ts">
	import { page } from '$app/stores';
	import { signIn } from '$lib/auth/client';

	let email = $state('');
	let isLoading = $state(false);
	let emailSent = $state(false);
	let error = $state('');

	// Get redirect URL from query params, default to /recipes
	const redirectUrl = $derived($page.url.searchParams.get('redirect') || '/recipes');

	async function handleMagicLink(e: SubmitEvent) {
		e.preventDefault();
		if (!email.trim()) return;

		isLoading = true;
		error = '';

		try {
			const result = await signIn.magicLink({
				email: email.trim(),
				callbackURL: redirectUrl
			});
			if (result.error) {
				error = result.error.message || 'Something went wrong. Please try again.';
			} else {
				emailSent = true;
			}
		} catch (err) {
			error = 'Something went wrong. Please try again.';
		} finally {
			isLoading = false;
		}
	}

	async function handleOAuth(provider: 'google' | 'apple') {
		isLoading = true;
		error = '';

		try {
			await signIn.social({
				provider,
				callbackURL: redirectUrl
			});
		} catch (err) {
			error = 'Something went wrong. Please try again.';
			isLoading = false;
		}
	}
</script>

<svelte:head>
	<title>Sign in - Just Cook</title>
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
				<p>We sent a sign-in link to <strong>{email}</strong></p>
				<p class="hint">The link expires in 10 minutes.</p>
				<button class="text-btn" onclick={() => { emailSent = false; email = ''; }}>
					Use a different email
				</button>
			</div>
		{:else}
			<h1>Sign in</h1>

			{#if error}
				<p class="error">{error}</p>
			{/if}

			<form onsubmit={handleMagicLink}>
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
					{isLoading ? 'Sending...' : 'Continue with email'}
				</button>
			</form>

			<div class="divider">
				<span>or</span>
			</div>

			<div class="oauth">
				<button class="oauth-btn" onclick={() => handleOAuth('google')} disabled={isLoading}>
					<svg viewBox="0 0 24 24" width="18" height="18">
						<path fill="#4285F4" d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92c-.26 1.37-1.04 2.53-2.21 3.31v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.09z"/>
						<path fill="#34A853" d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z"/>
						<path fill="#FBBC05" d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.07H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.93l2.85-2.22.81-.62z"/>
						<path fill="#EA4335" d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.07l3.66 2.84c.87-2.6 3.3-4.53 6.16-4.53z"/>
					</svg>
					Continue with Google
				</button>

				<button class="oauth-btn" onclick={() => handleOAuth('apple')} disabled={isLoading}>
					<svg viewBox="0 0 24 24" width="18" height="18" fill="currentColor">
						<path d="M17.05 20.28c-.98.95-2.05.8-3.08.35-1.09-.46-2.09-.48-3.24 0-1.44.62-2.2.44-3.06-.35C2.79 15.25 3.51 7.59 9.05 7.31c1.35.07 2.29.74 3.08.8 1.18-.24 2.31-.93 3.57-.84 1.51.12 2.65.72 3.4 1.8-3.12 1.87-2.38 5.98.48 7.13-.57 1.5-1.31 2.99-2.54 4.09l.01-.01zM12.03 7.25c-.15-2.23 1.66-4.07 3.74-4.25.29 2.58-2.34 4.5-3.74 4.25z"/>
					</svg>
					Continue with Apple
				</button>
			</div>

			<div class="footer-links">
				<a href="/auth/forgot-password">Forgot password?</a>
				<p>Don't have an account? <a href="/auth/register">Sign up</a></p>
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

	h1 {
		font-size: 1.5rem;
		font-weight: 600;
		margin: 0 0 1.5rem;
		text-align: center;
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

	.divider {
		display: flex;
		align-items: center;
		gap: 1rem;
		margin: 1.5rem 0;
	}

	.divider::before,
	.divider::after {
		content: '';
		flex: 1;
		height: 1px;
		background: var(--color-border);
	}

	.divider span {
		font-size: 0.8125rem;
		color: var(--color-text-muted);
	}

	.oauth {
		display: flex;
		flex-direction: column;
		gap: 0.75rem;
	}

	.oauth-btn {
		display: flex;
		align-items: center;
		justify-content: center;
		gap: 0.75rem;
		width: 100%;
		padding: 0.75rem 1.5rem;
		background: var(--color-surface);
		border: 1px solid var(--color-border);
		border-radius: 4px;
		font-family: inherit;
		font-size: 0.9375rem;
		color: var(--color-text);
		cursor: pointer;
		transition: border-color 0.15s;
	}

	.oauth-btn:hover:not(:disabled) {
		border-color: var(--color-text-muted);
	}

	.oauth-btn:disabled {
		opacity: 0.6;
		cursor: not-allowed;
	}

	.footer-links {
		text-align: center;
		font-size: 0.875rem;
		color: var(--color-text-muted);
		margin-top: 1.5rem;
	}

	.footer-links a {
		color: var(--color-text);
		font-weight: 500;
	}

	.footer-links p {
		margin: 0.5rem 0 0;
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
		background: none;
		border: none;
		color: var(--color-text-muted);
		font-family: inherit;
		font-size: 0.875rem;
		text-decoration: underline;
		cursor: pointer;
		margin-top: 1.5rem;
	}

	.text-btn:hover {
		color: var(--color-text);
	}
</style>
