<script lang="ts">
	import { BadgeCheck } from 'lucide-svelte';
	import type { ProfileTier } from '$lib/server/db/schema/users';

	interface Props {
		tier: ProfileTier;
		size?: 'sm' | 'md' | 'lg';
		showLabel?: boolean;
	}

	let { tier, size = 'md', showLabel = false }: Props = $props();

	const sizes = {
		sm: 14,
		md: 18,
		lg: 22
	};

	const labels: Record<ProfileTier, string> = {
		user: 'Member',
		author: 'Author',
		chef: 'Chef'
	};
</script>

{#if tier === 'author'}
	<span class="badge author" class:size-sm={size === 'sm'} class:size-lg={size === 'lg'} title="Verified Author">
		<BadgeCheck size={sizes[size]} />
		{#if showLabel}<span class="label">{labels[tier]}</span>{/if}
	</span>
{:else if tier === 'chef'}
	<span class="badge chef" class:size-sm={size === 'sm'} class:size-lg={size === 'lg'} title="Chef">
		<img src="/chef.svg" alt="" class="chef-icon" style="width: {sizes[size]}px; height: {sizes[size]}px;" />
		{#if showLabel}<span class="label">{labels[tier]}</span>{/if}
	</span>
{/if}

<style>
	.badge {
		display: inline-flex;
		align-items: center;
		gap: 0.25rem;
	}

	.author {
		color: var(--color-text);
	}

	.chef {
		color: var(--color-text);
	}

	.chef-icon {
		display: block;
	}

	:global(.dark) .chef-icon {
		filter: invert(1);
	}

	.label {
		font-size: 0.75rem;
		font-weight: 500;
	}

	.size-lg .label {
		font-size: 0.875rem;
	}
</style>
