import { json } from '@sveltejs/kit';
import type { RequestHandler } from './$types';
import { db } from '$lib/server/db';
import { user, recipes, votes } from '$lib/server/db/schema';
import { eq, and, sql, notInArray, gte } from 'drizzle-orm';
import { cacheGetOrSet, cacheKeys } from '$lib/server/redis/cache';

/**
 * Fisher-Yates shuffle for unbiased randomization
 */
function shuffle<T>(array: T[]): T[] {
	const result = [...array];
	for (let i = result.length - 1; i > 0; i--) {
		const j = Math.floor(Math.random() * (i + 1));
		[result[i], result[j]] = [result[j], result[i]];
	}
	return result;
}

interface DiscoverRecipe {
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

interface DiscoverCandidate {
	id: string;
	slug: string;
	title: string;
	description: string | null;
	photoUrl: string | null;
	prepTimeMinutes: number | null;
	cookTimeMinutes: number | null;
	difficulty: string | null;
	cuisine: string | null;
	tag: string | null;
	authorId: string;
	publishedAt: Date | null;
	upvotes: number;
	downvotes: number;
	authorName: string;
	authorUsername: string;
}

interface DiscoverData {
	recipes: DiscoverRecipe[];
}

export const GET: RequestHandler = async ({ url }) => {
	const lang = url.searchParams.get('lang') || 'en';

	// Cache the high-quality candidates (before randomization)
	// The randomization happens after cache retrieval for variety
	const candidates = await cacheGetOrSet<DiscoverCandidate[]>(
		cacheKeys.discover(lang),
		'discover',
		async () => {
			// Use ISO strings for PostgreSQL compatibility (Date.toString() produces unparseable format)
			const now = new Date();
			const thirtyDaysAgo = new Date(now.getTime() - 30 * 24 * 60 * 60 * 1000).toISOString();

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
			// Filter by minimum votes and 85% upvote ratio directly in SQL
			// This avoids loading recipes that don't meet criteria into memory
			const filteredRecipes = await db
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
						trendingRecipeIds.length > 0
							? notInArray(recipes.id, trendingRecipeIds)
							: undefined
					)
				)
				.groupBy(recipes.id)
				.having(
					and(
						// Minimum 20 votes
						sql`COUNT(${votes.id}) >= 20`,
						// 85%+ upvote ratio: upvotes / (upvotes + downvotes) >= 0.85
						// Rewritten to avoid division: upvotes >= 0.85 * (upvotes + downvotes)
						// Which simplifies to: upvotes * 0.15 >= downvotes * 0.85
						// Or: upvotes * 15 >= downvotes * 85 (using integers to avoid float issues)
						sql`SUM(CASE WHEN ${votes.value} > 0 THEN 1 ELSE 0 END) * 15 >= SUM(CASE WHEN ${votes.value} < 0 THEN 1 ELSE 0 END) * 85`
					)
				);

			// Get author info for all filtered recipes
			const authorIds = [...new Set(filteredRecipes.map((r) => r.authorId))];
			const authors = authorIds.length > 0
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

			// Return enriched candidates (we'll shuffle at request time for variety)
			return filteredRecipes.map((r) => {
				const author = authorMap.get(r.authorId);
				return {
					id: r.id,
					slug: r.slug,
					title: r.title,
					description: r.description,
					photoUrl: r.photoUrl,
					prepTimeMinutes: r.prepTimeMinutes,
					cookTimeMinutes: r.cookTimeMinutes,
					difficulty: r.difficulty,
					cuisine: r.cuisine,
					tag: r.tag,
					authorId: r.authorId,
					publishedAt: r.publishedAt,
					upvotes: r.upvotes,
					downvotes: r.downvotes,
					authorName: author?.fullName || author?.name || 'Unknown',
					authorUsername: author?.username || ''
				};
			});
		}
	);

	// Shuffle and take 10 at request time for variety
	const selectedRecipes = shuffle(candidates).slice(0, 10);

	const data: DiscoverData = {
		recipes: selectedRecipes.map((r) => ({
			slug: r.slug,
			title: r.title,
			description: r.description,
			photoUrl: r.photoUrl,
			authorName: r.authorName,
			authorUsername: r.authorUsername,
			cuisine: r.cuisine,
			tag: r.tag,
			difficulty: r.difficulty,
			prepTimeMinutes: r.prepTimeMinutes,
			cookTimeMinutes: r.cookTimeMinutes,
			upvotes: r.upvotes,
			downvotes: r.downvotes,
			publishedAt: r.publishedAt
		}))
	};

	return json(data, {
		headers: {
			'Cache-Control': 'public, max-age=120, stale-while-revalidate=600'
		}
	});
};
