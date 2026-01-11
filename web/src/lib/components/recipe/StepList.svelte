<script lang="ts">
	interface Step {
		instruction: string;
	}

	interface Props {
		steps: Step[];
	}

	let { steps }: Props = $props();
</script>

<ol class="steps">
	{#each steps as step, i}
		<li>
			<span class="number">{i + 1}</span>
			<p class="instruction">{step.instruction}</p>
		</li>
	{/each}
</ol>

<style>
	.steps {
		list-style: none;
		padding: 0;
		margin: 0;
	}

	li {
		display: flex;
		gap: 1rem;
		padding: 1rem 0;
		border-bottom: 1px solid var(--color-border);
	}

	li:last-child {
		border-bottom: none;
	}

	.number {
		flex-shrink: 0;
		width: 1.5rem;
		height: 1.5rem;
		display: flex;
		align-items: center;
		justify-content: center;
		font-size: 0.75rem;
		font-weight: 600;
		color: var(--color-text-muted);
		border: 1px solid var(--color-border);
		border-radius: 50%;
	}

	.instruction {
		margin: 0;
		line-height: 1.6;
	}

	@media (max-width: 640px) {
		li {
			gap: 0.75rem;
			padding: 0.875rem 0;
		}

		.instruction {
			font-size: 0.9375rem;
		}
	}
</style>
