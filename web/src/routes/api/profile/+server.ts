import { json } from '@sveltejs/kit';
import type { RequestHandler } from './$types';
import { db } from '$lib/server/db';
import { user } from '$lib/server/db/schema';
import { eq } from 'drizzle-orm';
import { sanitizeText } from '$lib/server/validation/sanitize';
import { logger, getRequestId } from '$lib/server/logger';

function isValidUrl(urlString: string): boolean {
	try {
		const url = new URL(urlString);
		// Only allow http and https protocols to prevent XSS via javascript:, data:, etc.
		return url.protocol === 'http:' || url.protocol === 'https:';
	} catch {
		return false;
	}
}

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

	// Validate photoUrl if provided
	if (photoUrl && typeof photoUrl === 'string' && !isValidUrl(photoUrl)) {
		return json({ error: 'Invalid photo URL' }, { status: 400 });
	}

	// Validate bio length if provided
	if (bio && typeof bio === 'string' && bio.trim().length > 500) {
		return json({ error: 'Bio must be 500 characters or less' }, { status: 400 });
	}

	// Validate country length if provided
	if (country && typeof country === 'string' && country.trim().length > 100) {
		return json({ error: 'Country must be 100 characters or less' }, { status: 400 });
	}

	const userId = locals.user.id;

	try {
		// Sanitize inputs to prevent XSS
		const sanitizedFullName = sanitizeText(fullName);
		const sanitizedCountry = country ? sanitizeText(country) : null;
		const sanitizedBio = bio ? sanitizeText(bio) : null;

		// Update user profile fields
		await db
			.update(user)
			.set({
				fullName: sanitizedFullName,
				country: sanitizedCountry,
				bio: sanitizedBio,
				photoUrl: photoUrl || null,
				updatedAt: new Date()
			})
			.where(eq(user.id, userId));

		return json({ success: true });
	} catch (error) {
		logger.error('Failed to update profile', error, { userId });
		return json({ error: 'Failed to update profile', requestId: getRequestId() }, { status: 500 });
	}
};
