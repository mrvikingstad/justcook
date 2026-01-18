import { redirect } from '@sveltejs/kit';
import type { PageServerLoad } from './$types';
import { db } from '$lib/server/db';
import { bookmarks, recipes, votes, user } from '$lib/server/db/schema';
import { eq, sql, desc } from 'drizzle-orm';

export const load: PageServerLoad = async ({ locals }) => {
	if (!locals.user) {
		throw redirect(302, '/auth/login');
	}

	// Get user's bookmarked recipes with vote counts
	const bookmarkedRecipes = await db
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
			authorId: recipes.authorId,
			publishedAt: recipes.publishedAt,
			bookmarkedAt: bookmarks.createdAt,
			upvotes: sql<number>`COALESCE(SUM(CASE WHEN ${votes.value} > 0 THEN 1 ELSE 0 END), 0)::int`,
			downvotes: sql<number>`COALESCE(SUM(CASE WHEN ${votes.value} < 0 THEN 1 ELSE 0 END), 0)::int`
		})
		.from(bookmarks)
		.innerJoin(recipes, eq(bookmarks.recipeId, recipes.id))
		.leftJoin(votes, eq(recipes.id, votes.recipeId))
		.where(eq(bookmarks.userId, locals.user.id))
		.groupBy(recipes.id, bookmarks.createdAt)
		.orderBy(desc(bookmarks.createdAt));

	// Get author info
	const authorIds = [...new Set(bookmarkedRecipes.map((r) => r.authorId))];
	const authors =
		authorIds.length > 0
			? await db
					.select({
						userId: user.id,
						username: user.username,
						fullName: user.fullName,
						name: user.name
					})
					.from(user)
					.where(sql`${user.id} IN ${authorIds}`)
			: [];

	const authorMap = new Map(authors.map((a) => [a.userId, a]));

	return {
		recipes: bookmarkedRecipes.map((r) => {
			const author = authorMap.get(r.authorId);
			return {
				slug: r.slug,
				title: r.title,
				description: r.description,
				image: r.photoUrl,
				authorName: author?.fullName || author?.name || 'Unknown',
				authorUsername: author?.username || '',
				cuisine: r.cuisine,
				tag: r.tag,
				difficulty: r.difficulty as 'easy' | 'medium' | 'hard' | null,
				prepTimeMinutes: r.prepTimeMinutes,
				cookTimeMinutes: r.cookTimeMinutes,
				upvotes: r.upvotes,
				downvotes: r.downvotes,
				publishedAt: r.publishedAt
			};
		})
	};
};
