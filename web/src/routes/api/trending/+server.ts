import { json } from '@sveltejs/kit';
import type { RequestHandler } from './$types';
import { db } from '$lib/server/db';
import { user, follows, recipes, votes } from '$lib/server/db/schema';
import { eq, and, desc, sql, gte } from 'drizzle-orm';
import { cacheGetOrSet, cacheKeys } from '$lib/server/redis/cache';

interface TrendingRecipe {
	slug: string;
	title: string;
	description: string | null;
	photoUrl: string | null;
	authorName: string;
	authorUsername: string;
	cuisine: string | null;
	tag: string | null;
	difficulty: string | null;
	prepTimeMinutes: number | null;
	cookTimeMinutes: number | null;
	upvotes: number;
	downvotes: number;
	publishedAt: Date | null;
}

interface TrendingChef {
	username: string;
	fullName: string;
	photoUrl: string | null;
	profileTier: string;
	newFollowers: number;
}

interface TrendingData {
	recipes: TrendingRecipe[];
	chefs: TrendingChef[];
}

export const GET: RequestHandler = async ({ url }) => {
	const lang = url.searchParams.get('lang') || 'en';

	// Use Redis cache with 1-minute TTL
	const data = await cacheGetOrSet<TrendingData>(
		cacheKeys.trending(lang),
		'trending',
		async () => {
			const now = new Date();
			const sevenDaysAgo = new Date(now.getTime() - 7 * 24 * 60 * 60 * 1000);
			const thirtyDaysAgo = new Date(now.getTime() - 30 * 24 * 60 * 60 * 1000);

			// Trending Recipes: Top voted published recipes in the last 30 days
			const trendingRecipes = await db
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
					downvotes: sql<number>`COALESCE(SUM(CASE WHEN ${votes.value} < 0 THEN 1 ELSE 0 END), 0)::int`
				})
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
				.orderBy(desc(sql`SUM(CASE WHEN ${votes.value} > 0 THEN 1 ELSE 0 END) - SUM(CASE WHEN ${votes.value} < 0 THEN 1 ELSE 0 END)`))
				.limit(10);

			// Get author info for trending recipes
			const trendingAuthorIds = [...new Set(trendingRecipes.map((r) => r.authorId))];
			const trendingAuthors = trendingAuthorIds.length > 0
				? await db
						.select({
							userId: user.id,
							username: user.username,
							fullName: user.fullName,
							name: user.name
						})
						.from(user)
						.where(sql`${user.id} IN ${trendingAuthorIds}`)
				: [];

			const trendingAuthorMap = new Map(trendingAuthors.map((a) => [a.userId, a]));

			// Trending Chefs: Most new followers in the past 7 days
			const trendingChefs = await db
				.select({
					userId: follows.followingId,
					newFollowers: sql<number>`count(*)::int`
				})
				.from(follows)
				.where(gte(follows.createdAt, sevenDaysAgo))
				.groupBy(follows.followingId)
				.orderBy(desc(sql`count(*)`))
				.limit(10);

			// Get chef details
			const trendingChefIds = trendingChefs.map((c) => c.userId);
			const chefDetails = trendingChefIds.length > 0
				? await db
						.select({
							userId: user.id,
							username: user.username,
							fullName: user.fullName,
							name: user.name,
							photoUrl: user.photoUrl,
							profileTier: user.profileTier
						})
						.from(user)
						.where(sql`${user.id} IN ${trendingChefIds}`)
				: [];

			const chefDetailsMap = new Map(chefDetails.map((c) => [c.userId, c]));

			return {
				recipes: trendingRecipes.map((r) => {
					const author = trendingAuthorMap.get(r.authorId);
					return {
						slug: r.slug,
						title: r.title,
						description: r.description,
						photoUrl: r.photoUrl,
						authorName: author?.fullName || author?.name || 'Unknown',
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
				}),
				chefs: trendingChefs
					.map((c) => {
						const chef = chefDetailsMap.get(c.userId);
						return {
							username: chef?.username || '',
							fullName: chef?.fullName || chef?.name || 'Unknown',
							photoUrl: chef?.photoUrl ?? null,
							profileTier: chef?.profileTier || 'user',
							newFollowers: c.newFollowers
						};
					})
					.filter((c) => c.username)
			};
		}
	);

	return json(data, {
		headers: {
			'Cache-Control': 'public, max-age=60, stale-while-revalidate=300'
		}
	});
};
