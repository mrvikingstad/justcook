import type { PageServerLoad } from './$types';
import { db } from '$lib/server/db';
import { recipes, ingredients, steps, user, votes, comments } from '$lib/server/db/schema';
import { eq, sql, asc, and } from 'drizzle-orm';
import { error } from '@sveltejs/kit';

export const load: PageServerLoad = async ({ params, locals }) => {
	const { slug } = params;
	const currentUser = locals.user;

	// Get recipe by slug
	const recipeResult = await db
		.select({
			id: recipes.id,
			title: recipes.title,
			slug: recipes.slug,
			description: recipes.description,
			photoUrl: recipes.photoUrl,
			prepTimeMinutes: recipes.prepTimeMinutes,
			cookTimeMinutes: recipes.cookTimeMinutes,
			difficulty: recipes.difficulty,
			cuisine: recipes.cuisine,
			tag: recipes.tag,
			servings: recipes.servings,
			commentCount: recipes.commentCount,
			authorId: recipes.authorId,
			isPublished: recipes.isPublished,
			publishedAt: recipes.publishedAt,
			updatedAt: recipes.updatedAt
		})
		.from(recipes)
		.where(eq(recipes.slug, slug))
		.limit(1);

	const recipe = recipeResult[0];

	if (!recipe || !recipe.isPublished) {
		throw error(404, 'Recipe not found');
	}

	// Get vote counts
	const voteResult = await db
		.select({
			upvotes: sql<number>`COALESCE(SUM(CASE WHEN ${votes.value} > 0 THEN 1 ELSE 0 END), 0)::int`,
			downvotes: sql<number>`COALESCE(SUM(CASE WHEN ${votes.value} < 0 THEN 1 ELSE 0 END), 0)::int`
		})
		.from(votes)
		.where(eq(votes.recipeId, recipe.id));

	const voteCounts = voteResult[0] || { upvotes: 0, downvotes: 0 };

	// Get user's vote if logged in
	let userVote: 1 | -1 | null = null;
	if (currentUser) {
		const userVoteResult = await db
			.select({ value: votes.value })
			.from(votes)
			.where(and(eq(votes.userId, currentUser.id), eq(votes.recipeId, recipe.id)))
			.limit(1);

		if (userVoteResult.length > 0) {
			userVote = userVoteResult[0].value as 1 | -1;
		}
	}

	// Get author info
	const authorResult = await db
		.select({
			username: user.username,
			fullName: user.fullName,
			profileTier: user.profileTier
		})
		.from(user)
		.where(eq(user.id, recipe.authorId))
		.limit(1);

	const author = authorResult[0];

	// Get ingredients ordered by sortOrder
	const ingredientList = await db
		.select({
			name: ingredients.name,
			amount: ingredients.amount,
			unit: ingredients.unit,
			notes: ingredients.notes
		})
		.from(ingredients)
		.where(eq(ingredients.recipeId, recipe.id))
		.orderBy(asc(ingredients.sortOrder));

	// Get steps ordered by stepNumber
	const stepList = await db
		.select({
			instruction: steps.instruction
		})
		.from(steps)
		.where(eq(steps.recipeId, recipe.id))
		.orderBy(asc(steps.stepNumber));

	// Check if published and updated times differ significantly (more than 1 minute)
	const wasEdited = recipe.publishedAt && recipe.updatedAt &&
		(recipe.updatedAt.getTime() - recipe.publishedAt.getTime() > 60000);

	return {
		recipe: {
			id: recipe.id,
			title: recipe.title,
			slug: recipe.slug,
			description: recipe.description,
			image: recipe.photoUrl,
			authorId: recipe.authorId,
			authorName: author?.fullName || 'Unknown',
			authorUsername: author?.username || '',
			authorProfileTier: author?.profileTier || 'user',
			cuisine: recipe.cuisine,
			tag: recipe.tag,
			difficulty: recipe.difficulty as 'easy' | 'medium' | 'hard' | null,
			prepTimeMinutes: recipe.prepTimeMinutes,
			cookTimeMinutes: recipe.cookTimeMinutes,
			baseServings: recipe.servings,
			upvotes: voteCounts.upvotes,
			downvotes: voteCounts.downvotes,
			userVote,
			commentCount: recipe.commentCount,
			publishedAt: recipe.publishedAt,
			updatedAt: wasEdited ? recipe.updatedAt : null,
			ingredients: ingredientList.map((i) => ({
				name: i.name,
				amount: i.amount ? parseFloat(i.amount) : null,
				unit: i.unit,
				notes: i.notes
			})),
			steps: stepList
		},
		isOwn: currentUser?.id === recipe.authorId
	};
};
