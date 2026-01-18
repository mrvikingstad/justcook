<script lang="ts">
	import { onMount, tick } from 'svelte';
	import {
		MessageSquare,
		Send,
		Plus,
		Trash2,
		Menu,
		X,
		User,
		AlertCircle,
		Edit2,
		Check
	} from 'lucide-svelte';
	import { renderMarkdown } from '$lib/utils/markdown';

	interface Message {
		id: string;
		role: 'user' | 'assistant';
		content: string;
		createdAt: string;
	}

	interface Conversation {
		id: string;
		title: string | null;
		messageCount: number;
		updatedAt: string;
	}

	// Constants
	const MAX_MESSAGE_LENGTH = 4000;

	// State
	let conversations = $state<Conversation[]>([]);
	let currentConversationId = $state<string | null>(null);
	let loadingConversationId = $state<string | null>(null); // For race condition prevention
	let messages = $state<Message[]>([]);
	let inputMessage = $state('');
	let isLoading = $state(false);
	let isSending = $state(false);
	let error = $state<string | null>(null);
	let failedMessage = $state<string | null>(null); // For retry functionality
	let sidebarOpen = $state(false);
	let messagesContainer: HTMLDivElement;
	let editingTitle = $state<string | null>(null);
	let editTitleValue = $state('');
	let textareaEl: HTMLTextAreaElement;

	// Derived
	const currentConversation = $derived(conversations.find((c) => c.id === currentConversationId));
	const charactersRemaining = $derived(MAX_MESSAGE_LENGTH - inputMessage.length);
	const showCharacterCount = $derived(charactersRemaining < 500);

	// Load conversations on mount
	onMount(async () => {
		await loadConversations();
	});

	async function loadConversations() {
		try {
			const response = await fetch('/api/ai/conversations');
			if (response.ok) {
				const data = await response.json();
				conversations = data.conversations;
			}
		} catch {
			// Silently fail - sidebar will show empty state
		}
	}

	async function loadConversation(id: string) {
		isLoading = true;
		error = null;
		loadingConversationId = id;

		try {
			const response = await fetch(`/api/ai/conversations/${id}`);

			// Race condition check: only apply if this is still the requested conversation
			if (loadingConversationId !== id) return;

			if (response.ok) {
				const data = await response.json();
				messages = data.messages;
				currentConversationId = id;
				sidebarOpen = false;
				await tick();
				scrollToBottom();
			} else {
				error = 'Failed to load conversation';
			}
		} catch (e) {
			if (loadingConversationId === id) {
				error = 'Failed to load conversation';
			}
		} finally {
			if (loadingConversationId === id) {
				isLoading = false;
			}
		}
	}

	async function startNewConversation() {
		currentConversationId = null;
		messages = [];
		error = null;
		sidebarOpen = false;
		await tick();
		textareaEl?.focus();
	}

	async function sendMessage(retryContent?: string) {
		const messageContent = retryContent || inputMessage.trim();
		if (!messageContent || isSending) return;

		const userMessage = messageContent;
		inputMessage = '';
		error = null;
		failedMessage = null;
		isSending = true;

		// Reset textarea height
		if (textareaEl) {
			textareaEl.style.height = 'auto';
		}

		// Optimistic UI update
		const tempUserMsg: Message = {
			id: `temp-${Date.now()}`,
			role: 'user',
			content: userMessage,
			createdAt: new Date().toISOString()
		};
		messages = [...messages, tempUserMsg];
		await tick();
		scrollToBottom();

		try {
			const response = await fetch('/api/ai/chat', {
				method: 'POST',
				headers: { 'Content-Type': 'application/json' },
				body: JSON.stringify({
					message: userMessage,
					conversationId: currentConversationId
				})
			});

			const data = await response.json();

			if (!response.ok) {
				throw new Error(data.error || 'Failed to send message');
			}

			// Update conversation ID if this was a new conversation
			if (!currentConversationId) {
				currentConversationId = data.conversationId;
				// Add to conversation list
				conversations = [
					{
						id: data.conversationId,
						title: data.title,
						messageCount: 2,
						updatedAt: new Date().toISOString()
					},
					...conversations
				];
			} else {
				// Update existing conversation in list
				conversations = conversations.map((c) =>
					c.id === currentConversationId
						? { ...c, updatedAt: new Date().toISOString(), messageCount: c.messageCount + 2 }
						: c
				);
			}

			// Add assistant message
			const assistantMsg: Message = {
				id: `assistant-${Date.now()}`,
				role: 'assistant',
				content: data.message,
				createdAt: new Date().toISOString()
			};
			messages = [...messages, assistantMsg];
			await tick();
			scrollToBottom();
		} catch (e) {
			error = e instanceof Error ? e.message : 'Failed to send message';
			failedMessage = userMessage; // Store for retry
			// Remove optimistic message on error
			messages = messages.filter((m) => m.id !== tempUserMsg.id);
		} finally {
			isSending = false;
		}
	}

	function retryMessage() {
		if (failedMessage) {
			sendMessage(failedMessage);
		}
	}

	async function deleteConversation(id: string, e: Event) {
		e.stopPropagation();

		if (!confirm('Delete this conversation?')) return;

		try {
			const response = await fetch('/api/ai/conversations', {
				method: 'DELETE',
				headers: { 'Content-Type': 'application/json' },
				body: JSON.stringify({ conversationId: id })
			});

			if (response.ok) {
				conversations = conversations.filter((c) => c.id !== id);
				if (currentConversationId === id) {
					startNewConversation();
				}
			}
		} catch {
			// Silently fail - conversation remains in list
		}
	}

	function scrollToBottom() {
		if (messagesContainer) {
			messagesContainer.scrollTop = messagesContainer.scrollHeight;
		}
	}

	function handleKeydown(e: KeyboardEvent) {
		if (e.key === 'Enter' && !e.shiftKey) {
			e.preventDefault();
			sendMessage();
		}
	}

	function autoResizeTextarea() {
		if (textareaEl) {
			textareaEl.style.height = 'auto';
			textareaEl.style.height = Math.min(textareaEl.scrollHeight, 150) + 'px';
		}
	}

	function formatMessageTime(dateStr: string): string {
		const date = new Date(dateStr);
		return date.toLocaleTimeString('en-US', {
			hour: 'numeric',
			minute: '2-digit',
			hour12: true
		});
	}

	async function saveTitle(id: string) {
		if (!editTitleValue.trim()) {
			editingTitle = null;
			return;
		}

		try {
			const response = await fetch(`/api/ai/conversations/${id}`, {
				method: 'PATCH',
				headers: { 'Content-Type': 'application/json' },
				body: JSON.stringify({ title: editTitleValue.trim() })
			});

			if (response.ok) {
				conversations = conversations.map((c) =>
					c.id === id ? { ...c, title: editTitleValue.trim() } : c
				);
			}
		} catch {
			// Silently fail - title reverts on next load
		} finally {
			editingTitle = null;
		}
	}

	function startSuggestion(text: string) {
		inputMessage = text;
		sendMessage();
	}
