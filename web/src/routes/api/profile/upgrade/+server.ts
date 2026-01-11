import { json } from '@sveltejs/kit';
import type { RequestHandler } from './$types';
import { db } from '$lib/server/db';
import { user, recipes, votes, TIER_REQUIREMENTS } from '$lib/server/db/schema';
import type { ProfileTier } from '$lib/server/db/schema/auth';
import { eq, and, sql } from 'drizzle-orm';

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

	// Calculate current stats
	const statsResult = await db
		.select({
			recipeCount: sql<number>`COUNT(DISTINCT ${recipes.id})::int`,
			totalUpvotes: sql<number>`COALESCE(SUM(CASE WHEN ${votes.value} > 0 THEN 1 ELSE 0 END), 0)::int`
		})
		.from(recipes)
		.leftJoin(votes, eq(recipes.id, votes.recipeId))
		.where(and(eq(recipes.authorId, userId), eq(recipes.isPublished, true)));

	const stats = statsResult[0] ?? { recipeCount: 0, totalUpvotes: 0 };

	// Validate requirements
	const requirements = TIER_REQUIREMENTS[targetTier as keyof typeof TIER_REQUIREMENTS];

	if (stats.recipeCount < requirements.recipes || stats.totalUpvotes < requirements.upvotes) {
		return json(
			{
				error: 'Requirements not met',
				required: requirements,
				current: stats
			},
			{ status: 400 }
		);
	}

	// Upgrade tier
	try {
		await db
			.update(user)
			.set({
				profileTier: targetTier,
				updatedAt: new Date()
			})
			.where(eq(user.id, userId));

		return json({ success: true, newTier: targetTier });
	} catch (error) {
		console.error('Failed to upgrade tier:', error);
		return json({ error: 'Failed to upgrade tier' }, { status: 500 });
	}
};
