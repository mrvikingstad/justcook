<script lang="ts">
	interface Equipment {
		name: string;
		notes?: string | null;
	}

	interface Props {
		equipment: Equipment[];
	}

	let { equipment }: Props = $props();
</script>

{#if equipment.length > 0}
	<ul class="equipment">
		{#each equipment as item}
			<li>
				<span class="name">{item.name}</span>
				{#if item.notes}
					<span class="notes">({item.notes})</span>
				{/if}
			</li>
		{/each}
	</ul>
{/if}

<style>
	.equipment {
		list-style: none;
		padding: 0;
		margin: 0;
	}

	li {
		display: flex;
		align-items: baseline;
		gap: 0.5rem;
		padding: 0.5rem 0;
		border-bottom: 1px solid var(--color-border);
	}

	li:last-child {
		border-bottom: none;
	}

	.name {
		font-weight: 500;
	}

	.notes {
		color: var(--color-text-muted);
		font-size: 0.875em;
	}

	@media (max-width: 640px) {
		li {
			font-size: 0.9375rem;
			padding: 0.625rem 0;
		}
	}
</style>
