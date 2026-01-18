import { json } from '@sveltejs/kit';
import type { RequestHandler } from './$types';
import { db } from '$lib/server/db';
import { recipes, ingredients, steps, user, votes, bookmarks } from '$lib/server/db/schema';
import { eq, sql, asc, and } from 'drizzle-orm';

export const GET: RequestHandler = async ({ params, locals }) => {
	const { slug } = params;
	const currentUser = locals.user;

	// Single query: Recipe + Author + User's vote + Bookmark status
	// Uses LEFT JOINs to combine what was previously 5 separate queries
	const mainResult = await db
		.select({
			// Recipe fields
			id: recipes.id,
			title: recipes.title,
			slug: recipes.slug,
			description: recipes.description,
			photoUrl: recipes.photoUrl,
			prepTimeMinutes: recipes.prepTimeMinutes,
			cookTimeMinutes: recipes.cookTimeMinutes,
			difficulty: recipes.difficulty,
			cuisine: recipes.cuisine,
			tag: recipes.tag,
			servings: recipes.servings,
			commentCount: recipes.commentCount,
			authorId: recipes.authorId,
			isPublished: recipes.isPublished,
			publishedAt: recipes.publishedAt,
			updatedAt: recipes.updatedAt,
			// Use denormalized vote counts (maintained by vote transactions)
			upvotes: recipes.upvotes,
			downvotes: recipes.downvotes,
			// Author fields (from LEFT JOIN)
			authorUsername: user.username,
			authorFullName: user.fullName,
			authorName: user.name,
			authorPhotoUrl: user.photoUrl,
			authorProfileTier: user.profileTier,
			// User's vote (null if not logged in or no vote)
			userVote: votes.value,
			// Bookmark ID (null if not bookmarked)
			bookmarkId: bookmarks.id
		})
		.from(recipes)
		.leftJoin(user, eq(user.id, recipes.authorId))
		.leftJoin(
			votes,
			and(
				eq(votes.recipeId, recipes.id),
				currentUser ? eq(votes.userId, currentUser.id) : sql`false`
			)
		)
		.leftJoin(
			bookmarks,
			and(
				eq(bookmarks.recipeId, recipes.id),
				currentUser ? eq(bookmarks.userId, currentUser.id) : sql`false`
			)
		)
		.where(eq(recipes.slug, slug))
		.limit(1);

	const result = mainResult[0];

	if (!result || !result.isPublished) {
		return json({ error: 'Recipe not found' }, { status: 404 });
	}

	// Query 2: Get ingredients ordered by sortOrder
	// Query 3: Get steps ordered by stepNumber
	// Run these in parallel since they're independent
	const [ingredientList, stepList] = await Promise.all([
		db
			.select({
				name: ingredients.name,
				amount: ingredients.amount,
				unit: ingredients.unit,
				notes: ingredients.notes
			})
			.from(ingredients)
			.where(eq(ingredients.recipeId, result.id))
			.orderBy(asc(ingredients.sortOrder)),
		db
			.select({
				stepNumber: steps.stepNumber,
				instruction: steps.instruction
			})
			.from(steps)
			.where(eq(steps.recipeId, result.id))
			.orderBy(asc(steps.stepNumber))
	]);

	// Check if edited (more than 1 minute after publish)
	const wasEdited =
		result.publishedAt &&
		result.updatedAt &&
		result.updatedAt.getTime() - result.publishedAt.getTime() > 60000;

	return json({
		recipe: {
			id: result.id,
			title: result.title,
			slug: result.slug,
			description: result.description,
			photoUrl: result.photoUrl,
			authorId: result.authorId,
			author: {
				id: result.authorId,
				username: result.authorUsername || '',
				fullName: result.authorFullName || result.authorName || 'Unknown',
				photoUrl: result.authorPhotoUrl,
				profileTier: result.authorProfileTier || 'user'
			},
			cuisine: result.cuisine,
			tag: result.tag,
			difficulty: result.difficulty,
			prepTimeMinutes: result.prepTimeMinutes,
			cookTimeMinutes: result.cookTimeMinutes,
			servings: result.servings,
			upvotes: result.upvotes,
			downvotes: result.downvotes,
			userVote: result.userVote,
			isBookmarked: result.bookmarkId !== null,
			commentCount: result.commentCount,
			publishedAt: result.publishedAt,
			updatedAt: wasEdited ? result.updatedAt : null,
			ingredients: ingredientList.map((i) => ({
				name: i.name,
				amount: i.amount ? parseFloat(i.amount) : null,
				unit: i.unit,
				notes: i.notes
			})),
			steps: stepList.map((s) => ({
				stepNumber: s.stepNumber,
				instruction: s.instruction
			}))
		},
		isOwn: currentUser?.id === result.authorId
	});
};
