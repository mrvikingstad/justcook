<script lang="ts">
	interface Tip {
		content: string;
	}

	interface Props {
		tips: Tip[];
	}

	let { tips }: Props = $props();
</script>

{#if tips.length > 0}
	<ul class="tips">
		{#each tips as tip}
			<li>{tip.content}</li>
		{/each}
	</ul>
{/if}

<style>
	.tips {
		list-style: disc;
		padding-left: 1.25rem;
		margin: 0;
	}

	li {
		padding: 0.375rem 0;
		line-height: 1.5;
		color: var(--color-text-secondary);
	}

	li::marker {
		color: var(--color-text-muted);
	}

	@media (max-width: 640px) {
		li {
			font-size: 0.9375rem;
		}
	}
</style>
