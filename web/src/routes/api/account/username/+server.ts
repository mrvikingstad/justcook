import { json, error } from '@sveltejs/kit';
import type { RequestHandler } from './$types';
import { db } from '$lib/server/db';
import { user } from '$lib/server/db/schema';
import { eq, and, ne, sql } from 'drizzle-orm';

const USERNAME_CHANGE_COOLDOWN_DAYS = 30;

export const PUT: RequestHandler = async ({ request, locals }) => {
	if (!locals.user) {
		throw error(401, 'Not authenticated');
	}

	const { username: newUsername } = await request.json();

	if (!newUsername || typeof newUsername !== 'string') {
		throw error(400, 'Username is required');
	}

	const trimmedUsername = newUsername.trim().toLowerCase();

	// Validate username format
	if (trimmedUsername.length < 3) {
		throw error(400, 'Username must be at least 3 characters');
	}

	if (trimmedUsername.length > 30) {
		throw error(400, 'Username must be at most 30 characters');
	}

	if (!/^[a-z0-9_]+$/.test(trimmedUsername)) {
		throw error(400, 'Username can only contain letters, numbers, and underscores');
	}

	// Reserved usernames
	const reserved = ['admin', 'administrator', 'mod', 'moderator', 'justcook', 'support', 'help', 'api', 'www'];
	if (reserved.includes(trimmedUsername)) {
		throw error(400, 'This username is reserved');
	}

	// Get current user data
	const [currentUser] = await db
		.select({
			username: user.username,
			usernameChangedAt: user.usernameChangedAt
		})
		.from(user)
		.where(eq(user.id, locals.user.id))
		.limit(1);

	if (!currentUser) {
		throw error(404, 'User not found');
	}

	// Check if username is the same
	if (currentUser.username === trimmedUsername) {
		return json({ success: true, username: trimmedUsername });
	}

	// Check cooldown period
	if (currentUser.usernameChangedAt) {
		const daysSinceChange = Math.floor(
			(Date.now() - currentUser.usernameChangedAt.getTime()) / (1000 * 60 * 60 * 24)
		);

		if (daysSinceChange < USERNAME_CHANGE_COOLDOWN_DAYS) {
			const daysRemaining = USERNAME_CHANGE_COOLDOWN_DAYS - daysSinceChange;
			throw error(400, `You can change your username again in ${daysRemaining} day${daysRemaining === 1 ? '' : 's'}`);
		}
	}

	// Check if username is already taken
	const [existingUser] = await db
		.select({ id: user.id })
		.from(user)
		.where(and(
			eq(sql`LOWER(${user.username})`, trimmedUsername),
			ne(user.id, locals.user.id)
		))
		.limit(1);

	if (existingUser) {
		throw error(400, 'Username is already taken');
	}

	// Update username
	await db
		.update(user)
		.set({
			username: trimmedUsername,
			displayUsername: newUsername.trim(), // Preserve original casing for display
			usernameChangedAt: new Date(),
			updatedAt: new Date()
		})
		.where(eq(user.id, locals.user.id));

	return json({
		success: true,
		username: trimmedUsername,
		displayUsername: newUsername.trim()
	});
};
