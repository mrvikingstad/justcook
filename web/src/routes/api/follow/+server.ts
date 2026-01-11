import { json } from '@sveltejs/kit';
import type { RequestHandler } from './$types';
import { db } from '$lib/server/db';
import { follows } from '$lib/server/db/schema';
import { and, eq } from 'drizzle-orm';

export const POST: RequestHandler = async ({ request, locals }) => {
	if (!locals.user) {
		return json({ error: 'Unauthorized' }, { status: 401 });
	}

	const { userId } = await request.json();

	if (!userId || typeof userId !== 'string') {
		return json({ error: 'Invalid user ID' }, { status: 400 });
	}

	if (userId === locals.user.id) {
		return json({ error: 'Cannot follow yourself' }, { status: 400 });
	}

	try {
		await db.insert(follows).values({
			followerId: locals.user.id,
			followingId: userId
		});

		return json({ success: true, following: true });
	} catch (error: unknown) {
		// Handle unique constraint violation (already following)
		if (error && typeof error === 'object' && 'code' in error && error.code === '23505') {
			return json({ error: 'Already following this user' }, { status: 409 });
		}
		throw error;
	}
};

export const DELETE: RequestHandler = async ({ request, locals }) => {
	if (!locals.user) {
		return json({ error: 'Unauthorized' }, { status: 401 });
	}

	const { userId } = await request.json();

	if (!userId || typeof userId !== 'string') {
		return json({ error: 'Invalid user ID' }, { status: 400 });
	}

	await db
		.delete(follows)
		.where(and(eq(follows.followerId, locals.user.id), eq(follows.followingId, userId)));

	return json({ success: true, following: false });
};
