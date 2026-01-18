import { json } from '@sveltejs/kit';
import type { RequestHandler } from './$types';
import { db } from '$lib/server/db';
import { user, follows, recipes, votes } from '$lib/server/db/schema';
import { eq, and, sql, desc, count } from 'drizzle-orm';

const DEFAULT_LIMIT = 20;
const MAX_LIMIT = 50;

export const GET: RequestHandler = async ({ params, locals, url }) => {
	const { username } = params;
	const sortBy = url.searchParams.get('sort') || 'latest';
	const page = Math.max(1, parseInt(url.searchParams.get('page') || '1', 10));
	const limit = Math.min(MAX_LIMIT, Math.max(1, parseInt(url.searchParams.get('limit') || String(DEFAULT_LIMIT), 10)));
	const offset = (page - 1) * limit;

	// Find user by username - only select needed fields
	const [foundUser] = await db
		.select({
			id: user.id,
			username: user.username,
			name: user.name,
			fullName: user.fullName,
			country: user.country,
			bio: user.bio,
			photoUrl: user.photoUrl,
			image: user.image,
			profileTier: user.profileTier,
			createdAt: user.createdAt
		})
		.from(user)
		.where(eq(user.username, username))
		.limit(1);

	if (!foundUser) {
		return json({ error: 'User not found' }, { status: 404 });
	}

	// Determine sort order
	let orderByClause;
	if (sortBy === 'upvotes') {
		orderByClause = desc(sql`COALESCE(SUM(CASE WHEN ${votes.value} > 0 THEN 1 ELSE 0 END), 0)`);
	} else {
		// Default: latest
		orderByClause = desc(recipes.publishedAt);
	}

	// Get total recipe count and total upvotes for stats (separate query for accurate counts)
	const [statsResult] = await db
		.select({
			recipeCount: sql<number>`COUNT(DISTINCT ${recipes.id})::int`,
			totalUpvotes: sql<number>`COALESCE(SUM(CASE WHEN ${votes.value} > 0 THEN 1 ELSE 0 END), 0)::int`
		})
		.from(recipes)
		.leftJoin(votes, eq(recipes.id, votes.recipeId))
		.where(and(eq(recipes.authorId, foundUser.id), eq(recipes.isPublished, true)));

	const recipeCount = statsResult?.recipeCount ?? 0;
	const totalUpvotes = statsResult?.totalUpvotes ?? 0;

	// Get paginated user's published recipes with vote counts
	const chefRecipes = await db
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
			upvotes: sql<number>`COALESCE(SUM(CASE WHEN ${votes.value} > 0 THEN 1 ELSE 0 END), 0)::int`,
			downvotes: sql<number>`COALESCE(SUM(CASE WHEN ${votes.value} < 0 THEN 1 ELSE 0 END), 0)::int`
		})
		.from(recipes)
		.leftJoin(votes, eq(recipes.id, votes.recipeId))
		.where(and(eq(recipes.authorId, foundUser.id), eq(recipes.isPublished, true)))
		.groupBy(recipes.id)
		.orderBy(orderByClause)
		.limit(limit)
		.offset(offset);

	// Get follower count
	const followerResult = await db
		.select({ count: sql<number>`count(*)::int` })
		.from(follows)
		.where(eq(follows.followingId, foundUser.id));
	const followerCount = followerResult[0]?.count ?? 0;

	// Check if current user is following this chef
	let isFollowing = false;
	if (locals.user && locals.user.id !== foundUser.id) {
		const followResult = await db
			.select({ followerId: follows.followerId })
			.from(follows)
			.where(and(
				eq(follows.followerId, locals.user.id),
				eq(follows.followingId, foundUser.id)
			))
			.limit(1);
		isFollowing = followResult.length > 0;
	}

	// Determine profile display values
	const displayName = foundUser.fullName || foundUser.name || foundUser.username || 'User';
	const displayBio = foundUser.bio || null;
	const displayCountry = foundUser.country || null;
	const displayPhoto = foundUser.photoUrl || foundUser.image || null;
	const profileTier = foundUser.profileTier || 'user';
	const memberSince = foundUser.createdAt || new Date();

	const totalPages = Math.ceil(recipeCount / limit);

	return json({
		chef: {
			id: foundUser.id,
			username: foundUser.username,
			fullName: displayName,
			country: displayCountry,
			bio: displayBio,
			photoUrl: displayPhoto,
			profileTier: profileTier,
			chefSince: memberSince,
			stats: {
				recipeCount,
				totalUpvotes,
				followerCount
			}
		},
		recipes: chefRecipes.map((r) => ({
			slug: r.slug,
			title: r.title,
			description: r.description,
			photoUrl: r.photoUrl,
			authorName: displayName,
			authorUsername: foundUser.username,
			cuisine: r.cuisine,
			tag: r.tag,
			difficulty: r.difficulty,
			prepTimeMinutes: r.prepTimeMinutes,
			cookTimeMinutes: r.cookTimeMinutes,
			upvotes: r.upvotes,
			downvotes: r.downvotes,
			publishedAt: r.publishedAt
		})),
		pagination: {
			page,
			limit,
			totalCount: recipeCount,
			totalPages,
			hasMore: page < totalPages
		},
		isFollowing,
		isOwnProfile: locals.user?.id === foundUser.id
	});
};
