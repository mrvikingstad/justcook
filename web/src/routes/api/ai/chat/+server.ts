import { json } from '@sveltejs/kit';
import type { RequestHandler } from './$types';
import { db } from '$lib/server/db';
import { aiConversations, aiMessages, aiUsage } from '$lib/server/db/schema';
import { eq, desc, and, sql, gte } from 'drizzle-orm';
import { env } from '$env/dynamic/private';
import Anthropic from '@anthropic-ai/sdk';
import { JUSTCOOK_AI_SYSTEM_PROMPT, AI_CONFIG } from '$lib/server/ai/systemPrompt';
import { rateLimit } from '$lib/server/rateLimit';
import { logger, getRequestId } from '$lib/server/logger';

function getAnthropicClient(): Anthropic | null {
	if (!env.ANTHROPIC_API_KEY) {
		return null;
	}
	return new Anthropic({ apiKey: env.ANTHROPIC_API_KEY });
}

export const POST: RequestHandler = async ({ request, locals }) => {
	// Auth check
	if (!locals.user) {
		return json({ error: 'Unauthorized' }, { status: 401 });
	}

	const userId = locals.user.id;

	// Rate limiting
	const rateLimitResult = await rateLimit(`ai:${userId}`, 'strict');

	if (!rateLimitResult.allowed) {
		return json(
			{ error: 'Too many requests. Please wait before sending another message.' },
			{ status: 429, headers: { 'Retry-After': String(Math.ceil(rateLimitResult.resetIn / 1000)) } }
		);
	}

	// Check Anthropic client
	const anthropic = getAnthropicClient();
	if (!anthropic) {
		return json({ error: 'AI service is not configured' }, { status: 503 });
	}

	// Parse request
	let message: string;
	let conversationId: string | null;

	try {
		const body = await request.json();
		message = body.message;
		conversationId = body.conversationId || null;
	} catch {
		return json({ error: 'Invalid request body' }, { status: 400 });
	}

	if (!message || typeof message !== 'string' || message.trim().length === 0) {
		return json({ error: 'Message is required' }, { status: 400 });
	}

	if (message.length > 4000) {
		return json({ error: 'Message is too long (max 4000 characters)' }, { status: 400 });
	}

	const trimmedMessage = message.trim();

	try {
		// Check daily usage limit
		const today = new Date();
		today.setHours(0, 0, 0, 0);

		const [usageResult] = await db
			.select({ total: sql<number>`COALESCE(SUM(${aiUsage.tokensUsed}), 0)::int` })
			.from(aiUsage)
			.where(and(eq(aiUsage.userId, userId), gte(aiUsage.date, today)));

		const dailyUsage = usageResult?.total ?? 0;
		if (dailyUsage >= AI_CONFIG.dailyTokenLimit) {
			return json(
				{ error: 'Daily AI usage limit reached. Please try again tomorrow.' },
				{ status: 429 }
			);
		}

		// Get or create conversation
		let conversation;
		if (conversationId) {
			// Verify ownership
			[conversation] = await db
				.select()
				.from(aiConversations)
				.where(and(eq(aiConversations.id, conversationId), eq(aiConversations.userId, userId)))
				.limit(1);

			if (!conversation) {
				return json({ error: 'Conversation not found' }, { status: 404 });
			}
		} else {
			// Create new conversation
			[conversation] = await db.insert(aiConversations).values({ userId }).returning();
		}

		// Get conversation history for context
		const history = await db
			.select({ role: aiMessages.role, content: aiMessages.content })
			.from(aiMessages)
			.where(eq(aiMessages.conversationId, conversation.id))
			.orderBy(desc(aiMessages.createdAt))
			.limit(AI_CONFIG.maxContextMessages);

		// Reverse to get chronological order
		const contextMessages = history.reverse();

		// Build messages for API
		const messages: Anthropic.MessageParam[] = [
			...contextMessages.map((m) => ({
				role: m.role as 'user' | 'assistant',
				content: m.content
			})),
			{ role: 'user' as const, content: trimmedMessage }
		];

		// Call Claude API
		const response = await anthropic.messages.create({
			model: AI_CONFIG.model,
			max_tokens: AI_CONFIG.maxTokens,
			temperature: AI_CONFIG.temperature,
			system: JUSTCOOK_AI_SYSTEM_PROMPT,
			messages
		});

		// Extract response text
		const assistantMessage = response.content
			.filter((block): block is Anthropic.TextBlock => block.type === 'text')
			.map((block) => block.text)
			.join('');

		// Calculate tokens used
		const tokensUsed = (response.usage?.input_tokens ?? 0) + (response.usage?.output_tokens ?? 0);

		// Store messages and update usage in a transaction
		await db.transaction(async (tx) => {
			// Store user message
			await tx.insert(aiMessages).values({
				conversationId: conversation.id,
				role: 'user',
				content: trimmedMessage
			});

			// Store assistant message
			await tx.insert(aiMessages).values({
				conversationId: conversation.id,
				role: 'assistant',
				content: assistantMessage,
				tokenCount: response.usage?.output_tokens
			});

			// Update conversation
			await tx
				.update(aiConversations)
				.set({
					messageCount: sql`${aiConversations.messageCount} + 2`,
					updatedAt: new Date()
				})
				.where(eq(aiConversations.id, conversation.id));

			// Track usage
			await tx.insert(aiUsage).values({
				userId,
				tokensUsed,
				date: today
			});
		});

		// Auto-generate title for new conversations
		let title = conversation.title;
		if (!title && conversation.messageCount === 0) {
			title =
				trimmedMessage.length > 50 ? trimmedMessage.substring(0, 47) + '...' : trimmedMessage;

			await db
				.update(aiConversations)
				.set({ title })
				.where(eq(aiConversations.id, conversation.id));
		}

		return json({
			message: assistantMessage,
			conversationId: conversation.id,
			title
		});
	} catch (error) {
		logger.error('AI chat error', error, { userId, conversationId });

		if (error instanceof Anthropic.APIError) {
			if (error.status === 429) {
				return json(
					{ error: 'AI service is busy. Please try again in a moment.' },
					{ status: 503 }
				);
			}
		}

		return json({ error: 'Failed to get response from AI', requestId: getRequestId() }, { status: 500 });
	}
};
