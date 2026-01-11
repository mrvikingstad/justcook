<script lang="ts">
	import { theme, toggleTheme } from '$lib/stores/theme';
	import { signOut } from '$lib/auth/client';
	import { BookOpen, Bookmark, Users, User, LogOut, LogIn, Lightbulb, Menu, X, Settings, ChevronDown } from 'lucide-svelte';

	interface Props {
		user?: { id: string; name: string; email: string; image?: string | null; username?: string | null } | null;
	}

	let { user = null }: Props = $props();
	let mobileMenuOpen = $state(false);
	let profileMenuOpen = $state(false);

	async function handleSignOut() {
		await signOut();
		window.location.href = '/';
	}

	function closeMenu() {
		mobileMenuOpen = false;
	}

	function toggleProfileMenu() {
		profileMenuOpen = !profileMenuOpen;
	}

	function closeProfileMenu() {
		profileMenuOpen = false;
	}

	function handleClickOutside(event: MouseEvent) {
		const target = event.target as HTMLElement;
		if (!target.closest('.profile-menu-container')) {
			profileMenuOpen = false;
		}
	}
</script>

<svelte:window onclick={handleClickOutside} />

<header class="header">
	<div class="header-inner container">
		<div class="brand">
		<a href="/" class="logo">
			<img src="/chef-hat.svg" alt="" class="logo-icon" />
			Just Cook
		</a>
	</div>

	<nav class="desktop-nav">
		<a href="/recipes">
			<BookOpen size={16} />
			Recipes
		</a>
		<a href="/bookmarks">
			<Bookmark size={16} />
			Bookmarks
		</a>
		{#if user}
			<a href="/following">
				<Users size={16} />
				Following
			</a>
			<div class="profile-menu-container">
				<button onclick={toggleProfileMenu} class="profile-trigger">
					<User size={16} />
					Profile
					<ChevronDown size={14} class={profileMenuOpen ? 'chevron-up' : ''} />
				</button>
				{#if profileMenuOpen}
					<div class="profile-dropdown">
						<a href="/chef/{user.username || user.id}" onclick={closeProfileMenu}>
							<User size={16} />
							Profile
						</a>
						<a href="/settings" onclick={closeProfileMenu}>
							<Settings size={16} />
							Settings
						</a>
						<button onclick={() => { closeProfileMenu(); handleSignOut(); }} class="dropdown-btn">
							<LogOut size={16} />
							Sign out
						</button>
					</div>
				{/if}
			</div>
		{:else}
			<a href="/auth/login">
				<LogIn size={16} />
				Sign in
			</a>
		{/if}
		<button onclick={toggleTheme} class="theme-toggle" aria-label="Toggle theme">
			<Lightbulb size={18} class={$theme === 'light' ? 'bulb-on' : 'bulb-off'} />
		</button>
	</nav>

		<div class="mobile-controls">
			<button class="menu-toggle" onclick={() => mobileMenuOpen = !mobileMenuOpen} aria-label="Toggle menu">
				{#if mobileMenuOpen}
					<X size={24} />
				{:else}
					<Menu size={24} />
				{/if}
			</button>
		</div>
	</div>
</header>

{#if mobileMenuOpen}
	<div class="mobile-menu-overlay" onclick={closeMenu}></div>
	<nav class="mobile-menu">
		<a href="/recipes" onclick={closeMenu}>
			<BookOpen size={18} />
			Recipes
		</a>
		<a href="/bookmarks" onclick={closeMenu}>
			<Bookmark size={18} />
			Bookmarks
		</a>
		{#if user}
			<a href="/following" onclick={closeMenu}>
				<Users size={18} />
				Following
			</a>
			<div class="mobile-menu-divider"></div>
			<a href="/chef/{user.username || user.id}" onclick={closeMenu}>
				<User size={18} />
				Profile
			</a>
			<a href="/settings" onclick={closeMenu}>
				<Settings size={18} />
				Settings
			</a>
			<button onclick={() => { closeMenu(); handleSignOut(); }} class="nav-btn">
				<LogOut size={18} />
				Sign out
			</button>
		{:else}
			<a href="/auth/login" onclick={closeMenu}>
				<LogIn size={18} />
				Sign in
			</a>
		{/if}
		<div class="mobile-menu-divider"></div>
		<button onclick={toggleTheme} class="nav-btn theme-toggle-mobile">
			<Lightbulb size={18} class={$theme === 'light' ? 'bulb-on' : 'bulb-off'} />
			{$theme === 'light' ? 'Dark mode' : 'Light mode'}
		</button>
	</nav>
{/if}

<style>
	.header {
		width: 100%;
	}

	.header-inner {
		display: flex;
		align-items: center;
		justify-content: space-between;
		padding-top: 1.5rem;
		padding-bottom: 1.5rem;
		border-bottom: 1px solid var(--color-border);
	}

	.brand {
		flex-shrink: 0;
	}

	.logo {
		display: flex;
		align-items: center;
		gap: 0.5rem;
		font-weight: 600;
		text-decoration: none;
	}

	.logo-icon {
		width: 24px;
		height: 24px;
	}

	:global(.dark) .logo-icon {
		filter: invert(1);
	}

	.desktop-nav {
		display: flex;
		gap: 1.5rem;
		align-items: center;
	}

	.desktop-nav a {
		display: flex;
		align-items: center;
		gap: 0.5rem;
		color: var(--color-text-muted);
		text-decoration: none;
		font-size: 0.9375rem;
		transition: color 0.15s;
	}

	.desktop-nav a:hover {
		color: var(--color-text);
	}

	.theme-toggle,
	.nav-btn {
		display: flex;
		align-items: center;
		gap: 0.375rem;
		background: none;
		border: none;
		color: var(--color-text-muted);
		font-family: inherit;
		font-size: 0.9375rem;
		padding: 0;
		cursor: pointer;
	}

	.theme-toggle:hover,
	.nav-btn:hover {
		color: var(--color-text);
	}

	.theme-toggle-mobile {
		width: 100%;
		justify-content: flex-start;
	}

	:global(.bulb-on) {
		fill: var(--color-text);
		color: var(--color-text);
	}

	:global(.bulb-off) {
		fill: none;
		color: var(--color-text-muted);
	}

	/* Mobile controls */
	.mobile-controls {
		display: none;
		align-items: center;
		gap: 0.5rem;
	}

	.menu-toggle {
		display: flex;
		align-items: center;
		justify-content: center;
		background: none;
		border: none;
		color: var(--color-text);
		padding: 0.25rem;
		cursor: pointer;
	}

	/* Mobile menu overlay */
	.mobile-menu-overlay {
		position: fixed;
		inset: 0;
		background: rgba(0, 0, 0, 0.5);
		z-index: 998;
	}

	/* Mobile menu */
	.mobile-menu {
		position: fixed;
		top: 0;
		right: 0;
		bottom: 0;
		width: 280px;
		max-width: 80vw;
		background: var(--color-bg);
		border-left: 1px solid var(--color-border);
		padding: 1.5rem;
		display: flex;
		flex-direction: column;
		gap: 0.5rem;
		z-index: 999;
		overflow-y: auto;
	}

	.mobile-menu a,
	.mobile-menu .nav-btn {
		display: flex;
		align-items: center;
		gap: 0.75rem;
		padding: 0.875rem 0.75rem;
		color: var(--color-text);
		text-decoration: none;
		font-size: 1rem;
		border-radius: 8px;
		transition: background-color 0.15s;
	}

	.mobile-menu a:hover,
	.mobile-menu .nav-btn:hover {
		background: var(--color-surface);
	}

	/* Profile dropdown */
	.profile-menu-container {
		position: relative;
	}

	.profile-trigger {
		display: flex;
		align-items: center;
		gap: 0.5rem;
		background: none;
		border: none;
		color: var(--color-text-muted);
		font-family: inherit;
		font-size: 0.9375rem;
		padding: 0;
		cursor: pointer;
		transition: color 0.15s;
	}

	.profile-trigger:hover {
		color: var(--color-text);
	}

	:global(.chevron-up) {
		transform: rotate(180deg);
	}

	.profile-dropdown {
		position: absolute;
		top: calc(100% + 0.5rem);
		right: 0;
		min-width: 160px;
		background: var(--color-bg);
		border: 1px solid var(--color-border);
		border-radius: 8px;
		padding: 0.5rem;
		box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
		z-index: 100;
	}

	.profile-dropdown a,
	.dropdown-btn {
		display: flex;
		align-items: center;
		gap: 0.625rem;
		width: 100%;
		padding: 0.625rem 0.75rem;
		color: var(--color-text);
		text-decoration: none;
		font-size: 0.9375rem;
		border-radius: 6px;
		transition: background-color 0.15s;
		background: none;
		border: none;
		font-family: inherit;
		cursor: pointer;
	}

	.profile-dropdown a:hover,
	.dropdown-btn:hover {
		background: var(--color-surface);
	}

	/* Mobile menu divider */
	.mobile-menu-divider {
		height: 1px;
		background: var(--color-border);
		margin: 0.5rem 0;
	}

	/* Mobile breakpoint */
	@media (max-width: 768px) {
		.desktop-nav {
			display: none;
		}

		.mobile-controls {
			display: flex;
		}
	}
</style>
