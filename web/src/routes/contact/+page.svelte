<script lang="ts">
	import { Mail, MessageSquare, HelpCircle } from 'lucide-svelte';

	let name = $state('');
	let email = $state('');
	let subject = $state('general');
	let message = $state('');
	let isSubmitting = $state(false);
	let submitted = $state(false);
	let error = $state('');

	const subjects = [
		{ value: 'general', label: 'General Inquiry' },
		{ value: 'support', label: 'Technical Support' },
		{ value: 'feedback', label: 'Feedback' },
		{ value: 'report', label: 'Report an Issue' },
		{ value: 'business', label: 'Business Inquiry' }
	];

	async function handleSubmit(e: Event) {
		e.preventDefault();
		error = '';

		if (!name.trim() || !email.trim() || !message.trim()) {
			error = 'Please fill in all required fields.';
			return;
		}

		isSubmitting = true;

		// For now, we'll simulate a submission
		// In production, wire this up to an email service or API
		await new Promise((resolve) => setTimeout(resolve, 1000));

		submitted = true;
		isSubmitting = false;
	}

	function resetForm() {
		name = '';
		email = '';
		subject = 'general';
		message = '';
		submitted = false;
	}
</script>

<svelte:head>
	<title>Contact Us - JustCook</title>
	<meta name="description" content="Get in touch with JustCook - we're here to help with questions, feedback, and support." />
</svelte:head>

