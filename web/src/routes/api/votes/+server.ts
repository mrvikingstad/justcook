import { json } from '@sveltejs/kit';
import type { RequestHandler } from './$types';
import { db } from '$lib/server/db';
import { votes, recipes } from '$lib/server/db/schema';
import { and, eq, sql } from 'drizzle-orm';

export const POST: RequestHandler = async ({ request, locals }) => {
	if (!locals.user) {
		return json({ error: 'Unauthorized' }, { status: 401 });
	}

	const { recipeId, value } = await request.json();

	if (!recipeId || typeof recipeId !== 'string') {
		return json({ error: 'Invalid recipe ID' }, { status: 400 });
	}

	if (value !== 1 && value !== -1) {
		return json({ error: 'Invalid vote value. Must be 1 (upvote) or -1 (downvote)' }, { status: 400 });
	}

	try {
		// Check if recipe exists
		const recipe = await db
			.select({ id: recipes.id, authorId: recipes.authorId })
			.from(recipes)
			.where(eq(recipes.id, recipeId))
			.limit(1);

		if (recipe.length === 0) {
			return json({ error: 'Recipe not found' }, { status: 404 });
		}

		// Prevent voting on own recipe
		if (recipe[0].authorId === locals.user.id) {
			return json({ error: 'Cannot vote on your own recipe' }, { status: 403 });
		}

		// Check for existing vote
		const existingVote = await db
			.select({ id: votes.id, value: votes.value })
			.from(votes)
			.where(and(eq(votes.userId, locals.user.id), eq(votes.recipeId, recipeId)))
			.limit(1);

		if (existingVote.length > 0) {
			// Update existing vote
			if (existingVote[0].value === value) {
				// Same vote - remove it (toggle off)
				await db
					.delete(votes)
					.where(eq(votes.id, existingVote[0].id));

				// Get updated vote counts
				const voteCounts = await getVoteCounts(recipeId);
				return json({ success: true, userVote: null, ...voteCounts });
			} else {
				// Different vote - update it
				await db
					.update(votes)
					.set({ value, createdAt: new Date() })
					.where(eq(votes.id, existingVote[0].id));

				// Get updated vote counts
				const voteCounts = await getVoteCounts(recipeId);
				return json({ success: true, userVote: value, ...voteCounts });
			}
		} else {
			// Create new vote
			await db.insert(votes).values({
				recipeId,
				userId: locals.user.id,
				value
			});

			// Get updated vote counts
			const voteCounts = await getVoteCounts(recipeId);
			return json({ success: true, userVote: value, ...voteCounts });
		}
	} catch (error: unknown) {
		console.error('Failed to process vote:', error);
		return json({ error: 'Failed to process vote' }, { status: 500 });
	}
};

export const DELETE: RequestHandler = async ({ request, locals }) => {
	if (!locals.user) {
		return json({ error: 'Unauthorized' }, { status: 401 });
	}

	const { recipeId } = await request.json();

	if (!recipeId || typeof recipeId !== 'string') {
		return json({ error: 'Invalid recipe ID' }, { status: 400 });
	}

	try {
		await db
			.delete(votes)
			.where(and(eq(votes.userId, locals.user.id), eq(votes.recipeId, recipeId)));

		// Get updated vote counts
		const voteCounts = await getVoteCounts(recipeId);
		return json({ success: true, userVote: null, ...voteCounts });
	} catch (error: unknown) {
		console.error('Failed to remove vote:', error);
		return json({ error: 'Failed to remove vote' }, { status: 500 });
	}
};

async function getVoteCounts(recipeId: string) {
	const result = await db
		.select({
			upvotes: sql<number>`COALESCE(SUM(CASE WHEN ${votes.value} > 0 THEN 1 ELSE 0 END), 0)::int`,
			downvotes: sql<number>`COALESCE(SUM(CASE WHEN ${votes.value} < 0 THEN 1 ELSE 0 END), 0)::int`
		})
		.from(votes)
		.where(eq(votes.recipeId, recipeId));

	return result[0] || { upvotes: 0, downvotes: 0 };
}
