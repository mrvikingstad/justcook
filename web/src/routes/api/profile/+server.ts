import { json } from '@sveltejs/kit';
import type { RequestHandler } from './$types';
import { db } from '$lib/server/db';
import { user } from '$lib/server/db/schema';
import { eq } from 'drizzle-orm';

export const POST: RequestHandler = async ({ request, locals }) => {
	if (!locals.user) {
		return json({ error: 'Unauthorized' }, { status: 401 });
	}

	const data = await request.json();
	const { fullName, country, bio, photoUrl } = data;

	// Validate required fields
	if (typeof fullName !== 'string' || fullName.trim().length === 0) {
		return json({ error: 'Full name is required' }, { status: 400 });
	}

	if (fullName.trim().length > 100) {
		return json({ error: 'Full name must be 100 characters or less' }, { status: 400 });
	}

	const userId = locals.user.id;

	try {
		// Update user profile fields
		await db
			.update(user)
			.set({
				fullName: fullName.trim(),
				country: country?.trim() || null,
				bio: bio?.trim() || null,
				photoUrl: photoUrl || null,
				updatedAt: new Date()
			})
			.where(eq(user.id, userId));

		return json({ success: true });
	} catch (error) {
		console.error('Failed to update profile:', error);
		return json({ error: 'Failed to update profile' }, { status: 500 });
	}
};
