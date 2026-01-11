import { json } from '@sveltejs/kit';
import type { RequestHandler } from './$types';
import { db } from '$lib/server/db';
import { user, recipes, votes } from '$lib/server/db/schema';
import { eq, and, sql, notInArray, gte } from 'drizzle-orm';

export const GET: RequestHandler = async ({ url }) => {
	const lang = url.searchParams.get('lang') || 'en';
	const now = new Date();
	const thirtyDaysAgo = new Date(now.getTime() - 30 * 24 * 60 * 60 * 1000);

	// First get trending recipe IDs to exclude them
	const trendingRecipes = await db
		.select({ id: recipes.id })
		.from(recipes)
		.leftJoin(votes, eq(recipes.id, votes.recipeId))
		.where(
			and(
				eq(recipes.isPublished, true),
				eq(recipes.language, lang),
				gte(recipes.publishedAt, thirtyDaysAgo)
			)
		)
		.groupBy(recipes.id)
		.orderBy(sql`SUM(CASE WHEN ${votes.value} > 0 THEN 1 ELSE 0 END) - SUM(CASE WHEN ${votes.value} < 0 THEN 1 ELSE 0 END) DESC`)
		.limit(10);

	const trendingRecipeIds = trendingRecipes.map((r) => r.id);

	// Get recipes with vote stats for discover
	const discoverCandidates = await db
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
			upvotes: sql<number>`COALESCE(SUM(CASE WHEN ${votes.value} > 0 THEN 1 ELSE 0 END), 0)::int`,
			downvotes: sql<number>`COALESCE(SUM(CASE WHEN ${votes.value} < 0 THEN 1 ELSE 0 END), 0)::int`,
			totalVotes: sql<number>`COALESCE(COUNT(${votes.id}), 0)::int`
		})
		.from(recipes)
		.leftJoin(votes, eq(recipes.id, votes.recipeId))
		.where(
			and(
				eq(recipes.isPublished, true),
				eq(recipes.language, lang),
				trendingRecipeIds.length > 0
					? notInArray(recipes.id, trendingRecipeIds)
					: undefined
			)
		)
		.groupBy(recipes.id)
		.having(sql`COUNT(${votes.id}) >= 20`);

	// Filter by upvote ratio and randomize
	const highQualityRecipes = discoverCandidates
		.filter((r) => {
			const total = r.upvotes + r.downvotes;
			if (total < 20) return false;
			const ratio = r.upvotes / total;
			return ratio >= 0.85;
		})
		.sort(() => Math.random() - 0.5)
		.slice(0, 10);

	// Get author info
	const authorIds = [...new Set(highQualityRecipes.map((r) => r.authorId))];
	const authors = authorIds.length > 0
		? await db
				.select({
					userId: user.id,
					username: user.username,
					fullName: user.fullName
				})
				.from(user)
				.where(sql`${user.id} IN ${authorIds}`)
		: [];

	const authorMap = new Map(authors.map((a) => [a.userId, a]));

	return json({
		recipes: highQualityRecipes.map((r) => {
			const author = authorMap.get(r.authorId);
			return {
				slug: r.slug,
				title: r.title,
				description: r.description,
				photoUrl: r.photoUrl,
				authorName: author?.fullName || 'Unknown',
				authorUsername: author?.username || '',
				cuisine: r.cuisine,
				tag: r.tag,
				difficulty: r.difficulty,
				prepTimeMinutes: r.prepTimeMinutes,
				cookTimeMinutes: r.cookTimeMinutes,
				upvotes: r.upvotes,
				downvotes: r.downvotes,
				publishedAt: r.publishedAt
			};
		})
	});
};
