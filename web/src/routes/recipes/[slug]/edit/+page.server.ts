import { redirect, error } from '@sveltejs/kit';
import type { PageServerLoad } from './$types';
import { db } from '$lib/server/db';
import { recipes, ingredients, steps, user } from '$lib/server/db/schema';
import { eq, asc } from 'drizzle-orm';
import { getCountryByName } from '$lib/data/countries';

export const load: PageServerLoad = async ({ params, locals }) => {
	if (!locals.user) {
		throw redirect(302, `/auth/login?redirect=/recipes/${params.slug}/edit`);
	}

	const { slug } = params;

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
			language: recipes.language,
			servings: recipes.servings,
			authorId: recipes.authorId
		})
		.from(recipes)
		.where(eq(recipes.slug, slug))
		.limit(1);

	const recipe = recipeResult[0];

	if (!recipe) {
		throw error(404, 'Recipe not found');
	}

	// Check ownership
	if (recipe.authorId !== locals.user.id) {
		throw error(403, 'You can only edit your own recipes');
	}

	// Get ingredients ordered by sortOrder
	const ingredientList = await db
		.select({
			ingredientKey: ingredients.ingredientKey,
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

	// Fetch user's country from their profile
	const [userData] = await db
		.select({ country: user.country })
		.from(user)
		.where(eq(user.id, locals.user.id))
		.limit(1);

	// Derive language code from country
	let userLanguageCode: string | null = null;
	if (userData?.country) {
		const countryDef = getCountryByName(userData.country);
		userLanguageCode = countryDef?.language || null;
	}

	return {
		recipe: {
			id: recipe.id,
			title: recipe.title,
			slug: recipe.slug,
			description: recipe.description || '',
			photoUrl: recipe.photoUrl,
			prepTimeMinutes: recipe.prepTimeMinutes,
			cookTimeMinutes: recipe.cookTimeMinutes,
			difficulty: recipe.difficulty as 'easy' | 'medium' | 'hard' | null,
			cuisine: recipe.cuisine || '',
			tag: recipe.tag || '',
			language: recipe.language,
			servings: recipe.servings,
			ingredients: ingredientList.map((i) => ({
				ingredientKey: i.ingredientKey || '',
				name: i.name,
				amount: i.amount ? parseFloat(i.amount) : null,
				unit: i.unit || '',
				notes: i.notes || ''
			})),
			steps: stepList.map((s) => s.instruction)
		},
		userLanguageCode
	};
};
