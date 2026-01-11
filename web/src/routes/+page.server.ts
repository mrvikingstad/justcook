import type { PageServerLoad } from './$types';
import { db } from '$lib/server/db';
import { user, follows, recipes, votes } from '$lib/server/db/schema';
import { eq, and, desc, sql, gte, notInArray } from 'drizzle-orm';

export const load: PageServerLoad = async ({ url }) => {
	const lang = url.searchParams.get('lang') || 'en';
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
		.limit(5);

	// Get author info for trending recipes
	const trendingAuthorIds = [...new Set(trendingRecipes.map((r) => r.authorId))];
	const trendingAuthors = trendingAuthorIds.length > 0
		? await db
				.select({
					userId: user.id,
					username: user.username,
					fullName: user.fullName
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
		.limit(5);

	// Get chef details
	const trendingChefIds = trendingChefs.map((c) => c.userId);
	const chefDetails = trendingChefIds.length > 0
		? await db
				.select({
					userId: user.id,
					username: user.username,
					fullName: user.fullName,
					photoUrl: user.photoUrl,
					profileTier: user.profileTier
				})
				.from(user)
				.where(sql`${user.id} IN ${trendingChefIds}`)
		: [];

	const chefDetailsMap = new Map(chefDetails.map((c) => [c.userId, c]));

	// Discover Recipes: Random high-quality recipes not in trending
	// Criteria: >85% upvote ratio, minimum 20 votes, not in trending
	const trendingRecipeIds = trendingRecipes.map((r) => r.id);

	// Get recipes with vote stats
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
		.slice(0, 5);

	// Get author info for discover recipes
	const discoverAuthorIds = [...new Set(highQualityRecipes.map((r) => r.authorId))];
	const discoverAuthors = discoverAuthorIds.length > 0
		? await db
				.select({
					userId: user.id,
					username: user.username,
					fullName: user.fullName
				})
				.from(user)
				.where(sql`${user.id} IN ${discoverAuthorIds}`)
		: [];

	const discoverAuthorMap = new Map(discoverAuthors.map((a) => [a.userId, a]));

	return {
		trending: trendingRecipes.map((r) => {
			const author = trendingAuthorMap.get(r.authorId);
			return {
				slug: r.slug,
				title: r.title,
				description: r.description,
				image: r.photoUrl,
				authorName: author?.fullName || 'Unknown',
				authorUsername: author?.username || '',
				cuisine: r.cuisine,
				tag: r.tag,
				difficulty: r.difficulty as 'easy' | 'medium' | 'hard',
				prepTimeMinutes: r.prepTimeMinutes,
				cookTimeMinutes: r.cookTimeMinutes,
				upvotes: r.upvotes,
				downvotes: r.downvotes,
				publishedAt: r.publishedAt
			};
		}),
		trendingChefs: trendingChefs.map((c) => {
			const chef = chefDetailsMap.get(c.userId);
			return {
				username: chef?.username || '',
				fullName: chef?.fullName || 'Unknown',
				photoUrl: chef?.photoUrl,
				profileTier: chef?.profileTier || 'user',
				newFollowers: c.newFollowers
			};
		}).filter((c) => c.username),
		discover: highQualityRecipes.map((r) => {
			const author = discoverAuthorMap.get(r.authorId);
			return {
				slug: r.slug,
				title: r.title,
				description: r.description,
				image: r.photoUrl,
				authorName: author?.fullName || 'Unknown',
				authorUsername: author?.username || '',
				cuisine: r.cuisine,
				tag: r.tag,
				difficulty: r.difficulty as 'easy' | 'medium' | 'hard',
				prepTimeMinutes: r.prepTimeMinutes,
				cookTimeMinutes: r.cookTimeMinutes,
				upvotes: r.upvotes,
				downvotes: r.downvotes,
				publishedAt: r.publishedAt
			};
		}),
		lang
	};
};
