<script lang="ts">
	import { ChefHat } from 'lucide-svelte';
</script>

<footer class="footer">
	<div class="footer-content container">
		<div class="footer-grid">
			<div class="footer-section">
				<h4>JustCook</h4>
				<nav>
					<a href="/about">About</a>
					<a href="/contact">Contact Us</a>
				</nav>
			</div>

			<div class="footer-section">
				<h4>Legal</h4>
				<nav>
					<a href="/terms">Terms of Service</a>
					<a href="/privacy">Privacy Policy</a>
				</nav>
			</div>
		</div>

		<div class="footer-bottom">
			<div class="copyright">
				<ChefHat size={16} />
				<span>&copy; {new Date().getFullYear()} JustCook</span>
			</div>
			<a href="https://kestrel.no" target="_blank" rel="noopener noreferrer" class="built-by">
				Built by Kestrel
			</a>
		</div>
	</div>
</footer>

<style>
	.footer {
		margin-top: auto;
		border-top: 1px solid var(--color-border);
		background: var(--color-surface);
	}

	.footer-content {
		padding-top: 3rem;
		padding-bottom: 2rem;
	}

	.footer-grid {
		display: grid;
		grid-template-columns: repeat(2, 1fr);
		gap: 2rem;
		max-width: 400px;
	}

	.footer-section h4 {
		font-size: 0.875rem;
		font-weight: 600;
		margin: 0 0 1rem 0;
		color: var(--color-text);
	}

	.footer-section nav {
		display: flex;
		flex-direction: column;
		gap: 0.625rem;
	}

	.footer-section a {
		color: var(--color-text-muted);
		text-decoration: none;
		font-size: 0.875rem;
		transition: color 0.15s;
	}

	.footer-section a:hover {
		color: var(--color-text);
	}

	.footer-bottom {
		display: flex;
		justify-content: space-between;
		align-items: center;
		margin-top: 2.5rem;
		padding-top: 1.5rem;
		border-top: 1px solid var(--color-border);
	}

	.copyright {
		display: flex;
		align-items: center;
		gap: 0.5rem;
		color: var(--color-text-muted);
		font-size: 0.875rem;
	}

	.built-by {
		color: var(--color-text-muted);
		text-decoration: none;
		font-size: 0.875rem;
		transition: color 0.15s;
	}

	.built-by:hover {
		color: var(--color-text);
	}

	@media (max-width: 480px) {
		.footer-grid {
			grid-template-columns: 1fr;
			gap: 1.5rem;
		}

		.footer-bottom {
			flex-direction: column;
			gap: 1rem;
			text-align: center;
		}
	}
</style>
