import { json } from '@sveltejs/kit';
import type { RequestHandler } from './$types';
import { db } from '$lib/server/db';
import { aiConversations } from '$lib/server/db/schema';
import { eq, desc, and } from 'drizzle-orm';

// GET - List user's conversations
export const GET: RequestHandler = async ({ url, locals }) => {
	if (!locals.user) {
		return json({ error: 'Unauthorized' }, { status: 401 });
	}

	const limit = Math.min(50, parseInt(url.searchParams.get('limit') || '20', 10));
	const offset = parseInt(url.searchParams.get('offset') || '0', 10);

	const conversations = await db
		.select({
			id: aiConversations.id,
			title: aiConversations.title,
			messageCount: aiConversations.messageCount,
			updatedAt: aiConversations.updatedAt,
			createdAt: aiConversations.createdAt
		})
		.from(aiConversations)
		.where(and(eq(aiConversations.userId, locals.user.id), eq(aiConversations.isArchived, false)))
		.orderBy(desc(aiConversations.updatedAt))
		.limit(limit)
		.offset(offset);

	return json({ conversations });
};

// POST - Create new conversation
export const POST: RequestHandler = async ({ locals }) => {
	if (!locals.user) {
		return json({ error: 'Unauthorized' }, { status: 401 });
	}

	const [conversation] = await db
		.insert(aiConversations)
		.values({ userId: locals.user.id })
		.returning();

	return json({ conversation });
};

// DELETE - Delete a conversation
export const DELETE: RequestHandler = async ({ request, locals }) => {
	if (!locals.user) {
		return json({ error: 'Unauthorized' }, { status: 401 });
	}

	let conversationId: string;

	try {
		const body = await request.json();
		conversationId = body.conversationId;
	} catch {
		return json({ error: 'Invalid request body' }, { status: 400 });
	}

	if (!conversationId) {
		return json({ error: 'Conversation ID is required' }, { status: 400 });
	}

	// Verify ownership and delete
	const result = await db
		.delete(aiConversations)
		.where(and(eq(aiConversations.id, conversationId), eq(aiConversations.userId, locals.user.id)))
		.returning({ id: aiConversations.id });

	if (result.length === 0) {
		return json({ error: 'Conversation not found' }, { status: 404 });
	}

	return json({ success: true });
};
