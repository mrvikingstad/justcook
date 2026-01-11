import { error } from '@sveltejs/kit';
import type { PageServerLoad } from './$types';
import { db } from '$lib/server/db';
import { recipes, comments, user } from '$lib/server/db/schema';
import { eq, desc } from 'drizzle-orm';

export const load: PageServerLoad = async ({ params, locals }) => {
	const { slug } = params;

	// Get recipe by slug
	const [recipe] = await db
		.select({
			id: recipes.id,
			title: recipes.title,
			slug: recipes.slug,
			authorId: recipes.authorId,
			isPublished: recipes.isPublished
		})
		.from(recipes)
		.where(eq(recipes.slug, slug))
		.limit(1);

	if (!recipe || !recipe.isPublished) {
		throw error(404, 'Recipe not found');
	}

	// Get recipe author info
	const [recipeAuthor] = await db
		.select({
			username: user.username,
			fullName: user.fullName
		})
		.from(user)
		.where(eq(user.id, recipe.authorId))
		.limit(1);

	// Get comments with user info
	const commentsResult = await db
		.select({
			id: comments.id,
			content: comments.content,
			createdAt: comments.createdAt,
			userId: comments.userId,
			userName: user.name,
			userImage: user.image,
			fullName: user.fullName,
			photoUrl: user.photoUrl
		})
		.from(comments)
		.innerJoin(user, eq(comments.userId, user.id))
		.where(eq(comments.recipeId, recipe.id))
		.orderBy(desc(comments.createdAt));

	return {
		recipe: {
			id: recipe.id,
			title: recipe.title,
			slug: recipe.slug,
			authorId: recipe.authorId,
			authorName: recipeAuthor?.fullName || 'Unknown'
		},
		comments: commentsResult.map((c) => ({
			id: c.id,
			content: c.content,
			createdAt: c.createdAt,
			authorId: c.userId,
			authorName: c.fullName || c.userName || 'Anonymous',
			authorAvatar: c.photoUrl || c.userImage || null,
			isRecipeAuthor: c.userId === recipe.authorId,
			isOwn: locals.user?.id === c.userId
		})),
		isLoggedIn: !!locals.user,
		currentUserId: locals.user?.id || null
	};
};
