import type { PageServerLoad } from './$types';
import { db } from '$lib/server/db';
import { user, recipes, votes } from '$lib/server/db/schema';
import { eq, and, desc, sql, or, ilike } from 'drizzle-orm';
import { parseSearchQuery } from '$lib/utils/searchParser';

const RECIPES_PER_PAGE = 12;
const MIN_SEARCH_LENGTH = 2; // Minimum characters for text search

export const load: PageServerLoad = async ({ url }) => {
	const page = parseInt(url.searchParams.get('page') || '1');
	const sortBy = url.searchParams.get('sort') || 'total';
	const difficulty = url.searchParams.get('difficulty') || 'all';
	const maxTime = url.searchParams.get('maxTime') || 'all';
	const tag = url.searchParams.get('tag') || null;
	const q = url.searchParams.get('q') || '';
	const lang = url.searchParams.get('lang') || 'en';

	const offset = (page - 1) * RECIPES_PER_PAGE;

	// Parse search query to extract text and hashtags
	const { text: searchText, hashtags } = parseSearchQuery(q);

	// Build where conditions
	const conditions = [eq(recipes.isPublished, true)];

	// Filter by language
	conditions.push(eq(recipes.language, lang));

	if (difficulty !== 'all') {
		conditions.push(eq(recipes.difficulty, difficulty));
	}

	if (maxTime !== 'all') {
		const maxMinutes = parseInt(maxTime);
		conditions.push(
			sql`(${recipes.prepTimeMinutes} + ${recipes.cookTimeMinutes}) <= ${maxMinutes}`
		);
	}

	if (tag) {
		conditions.push(eq(recipes.tag, tag));
	}

	// Add hashtag conditions (AND logic - all hashtags must match)
	// Each hashtag matches EITHER cuisine OR tag field
	for (const hashtag of hashtags) {
		const hashtagCondition = or(
			ilike(recipes.cuisine, `%${hashtag}%`),
			ilike(recipes.tag, `%${hashtag}%`)
		);
		if (hashtagCondition) {
			conditions.push(hashtagCondition);
		}
	}

	// Add text search condition using PostgreSQL full-text search
	// Falls back to ILIKE for short queries (less than MIN_SEARCH_LENGTH chars)
	if (searchText && searchText.length >= MIN_SEARCH_LENGTH) {
		// Use full-text search with the search_vector column (GIN indexed)
		// plainto_tsquery handles user input safely and converts to tsquery format
		conditions.push(
			sql`${recipes.searchVector} @@ plainto_tsquery('english', ${searchText})`
		);
	} else if (searchText && searchText.length > 0) {
		// For very short queries, use prefix matching on title only
		const textCondition = ilike(recipes.title, `${searchText}%`);
		if (textCondition) {
			conditions.push(textCondition);
		}
	}

	// Get current date info for time-based sorting
	// Use ISO strings for PostgreSQL compatibility (Date.toString() produces unparseable format)
	const now = new Date();
	const sevenDaysAgo = new Date(now.getTime() - 7 * 24 * 60 * 60 * 1000).toISOString();
	const thirtyDaysAgo = new Date(now.getTime() - 30 * 24 * 60 * 60 * 1000).toISOString();

	// Determine sort column name for ORDER BY
	type SortKey = 'latest' | 'week' | 'month' | 'total';
	const sortKey: SortKey = (sortBy === 'latest' || sortBy === 'week' || sortBy === 'month') ? sortBy : 'total';

	// Get total count for pagination
	const countResult = await db
		.select({ count: sql<number>`count(*)::int` })
		.from(recipes)
		.where(and(...conditions));

	const totalCount = countResult[0]?.count || 0;
	const totalPages = Math.ceil(totalCount / RECIPES_PER_PAGE);

	// Get recipes with vote data (including time-filtered counts for efficient sorting)
	// All vote aggregations are computed in a single pass with conditional sums
	const recipeResults = await db
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
			// Time-filtered upvote counts for sorting (computed in same aggregation pass)
			weekUpvotes: sql<number>`COALESCE(SUM(CASE WHEN ${votes.value} > 0 AND ${votes.createdAt} >= ${sevenDaysAgo} THEN 1 ELSE 0 END), 0)::int`,
			monthUpvotes: sql<number>`COALESCE(SUM(CASE WHEN ${votes.value} > 0 AND ${votes.createdAt} >= ${thirtyDaysAgo} THEN 1 ELSE 0 END), 0)::int`
		})
		.from(recipes)
		.leftJoin(votes, eq(recipes.id, votes.recipeId))
		.where(and(...conditions))
		.groupBy(recipes.id)
		.orderBy(
			sortKey === 'latest' ? desc(recipes.publishedAt) :
			sortKey === 'week' ? desc(sql`COALESCE(SUM(CASE WHEN ${votes.value} > 0 AND ${votes.createdAt} >= ${sevenDaysAgo} THEN 1 ELSE 0 END), 0)`) :
			sortKey === 'month' ? desc(sql`COALESCE(SUM(CASE WHEN ${votes.value} > 0 AND ${votes.createdAt} >= ${thirtyDaysAgo} THEN 1 ELSE 0 END), 0)`) :
			desc(sql`COALESCE(SUM(CASE WHEN ${votes.value} > 0 THEN 1 ELSE 0 END), 0)`)
		)
		.limit(RECIPES_PER_PAGE)
		.offset(offset);

	// Get author info
	const authorIds = [...new Set(recipeResults.map((r) => r.authorId))];
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

	// Get popular tags for sidebar (sorted alphabetically, filtered by language)
	const popularTags = await db
		.select({
			tag: recipes.tag,
			count: sql<number>`count(*)::int`
		})
		.from(recipes)
		.where(and(
			eq(recipes.isPublished, true),
			eq(recipes.language, lang),
			sql`${recipes.tag} IS NOT NULL`
		))
		.groupBy(recipes.tag)
		.orderBy(recipes.tag)
		.limit(12);

	return {
		recipes: recipeResults.map((r) => {
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
		}),
		popularTags: popularTags.map((t) => t.tag).filter(Boolean) as string[],
		pagination: {
			page,
			totalPages,
			totalCount,
			hasNext: page < totalPages,
			hasPrev: page > 1
		},
		filters: {
			sortBy,
			difficulty,
			maxTime,
			tag,
			q,
			lang
		}
	};
};