</script>

<svelte:head>
	<title>Gusto - Your Cooking Assistant</title>
</svelte:head>

<div class="ai-page">
	<!-- Mobile sidebar toggle -->
	<button class="sidebar-toggle" onclick={() => (sidebarOpen = !sidebarOpen)}>
		{#if sidebarOpen}
			<X size={24} />
		{:else}
			<Menu size={24} />
		{/if}
	</button>

	<!-- Sidebar -->
	<aside class="sidebar" class:open={sidebarOpen}>
		<div class="sidebar-brand">
			<img src="/chefai.svg" alt="" class="brand-icon" />
			<div class="brand-text">
				<span class="brand-name">Gusto</span>
				<span class="brand-subtitle">Helping you with kitchen warfare</span>
			</div>
		</div>

		<div class="sidebar-header">
			<button class="new-chat-btn" onclick={startNewConversation}>
				<Plus size={18} />
				New chat
			</button>
		</div>

		<div class="conversations-list">
			{#each conversations as conv}
				<button
					class="conversation-item"
					class:active={conv.id === currentConversationId}
					onclick={() => loadConversation(conv.id)}
				>
					<MessageSquare size={16} />
					{#if editingTitle === conv.id}
						<input
							type="text"
							class="title-edit"
							bind:value={editTitleValue}
							onclick={(e) => e.stopPropagation()}
							onkeydown={(e) => e.key === 'Enter' && saveTitle(conv.id)}
							onblur={() => saveTitle(conv.id)}
						/>
						<button
							class="icon-btn"
							onclick={(e) => {
								e.stopPropagation();
								saveTitle(conv.id);
							}}
						>
							<Check size={14} />
						</button>
					{:else}
						<span class="conv-title">{conv.title || 'New conversation'}</span>
						<div class="conv-actions">
							<button
								class="icon-btn"
								onclick={(e) => {
									e.stopPropagation();
									editingTitle = conv.id;
									editTitleValue = conv.title || '';
								}}
							>
								<Edit2 size={14} />
							</button>
							<button class="icon-btn delete" onclick={(e) => deleteConversation(conv.id, e)}>
								<Trash2 size={14} />
							</button>
						</div>
					{/if}
				</button>
			{/each}

			{#if conversations.length === 0}
				<p class="no-conversations">No conversations yet</p>
			{/if}
		</div>
	</aside>

	<!-- Main chat area -->
	<main class="chat-main">
		{#if error}
			<div class="error-banner" role="alert">
				<AlertCircle size={16} />
				<span>{error}</span>
				<div class="error-actions">
					{#if failedMessage}
						<button onclick={retryMessage}>Retry</button>
					{/if}
					<button onclick={() => { error = null; failedMessage = null; }}>Dismiss</button>
				</div>
			</div>
		{/if}

		{#if messages.length === 0 && !isLoading}
			<!-- Welcome screen -->
			<div class="welcome">
				<div class="welcome-icon">
					<img src="/chefai.svg" alt="Gusto the Chef" class="chef-icon" />
				</div>
				<h1>Buongiorno! I'm Gusto!</h1>
				<p>
					Ask me about recipes, cooking techniques, ingredient substitutions, or troubleshooting
					your dishes. I'm here to help you become a better cook!
				</p>
				<div class="suggestions">
					<button
						onclick={() => startSuggestion('I have chicken, rice, and vegetables. What should I make?')}
					>
						What can I make with...
					</button>
					<button onclick={() => startSuggestion('Why did my sauce break and how do I fix it?')}>
						Fix a cooking mistake
					</button>
					<button onclick={() => startSuggestion('Teach me how to properly sear a steak')}>
						Learn a technique
					</button>
					<button onclick={() => startSuggestion("What's a good 30-minute weeknight dinner?")}>
						Quick meal ideas
					</button>
				</div>
			</div>
		{:else}
			<!-- Messages -->
			<div class="messages" bind:this={messagesContainer}>
				{#each messages as message}
					<div class="message {message.role}">
						<div class="message-avatar">
							{#if message.role === 'user'}
								<User size={18} />
							{:else}
								<img src="/chefai.svg" alt="Gusto" class="avatar-icon" />
							{/if}
						</div>
						<div class="message-body">
							<div class="message-content">
								{@html renderMarkdown(message.content)}
							</div>
							<span class="message-time">{formatMessageTime(message.createdAt)}</span>
						</div>
					</div>
				{/each}

				{#if isSending}
					<div class="message assistant">
						<div class="message-avatar">
							<img src="/chefai.svg" alt="Gusto" class="avatar-icon" />
						</div>
						<div class="message-body">
							<div class="message-content typing">
								<span class="typing-text">Gusto is thinking</span>
								<span class="typing-dots">
									<span></span><span></span><span></span>
								</span>
							</div>
						</div>
					</div>
				{/if}
			</div>
		{/if}

		<!-- Input area -->
		<div class="input-area">
			<div class="input-wrapper">
				<textarea
					bind:this={textareaEl}
					bind:value={inputMessage}
					onkeydown={handleKeydown}
					oninput={autoResizeTextarea}
					placeholder="Ask about cooking, recipes, techniques..."
					rows="1"
					disabled={isSending}
					maxlength={MAX_MESSAGE_LENGTH}
				></textarea>
				<button class="send-btn" onclick={() => sendMessage()} disabled={!inputMessage.trim() || isSending}>
					<Send size={18} />
				</button>
			</div>
			<div class="input-footer">
				<p class="input-hint">Press Enter to send, Shift+Enter for new line</p>
				{#if showCharacterCount}
					<span class="char-count" class:warning={charactersRemaining < 100}>
						{charactersRemaining} characters remaining
					</span>
				{/if}
			</div>
		</div>
	</main>
</div>

{#if sidebarOpen}
	<button class="sidebar-overlay" onclick={() => (sidebarOpen = false)}></button>
{/if}

<style>
	.ai-page {
		display: flex;
		flex: 1;
		overflow: hidden;
		position: relative;
	}

	/* Sidebar */
	.sidebar {
		position: fixed;
		top: 0;
		left: 0;
		bottom: 0;
		width: 280px;
		background: var(--color-surface);
		border-right: 1px solid var(--color-border);
		display: flex;
		flex-direction: column;
		z-index: 500;
	}

	.sidebar-brand {
		display: flex;
		align-items: center;
		gap: 0.75rem;
		padding: 1.25rem 1rem;
		border-bottom: 1px solid var(--color-border);
	}

	.brand-icon {
		width: 36px;
		height: 36px;
		object-fit: contain;
	}

	.brand-text {
		display: flex;
		flex-direction: column;
		gap: 0.125rem;
	}

	.brand-name {
		font-size: 1.125rem;
		font-weight: 600;
		color: var(--color-text);
		line-height: 1.2;
	}

	.brand-subtitle {
		font-size: 0.75rem;
		color: var(--color-text-muted);
		font-style: italic;
	}

	.sidebar-header {
		padding: 1rem;
	}

	.new-chat-btn {
		display: flex;
		align-items: center;
		justify-content: center;
		gap: 0.5rem;
		width: 100%;
		padding: 0.75rem 1rem;
		background: var(--color-text);
		border: none;
		border-radius: 8px;
		color: var(--color-bg);
		font-family: inherit;
		font-size: 0.9375rem;
		font-weight: 500;
		cursor: pointer;
		transition: opacity 0.15s;
	}

	.new-chat-btn:hover {
		opacity: 0.85;
	}

	.conversations-list {
		flex: 1;
		overflow-y: auto;
		padding: 0.5rem;
	}

	.conversation-item {
		display: flex;
		align-items: center;
		gap: 0.75rem;
		width: 100%;
		padding: 0.75rem;
		background: none;
		border: none;
		border-radius: 8px;
		color: var(--color-text);
		font-family: inherit;
		font-size: 0.875rem;
		text-align: left;
		cursor: pointer;
		transition: background-color 0.15s;
	}

	.conversation-item:hover {
		background: var(--color-bg);
	}

	.conversation-item.active {
		background: var(--color-bg);
	}

	.conv-title {
		flex: 1;
		overflow: hidden;
		text-overflow: ellipsis;
		white-space: nowrap;
	}

	.conv-actions {
		display: flex;
		gap: 0.25rem;
		opacity: 0;
		transition: opacity 0.15s;
	}

	.conversation-item:hover .conv-actions,
	.conversation-item:focus-within .conv-actions {
		opacity: 1;
	}

	.icon-btn {
		display: flex;
		align-items: center;
		justify-content: center;
		width: 28px;
		height: 28px;
		background: none;
		border: none;
		border-radius: 4px;
		color: var(--color-text-muted);
		cursor: pointer;
	}

	.icon-btn:hover {
		background: var(--color-border);
		color: var(--color-text);
	}

	.icon-btn.delete:hover {
		color: #dc2626;
	}

	.title-edit {
		flex: 1;
		padding: 0.25rem 0.5rem;
		background: var(--color-bg);
		border: 1px solid var(--color-border);
		border-radius: 4px;
		font-family: inherit;
		font-size: 0.875rem;
		color: var(--color-text);
	}

	.title-edit:focus {
		outline: none;
		border-color: var(--color-text);
	}

	.no-conversations {
		text-align: center;
		color: var(--color-text-muted);
		font-size: 0.875rem;
		padding: 2rem 1rem;
	}

	/* Main chat area */
	.chat-main {
		flex: 1;
		display: flex;
		flex-direction: column;
		min-width: 0;
		background: var(--color-bg);
		margin-left: 280px;
	}

	.error-banner {
		display: flex;
		align-items: center;
		gap: 0.5rem;
		padding: 0.75rem 1rem;
		background: #fef2f2;
		color: #b91c1c;
		font-size: 0.875rem;
	}

	:global(.dark) .error-banner {
		background: #450a0a;
		color: #fca5a5;
	}

	.error-actions {
		display: flex;
		gap: 0.5rem;
		margin-left: auto;
	}

	.error-banner button {
		padding: 0.25rem 0.75rem;
		background: none;
		border: 1px solid currentColor;
		border-radius: 4px;
		color: inherit;
		font-family: inherit;
		font-size: 0.8125rem;
		cursor: pointer;
		transition: background-color 0.15s;
	}

	.error-banner button:hover {
		background: rgba(0, 0, 0, 0.05);
	}

	:global(.dark) .error-banner button:hover {
		background: rgba(255, 255, 255, 0.1);
	}

	/* Welcome screen */
	.welcome {
		flex: 1;
		display: flex;
		flex-direction: column;
		align-items: center;
		justify-content: center;
		padding: 2rem;
		text-align: center;
		max-width: 600px;
		margin: 0 auto;
	}

	.welcome-icon {
		display: flex;
		align-items: center;
		justify-content: center;
		width: 120px;
		height: 120px;
		margin-bottom: 1.5rem;
	}

	.welcome-icon .chef-icon {
		width: 100%;
		height: 100%;
		object-fit: contain;
		filter: drop-shadow(0 4px 12px rgba(139, 90, 43, 0.3));
	}

	.welcome h1 {
		font-size: 1.75rem;
		font-weight: 600;
		margin: 0 0 0.75rem;
	}

	.welcome p {
		color: var(--color-text-muted);
		margin: 0 0 2.5rem;
		line-height: 1.6;
		font-size: 1rem;
	}

	.suggestions {
		display: grid;
		grid-template-columns: repeat(2, 1fr);
		gap: 0.75rem;
		width: 100%;
		max-width: 480px;
	}

	.suggestions button {
		padding: 1rem 1.25rem;
		background: var(--color-surface);
		border: 1px solid var(--color-border);
		border-radius: 12px;
		color: var(--color-text);
		font-family: inherit;
		font-size: 0.875rem;
		text-align: left;
		cursor: pointer;
		transition: all 0.2s ease;
		line-height: 1.4;
	}

	.suggestions button:hover {
		border-color: var(--color-text-muted);
		box-shadow: var(--shadow-elevation-low);
		transform: translateY(-1px);
	}

	/* Messages */
	.messages {
		flex: 1;
		overflow-y: auto;
		padding: 1.5rem;
	}

	.message {
		display: flex;
		gap: 1rem;
		margin-bottom: 1.5rem;
		max-width: 800px;
		margin-left: auto;
		margin-right: auto;
		animation: messageSlideIn 0.3s ease-out;
	}

	@keyframes messageSlideIn {
		from {
			opacity: 0;
			transform: translateY(8px);
		}
		to {
			opacity: 1;
			transform: translateY(0);
		}
	}

	.message-avatar {
		display: flex;
		align-items: center;
		justify-content: center;
		width: 40px;
		height: 40px;
		border-radius: 50%;
		flex-shrink: 0;
	}

	.message.user .message-avatar {
		background: var(--color-text);
		color: var(--color-bg);
	}

	.message.assistant .message-avatar {
		background: linear-gradient(135deg, #8b5a2b 0%, #a0522d 100%);
		padding: 4px;
	}

	.avatar-icon {
		width: 100%;
		height: 100%;
		object-fit: contain;
		border-radius: 50%;
	}

	.message-body {
		flex: 1;
		min-width: 0;
	}

	.message-content {
		font-size: 0.9375rem;
		line-height: 1.6;
	}

	.message.assistant .message-content {
		background: var(--color-surface);
		padding: 1rem 1.25rem;
		border-radius: 12px;
		border: 1px solid var(--color-border);
	}

	.message.user .message-content {
		padding-top: 0.5rem;
	}

	.message-time {
		display: block;
		font-size: 0.6875rem;
		color: var(--color-text-muted);
		opacity: 0;
		transition: opacity 0.2s;
		margin-top: 0.375rem;
	}

	.message:hover .message-time {
		opacity: 0.7;
	}

	/* Typing indicator */
	.message-content.typing {
		display: flex;
		align-items: center;
		gap: 0.5rem;
		color: var(--color-text-muted);
	}

	.typing-text {
		font-size: 0.875rem;
	}

	.typing-dots {
		display: inline-flex;
		gap: 4px;
	}

	.typing-dots span {
		width: 6px;
		height: 6px;
		background: var(--color-text-muted);
		border-radius: 50%;
		animation: typingBounce 1.4s infinite ease-in-out both;
	}

	.typing-dots span:nth-child(1) {
		animation-delay: -0.32s;
	}
	.typing-dots span:nth-child(2) {
		animation-delay: -0.16s;
	}
	.typing-dots span:nth-child(3) {
		animation-delay: 0s;
	}

	@keyframes typingBounce {
		0%,
		80%,
		100% {
			transform: scale(0.6);
			opacity: 0.5;
		}
		40% {
			transform: scale(1);
			opacity: 1;
		}
	}

	/* Markdown content styles */
	.message-content :global(p) {
		margin: 0 0 1em;
		line-height: 1.65;
	}

	.message-content :global(p:last-child) {
		margin-bottom: 0;
	}

	.message-content :global(.md-heading) {
		font-weight: 600;
		margin: 1.5em 0 0.75em;
		line-height: 1.3;
	}

	.message-content :global(.md-h1) {
		font-size: 1.375rem;
	}
	.message-content :global(.md-h2) {
		font-size: 1.25rem;
	}
	.message-content :global(.md-h3) {
		font-size: 1.125rem;
	}
	.message-content :global(.md-h4) {
		font-size: 1rem;
	}

	.message-content :global(.md-heading:first-child) {
		margin-top: 0;
	}

	.message-content :global(ul),
	.message-content :global(ol) {
		margin: 0 0 1em;
		padding-left: 1.5em;
	}

	.message-content :global(li) {
		margin-bottom: 0.5em;
		line-height: 1.5;
	}

	.message-content :global(li:last-child) {
		margin-bottom: 0;
	}

	.message-content :global(.code-block) {
		background: var(--color-bg);
		border: 1px solid var(--color-border);
		border-radius: 8px;
		padding: 1rem;
		margin: 1em 0;
		overflow-x: auto;
		font-family: 'SF Mono', 'Menlo', 'Monaco', 'Courier New', monospace;
		font-size: 0.875rem;
		line-height: 1.5;
	}

	.message-content :global(.inline-code) {
		background: var(--color-bg);
		border: 1px solid var(--color-border);
		border-radius: 4px;
		padding: 0.125em 0.375em;
		font-family: 'SF Mono', 'Menlo', 'Monaco', 'Courier New', monospace;
		font-size: 0.875em;
	}

	.message-content :global(.chef-tip) {
		border-left: 3px solid #8b5a2b;
		background: rgba(139, 90, 43, 0.05);
		padding: 0.875rem 1rem;
		margin: 1em 0;
		border-radius: 0 8px 8px 0;
		font-style: italic;
	}

	.message-content :global(.chef-tip p) {
		margin: 0;
	}

	:global(.dark) .message-content :global(.chef-tip) {
		background: rgba(139, 90, 43, 0.15);
	}

	.message-content :global(.md-link) {
		color: #8b5a2b;
		text-decoration: underline;
		text-underline-offset: 2px;
	}

	.message-content :global(.md-link:hover) {
		color: var(--color-text);
	}

	.message-content :global(table) {
		width: 100%;
		border-collapse: collapse;
		margin: 1em 0;
		font-size: 0.9375rem;
	}

	.message-content :global(th),
	.message-content :global(td) {
		border: 1px solid var(--color-border);
		padding: 0.5rem 0.75rem;
		text-align: left;
	}

	.message-content :global(th) {
		background: var(--color-bg);
		font-weight: 600;
	}

	.message-content :global(hr) {
		border: none;
		border-top: 1px solid var(--color-border);
		margin: 1.5em 0;
	}

	.message-content :global(strong) {
		font-weight: 600;
		color: var(--color-text);
	}

	.message-content :global(em) {
		font-style: italic;
	}

	/* Input area */
	.input-area {
		padding: 1.25rem 1.5rem 1.75rem;
		background: var(--color-bg);
	}

	.input-wrapper {
		display: flex;
		align-items: flex-end;
		gap: 0.75rem;
		max-width: 800px;
		margin: 0 auto;
		background: var(--color-surface);
		border: 1px solid var(--color-border);
		border-radius: 16px;
		padding: 0.5rem;
		transition:
			border-color 0.2s,
			box-shadow 0.2s;
	}

	.input-wrapper:focus-within {
		border-color: var(--color-text-muted);
		box-shadow: var(--shadow-elevation-low);
	}

	.input-wrapper textarea {
		flex: 1;
		padding: 0.75rem 1rem;
		background: transparent;
		border: none;
		font-family: inherit;
		font-size: 0.9375rem;
		color: var(--color-text);
		resize: none;
		min-height: 24px;
		max-height: 150px;
		line-height: 1.5;
	}

	.input-wrapper textarea:focus {
		outline: none;
	}

	.input-wrapper textarea::placeholder {
		color: var(--color-text-muted);
	}

	.input-wrapper textarea:disabled {
		opacity: 0.6;
	}

	.send-btn {
		display: flex;
		align-items: center;
		justify-content: center;
		width: 44px;
		height: 44px;
		background: var(--color-text);
		border: none;
		border-radius: 12px;
		color: var(--color-bg);
		cursor: pointer;
		transition: all 0.2s ease;
		flex-shrink: 0;
	}

	.send-btn:hover:not(:disabled) {
		transform: scale(1.05);
	}

	.send-btn:disabled {
		opacity: 0.3;
		cursor: not-allowed;
	}

	.input-footer {
		display: flex;
		justify-content: center;
		align-items: center;
		gap: 1rem;
		max-width: 800px;
		margin: 0.5rem auto 0;
	}

	.input-hint {
		display: none;
		font-size: 0.6875rem;
		color: var(--color-text-muted);
		margin: 0;
		opacity: 0.7;
	}

	.char-count {
		font-size: 0.6875rem;
		color: var(--color-text-muted);
		opacity: 0.7;
	}

	.char-count.warning {
		color: #dc2626;
		opacity: 1;
		font-weight: 500;
	}

	/* Mobile sidebar toggle */
	.sidebar-toggle {
		display: none;
		position: fixed;
		top: 90px;
		left: 1rem;
		z-index: 100;
		width: 44px;
		height: 44px;
		background: var(--color-surface);
		border: 1px solid var(--color-border);
		border-radius: 8px;
		color: var(--color-text);
		cursor: pointer;
		align-items: center;
		justify-content: center;
	}

	.sidebar-overlay {
		display: none;
		position: fixed;
		inset: 0;
		background: rgba(0, 0, 0, 0.5);
		z-index: 998;
		border: none;
		cursor: pointer;
	}

	/* Mobile styles */
	@media (max-width: 768px) {
		.sidebar {
			z-index: 999;
			transform: translateX(-100%);
			transition: transform 0.2s ease;
		}

		.sidebar.open {
			transform: translateX(0);
		}

		.chat-main {
			margin-left: 0;
		}

		.sidebar-toggle {
			display: flex;
		}

		.sidebar-overlay {
			display: block;
		}

		.messages {
			padding: 1rem;
		}

		.input-area {
			padding: 1rem;
		}

		.input-hint {
			display: block;
		}

		.message {
			gap: 0.75rem;
		}

		.message-avatar {
			width: 32px;
			height: 32px;
		}

		.message.assistant .message-avatar {
			padding: 3px;
		}

		.welcome {
			padding: 1.5rem;
		}

		.welcome-icon {
			width: 100px;
			height: 100px;
		}

		.suggestions {
			grid-template-columns: 1fr;
		}
	}
</style>
