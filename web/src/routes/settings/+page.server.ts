import { redirect } from '@sveltejs/kit';
import type { PageServerLoad } from './$types';
import { db } from '$lib/server/db';
import { user, account, recipes, votes, TIER_REQUIREMENTS } from '$lib/server/db/schema';
import type { ProfileTier } from '$lib/server/db/schema/auth';
import { eq, and, sql } from 'drizzle-orm';

export const load: PageServerLoad = async ({ locals }) => {
	if (!locals.user) {
		throw redirect(302, '/auth/login?redirect=/settings');
	}

	const userId = locals.user.id;

	// Get user's account details including profile fields
	const [userDetails] = await db
		.select({
			username: user.username,
			displayUsername: user.displayUsername,
			email: user.email,
			usernameChangedAt: user.usernameChangedAt,
			fullName: user.fullName,
			country: user.country,
			bio: user.bio,
			photoUrl: user.photoUrl,
			profileTier: user.profileTier
		})
		.from(user)
		.where(eq(user.id, userId))
		.limit(1);

	// Check if user has a credential (password) account
	const [credentialAccount] = await db
		.select({ id: account.id })
		.from(account)
		.where(and(
			eq(account.userId, userId),
			eq(account.providerId, 'credential')
		))
		.limit(1);

	const hasPassword = !!credentialAccount;

	// Calculate days until username can be changed
	let usernameChangeCooldown = 0;
	if (userDetails?.usernameChangedAt) {
		const daysSinceChange = Math.floor(
			(Date.now() - userDetails.usernameChangedAt.getTime()) / (1000 * 60 * 60 * 24)
		);
		usernameChangeCooldown = Math.max(0, 30 - daysSinceChange);
	}

	// Calculate stats for tier progress
	const statsResult = await db
		.select({
			recipeCount: sql<number>`COUNT(DISTINCT ${recipes.id})::int`,
			totalUpvotes: sql<number>`COALESCE(SUM(CASE WHEN ${votes.value} > 0 THEN 1 ELSE 0 END), 0)::int`
		})
		.from(recipes)
		.leftJoin(votes, eq(recipes.id, votes.recipeId))
		.where(and(eq(recipes.authorId, userId), eq(recipes.isPublished, true)));

	const stats = statsResult[0] ?? { recipeCount: 0, totalUpvotes: 0 };

	// Determine eligibility
	const currentTier = (userDetails?.profileTier ?? 'user') as ProfileTier;
	const canClaimAuthor =
		currentTier === 'user' &&
		stats.recipeCount >= TIER_REQUIREMENTS.author.recipes &&
		stats.totalUpvotes >= TIER_REQUIREMENTS.author.upvotes;
	const canClaimChef =
		currentTier === 'author' &&
		stats.recipeCount >= TIER_REQUIREMENTS.chef.recipes &&
		stats.totalUpvotes >= TIER_REQUIREMENTS.chef.upvotes;

	const hasProfile = !!(userDetails?.fullName);

	return {
		user: locals.user,
		account: {
			username: userDetails?.username ?? null,
			displayUsername: userDetails?.displayUsername ?? userDetails?.username ?? null,
			email: userDetails?.email ?? '',
			hasPassword,
			usernameChangeCooldown
		},
		profile: hasProfile
			? {
					fullName: userDetails.fullName!,
					country: userDetails.country,
					bio: userDetails.bio,
					photoUrl: userDetails.photoUrl,
					profileTier: userDetails.profileTier as ProfileTier
				}
			: null,
		stats,
		currentTier,
		canClaimAuthor,
		canClaimChef,
		tierRequirements: TIER_REQUIREMENTS
	};
};
