import { json } from '@sveltejs/kit';
import type { RequestHandler } from './$types';
import { db } from '$lib/server/db';
import { votes, recipes } from '$lib/server/db/schema';
import { and, eq, sql } from 'drizzle-orm';
import { logger, getRequestId } from '$lib/server/logger';

// Helper to get current vote counts from recipe (O(1) instead of O(n))
async function getVoteCounts(recipeId: string) {
	const result = await db
		.select({
			upvotes: recipes.upvotes,
			downvotes: recipes.downvotes
		})
		.from(recipes)
		.where(eq(recipes.id, recipeId))
		.limit(1);

	return result[0] || { upvotes: 0, downvotes: 0 };
}

// Helper to atomically update vote counts using SQL increment/decrement
// This is O(1) instead of O(n) - no need to recalculate all votes
async function updateVoteCountsAtomic(
	tx: { update: typeof db.update },
	recipeId: string,
	upvoteDelta: number,
	downvoteDelta: number
) {
	await tx
		.update(recipes)
		.set({
			upvotes: sql`${recipes.upvotes} + ${upvoteDelta}`,
			downvotes: sql`${recipes.downvotes} + ${downvoteDelta}`,
			voteScore: sql`${recipes.voteScore} + ${upvoteDelta} - ${downvoteDelta}`
		})
		.where(eq(recipes.id, recipeId));
}

export const POST: RequestHandler = async ({ request, locals }) => {
	if (!locals.user) {
		return json({ error: 'Unauthorized' }, { status: 401 });
	}

	const userId = locals.user.id;
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
			.where(and(eq(votes.userId, userId), eq(votes.recipeId, recipeId)))
			.limit(1);

		if (existingVote.length > 0) {
			const oldValue = existingVote[0].value;
			if (oldValue === value) {
				// Same vote - remove it (toggle off) in a transaction
				// Decrement the appropriate counter
				const upvoteDelta = value > 0 ? -1 : 0;
				const downvoteDelta = value < 0 ? -1 : 0;

				await db.transaction(async (tx) => {
					await tx.delete(votes).where(eq(votes.id, existingVote[0].id));
					await updateVoteCountsAtomic(tx, recipeId, upvoteDelta, downvoteDelta);
				});

				const voteCounts = await getVoteCounts(recipeId);
				return json({ success: true, userVote: null, ...voteCounts });
			} else {
				// Different vote - update it in a transaction
				// Old vote was opposite, so we need to flip both counters
				const upvoteDelta = value > 0 ? 1 : -1; // +1 if new is upvote, -1 if removing upvote
				const downvoteDelta = value < 0 ? 1 : -1; // +1 if new is downvote, -1 if removing downvote

				await db.transaction(async (tx) => {
					await tx
						.update(votes)
						.set({ value, createdAt: new Date() })
						.where(eq(votes.id, existingVote[0].id));
					await updateVoteCountsAtomic(tx, recipeId, upvoteDelta, downvoteDelta);
				});

				const voteCounts = await getVoteCounts(recipeId);
				return json({ success: true, userVote: value, ...voteCounts });
			}
		} else {
			// Create new vote in a transaction
			// Increment the appropriate counter
			const upvoteDelta = value > 0 ? 1 : 0;
			const downvoteDelta = value < 0 ? 1 : 0;

			await db.transaction(async (tx) => {
				await tx.insert(votes).values({
					recipeId,
					userId,
					value
				});
				await updateVoteCountsAtomic(tx, recipeId, upvoteDelta, downvoteDelta);
			});

			const voteCounts = await getVoteCounts(recipeId);
			return json({ success: true, userVote: value, ...voteCounts });
		}
	} catch (error: unknown) {
		logger.error('Failed to process vote', error, { recipeId, userId });
		return json({ error: 'Failed to process vote', requestId: getRequestId() }, { status: 500 });
	}
};

export const DELETE: RequestHandler = async ({ request, locals }) => {
	if (!locals.user) {
		return json({ error: 'Unauthorized' }, { status: 401 });
	}

	const userId = locals.user.id;
	const { recipeId } = await request.json();

	if (!recipeId || typeof recipeId !== 'string') {
		return json({ error: 'Invalid recipe ID' }, { status: 400 });
	}

	try {
		// First check what the existing vote was so we know which counter to decrement
		const existingVote = await db
			.select({ value: votes.value })
			.from(votes)
			.where(and(eq(votes.userId, userId), eq(votes.recipeId, recipeId)))
			.limit(1);

		if (existingVote.length === 0) {
			// No vote to delete
			const voteCounts = await getVoteCounts(recipeId);
			return json({ success: true, userVote: null, ...voteCounts });
		}

		const oldValue = existingVote[0].value;
		const upvoteDelta = oldValue > 0 ? -1 : 0;
		const downvoteDelta = oldValue < 0 ? -1 : 0;

		// Delete vote and update counts atomically
		await db.transaction(async (tx) => {
			await tx
				.delete(votes)
				.where(and(eq(votes.userId, userId), eq(votes.recipeId, recipeId)));
			await updateVoteCountsAtomic(tx, recipeId, upvoteDelta, downvoteDelta);
		});

		const voteCounts = await getVoteCounts(recipeId);
		return json({ success: true, userVote: null, ...voteCounts });
	} catch (error: unknown) {
		logger.error('Failed to remove vote', error, { recipeId, userId });
		return json({ error: 'Failed to remove vote', requestId: getRequestId() }, { status: 500 });
	}
};
