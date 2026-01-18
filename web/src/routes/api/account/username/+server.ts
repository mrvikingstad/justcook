import { json, error } from '@sveltejs/kit';
import type { RequestHandler } from './$types';
import { db } from '$lib/server/db';
import { user } from '$lib/server/db/schema';
import { eq, and, ne, sql } from 'drizzle-orm';

const USERNAME_CHANGE_COOLDOWN_DAYS = 30;

// Reserved usernames that cannot be claimed
const RESERVED_USERNAMES = new Set([
	// System and admin
	'admin',
	'administrator',
	'mod',
	'moderator',
	'support',
	'help',
	'system',
	'root',
	'null',
	'undefined',
	// Brand names
	'justcook',
	'just_cook',
	'justcook_official',
	'official',
	// Common routes
	'api',
	'www',
	'app',
	'web',
	'login',
	'logout',
	'signin',
	'signout',
	'signup',
	'register',
	'account',
	'settings',
	'profile',
	'user',
	'users',
	'chef',
	'chefs',
	'recipe',
	'recipes',
	'home',
	'explore',
	'discover',
	'trending',
	'search',
	'about',
	'contact',
	'privacy',
	'terms',
	'blog',
	'news'
]);

/**
 * Validate username format and content
 */
function validateUsername(username: string): { valid: boolean; error?: string } {
	// Length checks
	if (username.length < 3) {
		return { valid: false, error: 'Username must be at least 3 characters' };
	}

	if (username.length > 30) {
		return { valid: false, error: 'Username must be at most 30 characters' };
	}

	// Character validation
	if (!/^[a-z0-9_]+$/.test(username)) {
		return { valid: false, error: 'Username can only contain letters, numbers, and underscores' };
	}

	// Cannot start or end with underscore
	if (username.startsWith('_') || username.endsWith('_')) {
		return { valid: false, error: 'Username cannot start or end with an underscore' };
	}

	// Cannot have consecutive underscores
	if (/__/.test(username)) {
		return { valid: false, error: 'Username cannot contain consecutive underscores' };
	}

	// Cannot be all numbers
	if (/^\d+$/.test(username)) {
		return { valid: false, error: 'Username cannot be only numbers' };
	}

	// Reserved username check
	if (RESERVED_USERNAMES.has(username)) {
		return { valid: false, error: 'This username is reserved' };
	}

	// Check for reserved prefixes/patterns
	if (username.startsWith('admin') || username.startsWith('mod_') || username.startsWith('official_')) {
		return { valid: false, error: 'This username is reserved' };
	}

	return { valid: true };
}

export const PUT: RequestHandler = async ({ request, locals }) => {
	if (!locals.user) {
		throw error(401, 'Not authenticated');
	}

	const { username: newUsername } = await request.json();

	if (!newUsername || typeof newUsername !== 'string') {
		throw error(400, 'Username is required');
	}

	const trimmedUsername = newUsername.trim().toLowerCase();

	// Validate username format and content
	const validation = validateUsername(trimmedUsername);
	if (!validation.valid) {
		throw error(400, validation.error!);
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
