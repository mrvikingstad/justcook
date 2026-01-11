import { error } from '@sveltejs/kit';
import type { PageServerLoad } from './$types';
import { db } from '$lib/server/db';
import { user } from '$lib/server/db/schema';
import { eq } from 'drizzle-orm';

export const load: PageServerLoad = async ({ params }) => {
	const { username } = params;

	// Find user by username
	const [foundUser] = await db
		.select()
		.from(user)
		.where(eq(user.username, username))
		.limit(1);

	if (!foundUser) {
		throw error(404, 'User not found');
	}

	const displayName = foundUser.fullName || foundUser.name || foundUser.username;
	const avatar = foundUser.photoUrl || foundUser.image || null;
	const profileTier = foundUser.profileTier || 'user';
	const joinedAt = foundUser.createdAt || new Date();
	const hasProfile = !!(foundUser.fullName && foundUser.country);

	return {
		user: {
			username: foundUser.username,
			displayName,
			avatar,
			profileTier,
			joinedAt,
			hasChefProfile: hasProfile
		}
	};
};
