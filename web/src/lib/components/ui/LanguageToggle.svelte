<script lang="ts">
	import { Globe } from 'lucide-svelte';
	import { languageMode, userLanguageCode, toggleLanguageMode } from '$lib/stores/languageMode';
	import { getLanguage } from '$lib/data/languages';

	interface Props {
		onToggle?: (lang: string) => void;
	}

	let { onToggle }: Props = $props();

	const localLanguage = $derived(
		$userLanguageCode ? getLanguage($userLanguageCode) : null
	);

	const isLocal = $derived($languageMode === 'local');

	function handleToggle() {
		toggleLanguageMode();
		// Notify parent of the new effective language
		if (onToggle) {
			const newLang = $languageMode === 'local' && $userLanguageCode ? $userLanguageCode : 'en';
			onToggle(newLang);
		}
	}
</script>

{#if $userLanguageCode && $userLanguageCode !== 'en'}
	<div class="toggle-wrapper">
		<button
			class="toggle"
			class:local={isLocal}
			onclick={handleToggle}
			title={isLocal ? 'Switch to Global (English)' : `Switch to ${localLanguage?.name || 'Local'}`}
		>
			<span class="option" class:active={!isLocal}>
				<Globe size={14} />
				<span class="label">Global</span>
			</span>
			<span class="option" class:active={isLocal}>
				<span class="label">{localLanguage?.nativeName || localLanguage?.name || 'Local'}</span>
			</span>
		</button>
	</div>
{/if}

<style>
	.toggle-wrapper {
		display: flex;
		align-items: center;
	}

	.toggle {
		display: flex;
		align-items: center;
		background: var(--color-surface);
		border: 1px solid var(--color-border);
		border-radius: 100px;
		padding: 0.125rem;
		cursor: pointer;
		transition: border-color 0.15s;
	}

	.toggle:hover {
		border-color: var(--color-text-muted);
	}

	.option {
		display: flex;
		align-items: center;
		gap: 0.25rem;
		padding: 0.375rem 0.625rem;
		border-radius: 100px;
		font-size: 0.75rem;
		color: var(--color-text-muted);
		transition: all 0.15s;
	}

	.option.active {
		background: var(--color-text);
		color: var(--color-bg);
	}

	.label {
		white-space: nowrap;
	}

	@media (max-width: 480px) {
		.label {
			display: none;
		}

		.option {
			padding: 0.375rem 0.5rem;
		}

		.option.active .label {
			display: inline;
		}
	}
</style>
