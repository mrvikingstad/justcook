import { redirect } from '@sveltejs/kit';
import type { PageServerLoad } from './$types';
import { db } from '$lib/server/db';
import { user, follows, recipes, votes } from '$lib/server/db/schema';
import { eq, and, desc, inArray, sql } from 'drizzle-orm';

export const load: PageServerLoad = async ({ locals }) => {
	if (!locals.user) {
		throw redirect(302, '/auth/login');
	}

	// Get list of users that the current user follows
	const followingRecords = await db
		.select({ followingId: follows.followingId })
		.from(follows)
		.where(eq(follows.followerId, locals.user.id));

	const followingIds = followingRecords.map((f) => f.followingId);

	if (followingIds.length === 0) {
		return {
			timelineItems: [],
			isEmpty: true
		};
	}

	// Get published recipes from followed users with vote counts
	const timelineRecipes = await db
		.select({
			id: recipes.id,
			slug: recipes.slug,
			title: recipes.title,
			description: recipes.description,
			photoUrl: recipes.photoUrl,
			prepTimeMinutes: recipes.prepTimeMinutes,
			cookTimeMinutes: recipes.cookTimeMinutes,
			difficulty: recipes.difficulty,
			cuisine: recipes.cuisine,
			tag: recipes.tag,
			publishedAt: recipes.publishedAt,
			authorId: recipes.authorId,
			upvotes: sql<number>`COALESCE(SUM(CASE WHEN ${votes.value} > 0 THEN 1 ELSE 0 END), 0)::int`,
			downvotes: sql<number>`COALESCE(SUM(CASE WHEN ${votes.value} < 0 THEN 1 ELSE 0 END), 0)::int`
		})
		.from(recipes)
		.leftJoin(votes, eq(recipes.id, votes.recipeId))
		.where(and(inArray(recipes.authorId, followingIds), eq(recipes.isPublished, true)))
		.groupBy(recipes.id)
		.orderBy(desc(recipes.publishedAt))
		.limit(50);

	// Get author info for each recipe
	const authorIds = [...new Set(timelineRecipes.map((r) => r.authorId))];
	const authors = await db
		.select({
			userId: user.id,
			username: user.username,
			fullName: user.fullName,
			photoUrl: user.photoUrl
		})
		.from(user)
		.where(inArray(user.id, authorIds));

	const authorMap = new Map(authors.map((a) => [a.userId, a]));

	const timelineItems = timelineRecipes.map((recipe) => {
		const author = authorMap.get(recipe.authorId);
		return {
			recipe: {
				slug: recipe.slug,
				title: recipe.title,
				description: recipe.description,
				image: recipe.photoUrl,
				authorName: author?.fullName || 'Unknown Chef',
				authorUsername: author?.username || '',
				cuisine: recipe.cuisine,
				tag: recipe.tag,
				difficulty: recipe.difficulty as 'easy' | 'medium' | 'hard' | null,
				prepTimeMinutes: recipe.prepTimeMinutes,
				cookTimeMinutes: recipe.cookTimeMinutes,
				upvotes: recipe.upvotes,
				downvotes: recipe.downvotes,
				publishedAt: recipe.publishedAt
			},
			author: {
				username: author?.username || '',
				fullName: author?.fullName || 'Unknown Chef',
				photoUrl: author?.photoUrl
			},
			publishedAt: recipe.publishedAt
		};
	});

	return {
		timelineItems,
		isEmpty: false
	};
};