<div class="contact-page container">
	<header class="page-header">
		<h1>Contact Us</h1>
		<p class="subtitle">Have a question or feedback? We'd love to hear from you.</p>
	</header>

	{#if submitted}
		<div class="success-message">
			<div class="success-icon">
				<Mail size={32} />
			</div>
			<h2>Message Sent!</h2>
			<p>Thank you for reaching out. We'll get back to you as soon as possible.</p>
			<button onclick={resetForm} class="btn btn-secondary">Send Another Message</button>
		</div>
	{:else}
		<div class="contact-content">
			<div class="contact-info">
				<div class="info-card">
					<div class="info-icon">
						<HelpCircle size={20} />
					</div>
					<div>
						<h3>Support</h3>
						<p>Having trouble with JustCook? Let us know and we'll help you out.</p>
					</div>
				</div>
				<div class="info-card">
					<div class="info-icon">
						<MessageSquare size={20} />
					</div>
					<div>
						<h3>Feedback</h3>
						<p>We're always looking to improve. Share your ideas and suggestions.</p>
					</div>
				</div>
			</div>

			<form class="contact-form" onsubmit={handleSubmit}>
				{#if error}
					<div class="error-message">{error}</div>
				{/if}

				<div class="form-row">
					<div class="form-group">
						<label for="name">Name <span class="required">*</span></label>
						<input
							type="text"
							id="name"
							bind:value={name}
							placeholder="Your name"
							required
						/>
					</div>
					<div class="form-group">
						<label for="email">Email <span class="required">*</span></label>
						<input
							type="email"
							id="email"
							bind:value={email}
							placeholder="your@email.com"
							required
						/>
					</div>
				</div>

				<div class="form-group">
					<label for="subject">Subject</label>
					<select id="subject" bind:value={subject}>
						{#each subjects as opt}
							<option value={opt.value}>{opt.label}</option>
						{/each}
					</select>
				</div>

				<div class="form-group">
					<label for="message">Message <span class="required">*</span></label>
					<textarea
						id="message"
						bind:value={message}
						placeholder="How can we help you?"
						rows="6"
						required
					></textarea>
				</div>

				<button type="submit" class="btn btn-primary" disabled={isSubmitting}>
					{#if isSubmitting}
						Sending...
					{:else}
						Send Message
					{/if}
				</button>
			</form>
		</div>
	{/if}
</div>

<style>
	.contact-page {
		padding: 3rem 1.5rem;
		max-width: 800px;
	}

	.page-header {
		text-align: center;
		margin-bottom: 2.5rem;
	}

	.page-header h1 {
		font-size: 2rem;
		font-weight: 700;
		margin: 0 0 0.5rem 0;
	}

	.subtitle {
		color: var(--color-text-muted);
		font-size: 1.0625rem;
		margin: 0;
	}

	.contact-content {
		display: grid;
		gap: 2.5rem;
	}

	.contact-info {
		display: grid;
		grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
		gap: 1rem;
	}

	.info-card {
		display: flex;
		gap: 1rem;
		padding: 1.25rem;
		background: var(--color-surface);
		border: 1px solid var(--color-border);
		border-radius: 12px;
	}

	.info-icon {
		flex-shrink: 0;
		width: 40px;
		height: 40px;
		display: flex;
		align-items: center;
		justify-content: center;
		background: var(--color-bg);
		border-radius: 10px;
		color: var(--color-text);
	}

	.info-card h3 {
		font-size: 1rem;
		font-weight: 600;
		margin: 0 0 0.25rem 0;
	}

	.info-card p {
		font-size: 0.875rem;
		color: var(--color-text-muted);
		margin: 0;
		line-height: 1.5;
	}

	.contact-form {
		background: var(--color-surface);
		border: 1px solid var(--color-border);
		border-radius: 16px;
		padding: 2rem;
	}

	.error-message {
		background: #fef2f2;
		border: 1px solid #fecaca;
		color: #dc2626;
		padding: 0.875rem 1rem;
		border-radius: 8px;
		margin-bottom: 1.5rem;
		font-size: 0.9375rem;
	}

	:global(.dark) .error-message {
		background: rgba(220, 38, 38, 0.1);
		border-color: rgba(220, 38, 38, 0.3);
	}

	.form-row {
		display: grid;
		grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
		gap: 1rem;
	}

	.form-group {
		margin-bottom: 1.25rem;
	}

	.form-group label {
		display: block;
		font-size: 0.9375rem;
		font-weight: 500;
		margin-bottom: 0.5rem;
	}

	.required {
		color: #dc2626;
	}

	.form-group input,
	.form-group select,
	.form-group textarea {
		width: 100%;
		padding: 0.75rem 1rem;
		font-size: 1rem;
		font-family: inherit;
		background: var(--color-bg);
		border: 1px solid var(--color-border);
		border-radius: 8px;
		color: var(--color-text);
		transition: border-color 0.15s;
	}

	.form-group input:focus,
	.form-group select:focus,
	.form-group textarea:focus {
		outline: none;
		border-color: var(--color-text);
	}

	.form-group textarea {
		resize: vertical;
		min-height: 120px;
	}

	.btn {
		display: inline-flex;
		align-items: center;
		justify-content: center;
		padding: 0.875rem 1.75rem;
		border-radius: 8px;
		font-size: 1rem;
		font-weight: 500;
		text-decoration: none;
		border: none;
		cursor: pointer;
		transition: all 0.15s;
	}

	.btn:disabled {
		opacity: 0.6;
		cursor: not-allowed;
	}

	.btn-primary {
		background: var(--color-text);
		color: var(--color-bg);
	}

	.btn-primary:hover:not(:disabled) {
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

	.success-message {
		text-align: center;
		padding: 3rem 2rem;
		background: var(--color-surface);
		border: 1px solid var(--color-border);
		border-radius: 16px;
	}

	.success-icon {
		width: 64px;
		height: 64px;
		display: flex;
		align-items: center;
		justify-content: center;
		background: #dcfce7;
		color: #16a34a;
		border-radius: 50%;
		margin: 0 auto 1.5rem;
	}

	:global(.dark) .success-icon {
		background: rgba(22, 163, 74, 0.15);
	}

	.success-message h2 {
		font-size: 1.5rem;
		font-weight: 600;
		margin: 0 0 0.5rem 0;
	}

	.success-message p {
		color: var(--color-text-muted);
		margin: 0 0 1.5rem 0;
	}

	@media (max-width: 600px) {
		.contact-form {
			padding: 1.5rem;
		}
	}
</style>
