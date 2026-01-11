import { error } from '@sveltejs/kit';
import type { PageServerLoad } from './$types';
import { db } from '$lib/server/db';
import { user, follows, recipes, votes } from '$lib/server/db/schema';
import { eq, and, sql, desc, asc } from 'drizzle-orm';

export const load: PageServerLoad = async ({ params, locals, url }) => {
	const { username } = params;
	const sortBy = url.searchParams.get('sort') || 'latest';

	// Find user by username
	const [foundUser] = await db
		.select()
		.from(user)
		.where(eq(user.username, username))
		.limit(1);

	if (!foundUser) {
		throw error(404, 'User not found');
	}

	// Determine sort order
	let orderByClause;
	if (sortBy === 'upvotes') {
		orderByClause = desc(sql`COALESCE(SUM(CASE WHEN ${votes.value} > 0 THEN 1 ELSE 0 END), 0)`);
	} else if (sortBy === 'earliest') {
		orderByClause = asc(recipes.publishedAt);
	} else {
		// Default: latest
		orderByClause = desc(recipes.publishedAt);
	}

	// Get user's published recipes with vote counts
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
		.orderBy(orderByClause);

	// Calculate total upvotes across all recipes
	const totalUpvotes = chefRecipes.reduce((sum, r) => sum + r.upvotes, 0);

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

	return {
		chef: {
			id: foundUser.id,
			username: foundUser.username,
			fullName: displayName,
			country: displayCountry,
			bio: displayBio,
			photo: displayPhoto,
			profileTier: profileTier,
			chefSince: memberSince,
			stats: {
				recipeCount: chefRecipes.length,
				totalUpvotes,
				followerCount
			}
		},
		recipes: chefRecipes.map((r) => ({
			slug: r.slug,
			title: r.title,
			description: r.description,
			image: r.photoUrl,
			authorName: displayName,
			authorUsername: foundUser.username,
			cuisine: r.cuisine,
			tag: r.tag,
			difficulty: r.difficulty as 'easy' | 'medium' | 'hard' | null,
			prepTimeMinutes: r.prepTimeMinutes,
			cookTimeMinutes: r.cookTimeMinutes,
			upvotes: r.upvotes,
			downvotes: r.downvotes,
			publishedAt: r.publishedAt
		})),
		isFollowing,
		isOwnProfile: locals.user?.id === foundUser.id,
		sortBy
	};
};
