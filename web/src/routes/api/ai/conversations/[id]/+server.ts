import { json } from '@sveltejs/kit';
import type { RequestHandler } from './$types';
import { db } from '$lib/server/db';
import { aiConversations, aiMessages } from '$lib/server/db/schema';
import { eq, and, asc } from 'drizzle-orm';

// GET - Get a specific conversation with messages
export const GET: RequestHandler = async ({ params, locals }) => {
	if (!locals.user) {
		return json({ error: 'Unauthorized' }, { status: 401 });
	}

	const conversationId = params.id;

	// Get conversation
	const [conversation] = await db
		.select()
		.from(aiConversations)
		.where(and(eq(aiConversations.id, conversationId), eq(aiConversations.userId, locals.user.id)))
		.limit(1);

	if (!conversation) {
		return json({ error: 'Conversation not found' }, { status: 404 });
	}

	// Get messages
	const messages = await db
		.select({
			id: aiMessages.id,
			role: aiMessages.role,
			content: aiMessages.content,
			createdAt: aiMessages.createdAt
		})
		.from(aiMessages)
		.where(eq(aiMessages.conversationId, conversationId))
		.orderBy(asc(aiMessages.createdAt));

	return json({ conversation, messages });
};

// PATCH - Update conversation (e.g., rename)
export const PATCH: RequestHandler = async ({ params, request, locals }) => {
	if (!locals.user) {
		return json({ error: 'Unauthorized' }, { status: 401 });
	}

	let title: string;

	try {
		const body = await request.json();
		title = body.title;
	} catch {
		return json({ error: 'Invalid request body' }, { status: 400 });
	}

	if (!title || typeof title !== 'string') {
		return json({ error: 'Title is required' }, { status: 400 });
	}

	const result = await db
		.update(aiConversations)
		.set({ title: title.trim().substring(0, 100) })
		.where(and(eq(aiConversations.id, params.id), eq(aiConversations.userId, locals.user.id)))
		.returning({ id: aiConversations.id });

	if (result.length === 0) {
		return json({ error: 'Conversation not found' }, { status: 404 });
	}

	return json({ success: true });
};
