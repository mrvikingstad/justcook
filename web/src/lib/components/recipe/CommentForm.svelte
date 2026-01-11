<script lang="ts">
	interface Props {
		placeholder?: string;
		submitLabel?: string;
		initialValue?: string;
		onSubmit?: (content: string) => void;
		onCancel?: () => void;
		showCancel?: boolean;
	}

	let {
		placeholder = 'Write a comment...',
		submitLabel = 'Post',
		initialValue = '',
		onSubmit,
		onCancel,
		showCancel = false
	}: Props = $props();

	let content = $state(initialValue);
	const canSubmit = $derived(content.trim().length > 0);

	function handleSubmit(e: SubmitEvent) {
		e.preventDefault();
		if (canSubmit && onSubmit) {
			onSubmit(content.trim());
			content = '';
		}
	}
</script>

<form class="form" onsubmit={handleSubmit}>
	<textarea
		bind:value={content}
		{placeholder}
		rows="3"
	></textarea>
	<div class="actions">
		{#if showCancel}
			<button type="button" class="cancel" onclick={onCancel}>Cancel</button>
		{/if}
		<button type="submit" class="submit" disabled={!canSubmit}>{submitLabel}</button>
	</div>
</form>

<style>
	.form {
		display: flex;
		flex-direction: column;
		gap: 0.75rem;
	}

	textarea {
		width: 100%;
		padding: 0.75rem;
		font-family: inherit;
		font-size: 0.9375rem;
		line-height: 1.5;
		background: var(--color-surface);
		border: 1px solid var(--color-border);
		border-radius: 4px;
		color: var(--color-text);
		resize: vertical;
		min-height: 80px;
	}

	textarea::placeholder {
		color: var(--color-text-muted);
	}

	textarea:focus {
		outline: none;
		border-color: var(--color-text);
	}

	.actions {
		display: flex;
		justify-content: flex-end;
		gap: 0.75rem;
	}

	button {
		padding: 0.5rem 1rem;
		font-family: inherit;
		font-size: 0.875rem;
		border-radius: 4px;
		cursor: pointer;
		transition: all 0.15s;
	}

	.cancel {
		background: none;
		border: none;
		color: var(--color-text-muted);
	}

	.cancel:hover {
		color: var(--color-text);
	}

	.submit {
		background: var(--color-text);
		border: none;
		color: var(--color-bg);
		font-weight: 500;
	}

	.submit:hover:not(:disabled) {
		opacity: 0.8;
	}

	.submit:disabled {
		opacity: 0.4;
		cursor: not-allowed;
	}
</style>
