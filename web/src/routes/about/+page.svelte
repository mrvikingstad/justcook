<script lang="ts">
	import { ChefHat, Heart, Users, Utensils } from 'lucide-svelte';
</script>

<svelte:head>
	<title>About - JustCook</title>
	<meta name="description" content="Learn about JustCook - a community-driven recipe sharing platform focused on simplicity and great food." />
</svelte:head>

<div class="about-page container">
	<header class="page-header">
		<h1>About JustCook</h1>
		<p class="subtitle">Just recipes. Simple, as they should be.</p>
	</header>

	<section class="mission">
		<h2>Our Mission</h2>
		<p>
			JustCook was born from a simple frustration: finding a recipe online shouldn't mean scrolling
			through someone's life story before getting to the ingredients. We believe cooking should be
			accessible, enjoyable, and straightforward.
		</p>
		<p>
			Our platform is designed to get you from hungry to cooking in seconds, not minutes.
			No ads, no endless scrolling, no distractions &mdash; just the recipes you need.
		</p>
	</section>

	<section class="values">
		<h2>What We Value</h2>
		<div class="values-grid">
			<div class="value-card">
				<div class="value-icon">
					<Utensils size={24} />
				</div>
				<h3>Simplicity</h3>
				<p>Clean, focused recipes without the clutter. Get to cooking faster.</p>
			</div>
			<div class="value-card">
				<div class="value-icon">
					<Users size={24} />
				</div>
				<h3>Community</h3>
				<p>Real recipes from real home cooks. Share, discover, and learn together.</p>
			</div>
			<div class="value-card">
				<div class="value-icon">
					<Heart size={24} />
				</div>
				<h3>Quality</h3>
				<p>Every recipe is crafted with care. Our tier system rewards the best contributors.</p>
			</div>
		</div>
	</section>

	<section class="tiers">
		<h2>Our Chef Tiers</h2>
		<p>
			JustCook celebrates our contributors through a tier system. As you share more recipes
			and engage with the community, you progress from <strong>User</strong> to <strong>Author</strong>
			to <strong>Chef</strong>. Each tier recognizes your contribution to our growing recipe collection.
		</p>
	</section>

	<section class="cta">
		<ChefHat size={32} />
		<h2>Ready to Cook?</h2>
		<p>Join our community of home cooks and food enthusiasts.</p>
		<div class="cta-buttons">
			<a href="/recipes" class="btn btn-primary">Browse Recipes</a>
			<a href="/auth/login" class="btn btn-secondary">Sign Up</a>
		</div>
	</section>
</div>

<style>
	.about-page {
		padding: 3rem 1.5rem;
	}

	.page-header {
		text-align: center;
		margin-bottom: 3rem;
	}

	.page-header h1 {
		font-size: 2.5rem;
		font-weight: 700;
		margin: 0 0 0.5rem 0;
	}

	.subtitle {
		color: var(--color-text-muted);
		font-size: 1.125rem;
		margin: 0;
	}

	section {
		margin-bottom: 3rem;
	}

	section h2 {
		font-size: 1.5rem;
		font-weight: 600;
		margin: 0 0 1rem 0;
	}

	section p {
		color: var(--color-text-muted);
		line-height: 1.7;
		margin: 0 0 1rem 0;
	}

	section p:last-child {
		margin-bottom: 0;
	}

	.values-grid {
		display: grid;
		grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
		gap: 1.5rem;
		margin-top: 1.5rem;
	}

	.value-card {
		background: var(--color-surface);
		border: 1px solid var(--color-border);
		border-radius: 12px;
		padding: 1.5rem;
	}

	.value-icon {
		width: 48px;
		height: 48px;
		display: flex;
		align-items: center;
		justify-content: center;
		background: var(--color-bg);
		border-radius: 12px;
		margin-bottom: 1rem;
		color: var(--color-text);
	}

	.value-card h3 {
		font-size: 1.125rem;
		font-weight: 600;
		margin: 0 0 0.5rem 0;
	}

	.value-card p {
		font-size: 0.9375rem;
		margin: 0;
	}

	.cta {
		text-align: center;
		background: var(--color-surface);
		border: 1px solid var(--color-border);
		border-radius: 16px;
		padding: 3rem 2rem;
		margin-top: 4rem;
	}

	.cta h2 {
		margin-top: 1rem;
	}

	.cta p {
		color: var(--color-text-muted);
	}

	.cta-buttons {
		display: flex;
		gap: 1rem;
		justify-content: center;
		margin-top: 1.5rem;
		flex-wrap: wrap;
	}

	.btn {
		display: inline-flex;
		align-items: center;
		justify-content: center;
		padding: 0.75rem 1.5rem;
		border-radius: 8px;
		font-size: 1rem;
		font-weight: 500;
		text-decoration: none;
		transition: all 0.15s;
	}

	.btn-primary {
		background: var(--color-text);
		color: var(--color-bg);
	}

	.btn-primary:hover {
		opacity: 0.9;
	}

	.btn-secondary {
		background: transparent;
		border: 1px solid var(--color-border);
		color: var(--color-text);
	}

	.btn-secondary:hover {
		background: var(--color-surface);
	}

	@media (max-width: 600px) {
		.page-header h1 {
			font-size: 2rem;
		}

		.cta {
			padding: 2rem 1.5rem;
		}
	}
</style>
