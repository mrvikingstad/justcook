import type { PageServerLoad } from './$types';
import { db } from '$lib/server/db';
import { recipes, ingredients, steps, tips, equipment, user, votes } from '$lib/server/db/schema';
import { eq, sql, asc, and } from 'drizzle-orm';
import { error } from '@sveltejs/kit';

export const load: PageServerLoad = async ({ params, locals }) => {
	const { slug } = params;
	const currentUser = locals.user;

	// Get recipe with author info and vote counts in a single query (fixes N+1)
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
			updatedAt: recipes.updatedAt,
			// Author info via JOIN
			authorUsername: user.username,
			authorFullName: user.fullName,
			authorName: user.name,
			authorProfileTier: user.profileTier,
			// Vote counts via aggregate
			upvotes: sql<number>`COALESCE(SUM(CASE WHEN ${votes.value} > 0 THEN 1 ELSE 0 END), 0)::int`,
			downvotes: sql<number>`COALESCE(SUM(CASE WHEN ${votes.value} < 0 THEN 1 ELSE 0 END), 0)::int`
		})
		.from(recipes)
		.leftJoin(user, eq(recipes.authorId, user.id))
		.leftJoin(votes, eq(recipes.id, votes.recipeId))
		.where(eq(recipes.slug, slug))
		.groupBy(recipes.id, user.id, user.username, user.fullName, user.name, user.profileTier)
		.limit(1);

	const recipe = recipeResult[0];

	if (!recipe) {
		throw error(404, 'Recipe not found');
	}

	// Only the owner can view unpublished recipes
	if (!recipe.isPublished && recipe.authorId !== currentUser?.id) {
		throw error(404, 'Recipe not found');
	}

	// Get user's vote if logged in (this is user-specific, so can't be cached with recipe)
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

	// Get ingredients, steps, tips, and equipment in parallel (these are recipe-specific, can run concurrently)
	const [ingredientList, stepList, tipList, equipmentList] = await Promise.all([
		db
			.select({
				name: ingredients.name,
				amount: ingredients.amount,
				unit: ingredients.unit,
				notes: ingredients.notes
			})
			.from(ingredients)
			.where(eq(ingredients.recipeId, recipe.id))
			.orderBy(asc(ingredients.sortOrder)),
		db
			.select({
				instruction: steps.instruction
			})
			.from(steps)
			.where(eq(steps.recipeId, recipe.id))
			.orderBy(asc(steps.stepNumber)),
		db
			.select({
				content: tips.content
			})
			.from(tips)
			.where(eq(tips.recipeId, recipe.id))
			.orderBy(asc(tips.sortOrder)),
		db
			.select({
				name: equipment.name,
				notes: equipment.notes
			})
			.from(equipment)
			.where(eq(equipment.recipeId, recipe.id))
			.orderBy(asc(equipment.sortOrder))
	]);

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
			authorName: recipe.authorFullName || recipe.authorName || 'Unknown',
			authorUsername: recipe.authorUsername || '',
			authorProfileTier: recipe.authorProfileTier || 'user',
			cuisine: recipe.cuisine,
			tag: recipe.tag,
			difficulty: recipe.difficulty as 'easy' | 'medium' | 'hard' | null,
			prepTimeMinutes: recipe.prepTimeMinutes,
			cookTimeMinutes: recipe.cookTimeMinutes,
			baseServings: recipe.servings,
			upvotes: recipe.upvotes,
			downvotes: recipe.downvotes,
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
			steps: stepList,
			tips: tipList,
			equipment: equipmentList
		},
		isOwn: currentUser?.id === recipe.authorId
	};
};
