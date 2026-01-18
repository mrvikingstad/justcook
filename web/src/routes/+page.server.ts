import type { PageServerLoad } from './$types';
import { db } from '$lib/server/db';
import { user, follows, recipes, votes } from '$lib/server/db/schema';
import { eq, and, desc, sql, gte, notInArray } from 'drizzle-orm';
import { cacheGetOrSet, cacheKeys, CACHE_TTL } from '$lib/server/redis/cache';

interface TrendingRecipe {
	slug: string;
	title: string;
	description: string | null;
	image: string | null;
	authorName: string;
	authorUsername: string;
	cuisine: string | null;
	tag: string | null;
	difficulty: 'easy' | 'medium' | 'hard' | null;
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

interface HomePageData {
	trending: TrendingRecipe[];
	trendingChefs: TrendingChef[];
	discover: TrendingRecipe[];
}

export const load: PageServerLoad = async ({ url }) => {
	const lang = url.searchParams.get('lang') || 'en';

	// Use Redis cache for expensive home page queries
	const data = await cacheGetOrSet<HomePageData>(
		`homepage:${lang}`,
		'trending',
		async () => {
			// Use ISO strings for PostgreSQL compatibility (Date.toString() produces unparseable format)
			const now = new Date();
			const sevenDaysAgo = new Date(now.getTime() - 7 * 24 * 60 * 60 * 1000).toISOString();
			const thirtyDaysAgo = new Date(now.getTime() - 30 * 24 * 60 * 60 * 1000).toISOString();

			// Trending Recipes: Top voted published recipes in the last 30 days (with author info - fixes N+1)
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
					authorUsername: user.username,
					authorFullName: user.fullName,
					authorName: user.name,
					publishedAt: recipes.publishedAt,
					upvotes: sql<number>`COALESCE(SUM(CASE WHEN ${votes.value} > 0 THEN 1 ELSE 0 END), 0)::int`,
					downvotes: sql<number>`COALESCE(SUM(CASE WHEN ${votes.value} < 0 THEN 1 ELSE 0 END), 0)::int`
				})
				.from(recipes)
				.leftJoin(votes, eq(recipes.id, votes.recipeId))
				.leftJoin(user, eq(recipes.authorId, user.id))
				.where(
					and(
						eq(recipes.isPublished, true),
						eq(recipes.language, lang),
						gte(recipes.publishedAt, thirtyDaysAgo)
					)
				)
				.groupBy(recipes.id, user.id, user.username, user.fullName, user.name)
				.orderBy(desc(sql`SUM(CASE WHEN ${votes.value} > 0 THEN 1 ELSE 0 END) - SUM(CASE WHEN ${votes.value} < 0 THEN 1 ELSE 0 END)`))
				.limit(5);

			// Trending Chefs: Most new followers in the past 7 days (with user details - fixes N+1)
			const trendingChefs = await db
				.select({
					userId: follows.followingId,
					username: user.username,
					fullName: user.fullName,
					name: user.name,
					photoUrl: user.photoUrl,
					profileTier: user.profileTier,
					newFollowers: sql<number>`count(*)::int`
				})
				.from(follows)
				.innerJoin(user, eq(follows.followingId, user.id))
				.where(gte(follows.createdAt, sevenDaysAgo))
				.groupBy(follows.followingId, user.id, user.username, user.fullName, user.name, user.photoUrl, user.profileTier)
				.orderBy(desc(sql`count(*)`))
				.limit(5);

			// Discover Recipes: Random high-quality recipes not in trending (with author info - fixes N+1)
			// Criteria: >85% upvote ratio, minimum 20 votes, not in trending
			const trendingRecipeIds = trendingRecipes.map((r) => r.id);

			// Get recipes with vote stats and author info in a single query
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
					voteScore: recipes.voteScore,
					authorId: recipes.authorId,
					authorUsername: user.username,
					authorFullName: user.fullName,
					authorName: user.name,
					publishedAt: recipes.publishedAt,
					upvotes: sql<number>`COALESCE(SUM(CASE WHEN ${votes.value} > 0 THEN 1 ELSE 0 END), 0)::int`,
					downvotes: sql<number>`COALESCE(SUM(CASE WHEN ${votes.value} < 0 THEN 1 ELSE 0 END), 0)::int`,
					totalVotes: sql<number>`COALESCE(COUNT(${votes.id}), 0)::int`
				})
				.from(recipes)
				.leftJoin(votes, eq(recipes.id, votes.recipeId))
				.leftJoin(user, eq(recipes.authorId, user.id))
				.where(
					and(
						eq(recipes.isPublished, true),
						eq(recipes.language, lang),
						trendingRecipeIds.length > 0
							? notInArray(recipes.id, trendingRecipeIds)
							: undefined
					)
				)
				.groupBy(recipes.id, user.id, user.username, user.fullName, user.name)
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
				.slice(0, 5);

			return {
				trending: trendingRecipes.map((r) => ({
					slug: r.slug,
					title: r.title,
					description: r.description,
					image: r.photoUrl,
					authorName: r.authorFullName || r.authorName || 'Unknown',
					authorUsername: r.authorUsername || '',
					cuisine: r.cuisine,
					tag: r.tag,
					difficulty: r.difficulty as 'easy' | 'medium' | 'hard' | null,
					prepTimeMinutes: r.prepTimeMinutes,
					cookTimeMinutes: r.cookTimeMinutes,
					upvotes: r.upvotes,
					downvotes: r.downvotes,
					publishedAt: r.publishedAt
				})),
				trendingChefs: trendingChefs
					.filter((c) => c.username)
					.map((c) => ({
						username: c.username || '',
						fullName: c.fullName || c.name || 'Unknown',
						photoUrl: c.photoUrl,
						profileTier: c.profileTier || 'user',
						newFollowers: c.newFollowers
					})),
				discover: highQualityRecipes.map((r) => ({
					slug: r.slug,
					title: r.title,
					description: r.description,
					image: r.photoUrl,
					authorName: r.authorFullName || r.authorName || 'Unknown',
					authorUsername: r.authorUsername || '',
					cuisine: r.cuisine,
					tag: r.tag,
					difficulty: r.difficulty as 'easy' | 'medium' | 'hard' | null,
					prepTimeMinutes: r.prepTimeMinutes,
					cookTimeMinutes: r.cookTimeMinutes,
					upvotes: r.upvotes,
					downvotes: r.downvotes,
					publishedAt: r.publishedAt
				}))
			};
		}
	);

	return {
		...data,
		lang
	};
};
