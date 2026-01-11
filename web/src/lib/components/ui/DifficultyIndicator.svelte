<script lang="ts">
	import { portal } from '$lib/actions/portal';

	interface Props {
		level: 'easy' | 'medium' | 'hard';
		showLabel?: boolean;
		size?: 'sm' | 'md';
	}

	let { level, showLabel = true, size = 'sm' }: Props = $props();

	const labels = {
		easy: 'Easy',
		medium: 'Medium',
		hard: 'Hard'
	};

	const descriptions = {
		easy: 'Great for beginners',
		medium: 'Some experience needed',
		hard: 'Advanced techniques required'
	};

	let showTooltip = $state(false);
	let tooltipX = $state(0);
	let tooltipY = $state(0);
	let containerEl: HTMLDivElement;

	function handleMouseEnter() {
		if (containerEl) {
			const rect = containerEl.getBoundingClientRect();
			tooltipX = rect.left + rect.width / 2;
			tooltipY = rect.top;
		}
		showTooltip = true;
	}

	function handleMouseLeave() {
		showTooltip = false;
	}
</script>

<div
	class="difficulty-indicator"
	class:size-sm={size === 'sm'}
	class:size-md={size === 'md'}
	role="img"
	aria-label="Skill level: {labels[level]}"
	onmouseenter={handleMouseEnter}
	onmouseleave={handleMouseLeave}
	bind:this={containerEl}
>
	{#if showLabel}
		<span class="label">Skill</span>
	{/if}
	<span class="hats" data-level={level}>
		<span class="chef-icon-wrapper"><img src="/chef.svg" alt="" class="chef-icon" /></span>
		<span class="chef-icon-wrapper"><img src="/chef.svg" alt="" class="chef-icon" /></span>
		<span class="chef-icon-wrapper"><img src="/chef.svg" alt="" class="chef-icon" /></span>
	</span>
</div>

{#if showTooltip}
	<div class="tooltip" style="left: {tooltipX}px; top: {tooltipY}px;" use:portal={'body'}>
		<span class="tooltip-title">{labels[level]}</span>
		<span class="tooltip-desc">{descriptions[level]}</span>
	</div>
{/if}

<style>
	.difficulty-indicator {
		position: relative;
		display: flex;
		align-items: center;
		gap: 0.375rem;
	}

	.label {
		color: var(--color-text-muted);
	}

	.size-sm .label {
		font-size: 0.75rem;
	}

	.size-sm .chef-icon-wrapper {
		width: 14px;
		height: 14px;
		margin-right: -4px;
	}

	.size-md .label {
		font-size: 0.875rem;
	}

	.size-md .chef-icon-wrapper {
		width: 18px;
		height: 18px;
		margin-right: -5px;
	}

	.hats {
		display: flex;
		align-items: center;
	}

	.chef-icon-wrapper {
		display: flex;
		align-items: center;
		justify-content: center;
		background: var(--color-surface);
		border-radius: 50%;
		opacity: 0.2;
		transition: opacity 0.2s;
	}

	.chef-icon-wrapper:last-child {
		margin-right: 0;
	}

	.chef-icon {
		width: 100%;
		height: 100%;
	}

	:global(.dark) .chef-icon {
		filter: invert(1);
	}

	.hats[data-level='easy'] .chef-icon-wrapper:nth-child(1) {
		opacity: 1;
	}

	.hats[data-level='medium'] .chef-icon-wrapper:nth-child(1),
	.hats[data-level='medium'] .chef-icon-wrapper:nth-child(2) {
		opacity: 1;
	}

	.hats[data-level='hard'] .chef-icon-wrapper {
		opacity: 1;
	}

	/* Tooltip - rendered globally via portal */
	:global(.tooltip) {
		position: fixed;
		transform: translate(-50%, -100%) translateY(-10px);
		display: flex;
		flex-direction: column;
		align-items: center;
		gap: 0.125rem;
		padding: 0.5rem 0.75rem;
		background: var(--color-surface);
		border: 1px solid var(--color-border);
		border-radius: 6px;
		box-shadow: var(--shadow-elevation-medium);
		white-space: nowrap;
		z-index: 9999;
		pointer-events: none;
	}

	:global(.tooltip)::after {
		content: '';
		position: absolute;
		top: 100%;
		left: 50%;
		transform: translateX(-50%);
		border: 6px solid transparent;
		border-top-color: var(--color-border);
	}

	:global(.tooltip)::before {
		content: '';
		position: absolute;
		top: 100%;
		left: 50%;
		transform: translateX(-50%);
		border: 5px solid transparent;
		border-top-color: var(--color-surface);
		z-index: 1;
	}

	:global(.tooltip-title) {
		font-size: 0.8125rem;
		font-weight: 600;
		color: var(--color-text);
	}

	:global(.tooltip-desc) {
		font-size: 0.6875rem;
		color: var(--color-text-muted);
	}
</style>
