<script lang="ts">
	import { ChevronDown } from 'lucide-svelte';
	import { portal } from '$lib/actions/portal';

	interface Option {
		value: string;
		label: string;
	}

	interface Props {
		options: Option[];
		value: string;
		onchange?: (value: string) => void;
	}

	let { options, value = $bindable(), onchange }: Props = $props();

	let isOpen = $state(false);
	let triggerEl: HTMLButtonElement;
	let dropdownX = $state(0);
	let dropdownY = $state(0);
	let dropdownWidth = $state(0);

	const selectedLabel = $derived(
		options.find((o) => o.value === value)?.label ?? options[0]?.label ?? ''
	);

	function toggle() {
		if (!isOpen && triggerEl) {
			const rect = triggerEl.getBoundingClientRect();
			dropdownX = rect.left;
			dropdownY = rect.bottom + 4;
			dropdownWidth = rect.width;
		}
		isOpen = !isOpen;
	}

	function select(optionValue: string) {
		value = optionValue;
		onchange?.(optionValue);
		isOpen = false;
	}

	function handleClickOutside(e: MouseEvent) {
		if (isOpen && triggerEl && !triggerEl.contains(e.target as Node)) {
			isOpen = false;
		}
	}

	function handleKeydown(e: KeyboardEvent) {
		if (e.key === 'Escape') {
			isOpen = false;
		}
	}
</script>

<svelte:window onclick={handleClickOutside} onkeydown={handleKeydown} />

<button
	class="select-trigger"
	onclick={toggle}
	bind:this={triggerEl}
	aria-haspopup="listbox"
	aria-expanded={isOpen}
>
	<span class="select-value">{selectedLabel}</span>
	<ChevronDown size={14} class="select-chevron {isOpen ? 'open' : ''}" />
</button>

{#if isOpen}
	<div
		class="select-dropdown"
		style="left: {dropdownX}px; top: {dropdownY}px; min-width: {dropdownWidth}px;"
		use:portal={'body'}
		role="listbox"
	>
		{#each options as option}
			<button
				class="select-option"
				class:selected={option.value === value}
				onclick={() => select(option.value)}
				role="option"
				aria-selected={option.value === value}
			>
				{option.label}
			</button>
		{/each}
	</div>
{/if}

<style>
	.select-trigger {
		display: flex;
		align-items: center;
		gap: 0.5rem;
		padding: 0.375rem 0.625rem;
		background: var(--color-surface);
		border: 1px solid var(--color-border);
		border-radius: 6px;
		font-family: inherit;
		font-size: 0.8125rem;
		color: var(--color-text);
		cursor: pointer;
		transition: border-color 0.15s, box-shadow 0.15s;
	}

	.select-trigger:hover {
		border-color: var(--color-text-muted);
	}

	.select-trigger:focus {
		outline: none;
		border-color: var(--color-text-muted);
		box-shadow: 0 0 0 2px var(--color-bg), 0 0 0 4px var(--color-border);
	}

	.select-value {
		flex: 1;
		text-align: left;
	}

	:global(.select-chevron) {
		color: var(--color-text-muted);
		transition: transform 0.2s;
	}

	:global(.select-chevron.open) {
		transform: rotate(180deg);
	}

	/* Dropdown - rendered via portal */
	:global(.select-dropdown) {
		position: fixed;
		display: flex;
		flex-direction: column;
		background: var(--color-surface);
		border: 1px solid var(--color-border);
		border-radius: 6px;
		box-shadow: var(--shadow-elevation-medium);
		z-index: 9999;
		overflow: hidden;
	}

	:global(.select-option) {
		padding: 0.5rem 0.75rem;
		background: none;
		border: none;
		font-family: inherit;
		font-size: 0.8125rem;
		color: var(--color-text);
		text-align: left;
		cursor: pointer;
		transition: background-color 0.1s;
	}

	:global(.select-option:hover) {
		background: var(--color-border);
	}

	:global(.select-option.selected) {
		font-weight: 500;
		background: var(--color-border);
	}
</style>
