<script lang="ts">
	import { Calendar, BadgeCheck } from 'lucide-svelte';

	let { data } = $props();
	const user = data.user;

	function formatDate(date: Date | string): string {
		const d = typeof date === 'string' ? new Date(date) : date;
		return d.toLocaleDateString('en-US', { month: 'long', year: 'numeric' });
	}
</script>

<svelte:head>
	<title>{user.displayName || user.username} - Just Cook</title>
</svelte:head>

<div class="page container">
	<div class="profile">
		<div class="avatar">
			{#if user.avatar}
				<img src={user.avatar} alt={user.displayName || user.username} />
			{:else}
				<span class="avatar-fallback">{(user.displayName || user.username || 'U')[0]}</span>
			{/if}
		</div>

		<div class="info">
			<div class="name-row">
				<h1>{user.displayName || user.username}</h1>
				{#if user.profileTier === 'author'}
					<span class="badge" title="Verified Author">
						<BadgeCheck size={20} />
					</span>
				{:else if user.profileTier === 'chef'}
					<span class="badge" title="Chef">
						<img src="/chef.svg" alt="Chef" class="badge-icon" />
					</span>
				{/if}
			</div>
			<p class="username">@{user.username}</p>

			<p class="joined">
				<Calendar size={14} />
				Joined {formatDate(user.joinedAt)}
			</p>
		</div>
	</div>

	{#if user.hasChefProfile}
		<div class="chef-link-section">
			<a href="/chef/{user.username}" class="chef-link">
				View chef profile
			</a>
		</div>
	{/if}
</div>

<style>
	.page {
		padding: 2rem 1.5rem 4rem;
		max-width: 500px;
	}

	.profile {
		display: flex;
		gap: 1.5rem;
		align-items: center;
	}

	.avatar {
		flex-shrink: 0;
		width: 80px;
		height: 80px;
		border-radius: 50%;
		overflow: hidden;
		background: var(--color-border);
		display: flex;
		align-items: center;
		justify-content: center;
	}

	.avatar img {
		width: 100%;
		height: 100%;
		object-fit: cover;
	}

	.avatar-fallback {
		font-size: 2rem;
		font-weight: 600;
		text-transform: uppercase;
		color: var(--color-text-muted);
	}

	.info {
		flex: 1;
		min-width: 0;
	}

	.name-row {
		display: flex;
		align-items: center;
		gap: 0.375rem;
	}

	h1 {
		font-size: 1.5rem;
		font-weight: 600;
		margin: 0;
	}

	.badge {
		display: flex;
		color: var(--color-text);
	}

	.badge-icon {
		width: 20px;
		height: 20px;
	}

	:global(.dark) .badge-icon {
		filter: invert(1);
	}

	.username {
		color: var(--color-text-muted);
		margin: 0.125rem 0 0;
	}

	.joined {
		display: flex;
		align-items: center;
		gap: 0.375rem;
		color: var(--color-text-muted);
		font-size: 0.875rem;
		margin: 0.75rem 0 0;
	}

	.chef-link-section {
		margin-top: 2rem;
		padding-top: 2rem;
		border-top: 1px solid var(--color-border);
	}

	.chef-link {
		display: inline-block;
		color: var(--color-text);
		font-weight: 500;
	}
</style>
