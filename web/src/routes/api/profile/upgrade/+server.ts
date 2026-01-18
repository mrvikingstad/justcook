import { json } from '@sveltejs/kit';
import type { RequestHandler } from './$types';
import { db } from '$lib/server/db';
import { user, recipes, votes, TIER_REQUIREMENTS } from '$lib/server/db/schema';
import type { ProfileTier } from '$lib/server/db/schema/auth';
import { eq, and, sql } from 'drizzle-orm';
import { logger, getRequestId } from '$lib/server/logger';

export const POST: RequestHandler = async ({ request, locals }) => {
	if (!locals.user) {
		return json({ error: 'Unauthorized' }, { status: 401 });
	}

	const data = await request.json();
	const { targetTier } = data;

	// Validate target tier
	if (targetTier !== 'author' && targetTier !== 'chef') {
		return json({ error: 'Invalid tier' }, { status: 400 });
	}

	const userId = locals.user.id;

	// Get current user
	const [currentUser] = await db
		.select({ profileTier: user.profileTier })
		.from(user)
		.where(eq(user.id, userId))
		.limit(1);

	if (!currentUser) {
		return json({ error: 'User not found' }, { status: 404 });
	}

	// Validate tier progression
	const currentTier = currentUser.profileTier as ProfileTier;

	if (targetTier === 'author' && currentTier !== 'user') {
		return json({ error: 'Already at or above author tier' }, { status: 400 });
	}

	if (targetTier === 'chef' && currentTier !== 'author') {
		return json({ error: 'Must be at author tier to upgrade to chef' }, { status: 400 });
	}

	// Validate requirements and upgrade tier in a transaction to prevent race conditions
	// (e.g., user deleting recipes between requirements check and tier update)
	const requirements = TIER_REQUIREMENTS[targetTier as keyof typeof TIER_REQUIREMENTS];

	try {
		const result = await db.transaction(async (tx) => {
			// Calculate current stats inside transaction for consistency
			const statsResult = await tx
				.select({
					recipeCount: sql<number>`COUNT(DISTINCT ${recipes.id})::int`,
					totalUpvotes: sql<number>`COALESCE(SUM(CASE WHEN ${votes.value} > 0 THEN 1 ELSE 0 END), 0)::int`
				})
				.from(recipes)
				.leftJoin(votes, eq(recipes.id, votes.recipeId))
				.where(and(eq(recipes.authorId, userId), eq(recipes.isPublished, true)));

			const stats = statsResult[0] ?? { recipeCount: 0, totalUpvotes: 0 };

			// Validate requirements
			if (stats.recipeCount < requirements.recipes || stats.totalUpvotes < requirements.upvotes) {
				return {
					success: false,
					error: 'Requirements not met',
					required: requirements,
					current: stats
				};
			}

			// Upgrade tier
			await tx
				.update(user)
				.set({
					profileTier: targetTier,
					updatedAt: new Date()
				})
				.where(eq(user.id, userId));

			return { success: true, newTier: targetTier };
		});

		if (!result.success) {
			return json(
				{
					error: result.error,
					required: result.required,
					current: result.current
				},
				{ status: 400 }
			);
		}

		return json({ success: true, newTier: result.newTier });
	} catch (error) {
		logger.error('Failed to upgrade tier', error, { userId, targetTier });
		return json({ error: 'Failed to upgrade tier', requestId: getRequestId() }, { status: 500 });
	}
};
