import { json } from '@sveltejs/kit';
import type { RequestHandler } from './$types';
import { db } from '$lib/server/db';
import { recipes, ingredients, steps, tips, equipment } from '$lib/server/db/schema';
import { eq, sql } from 'drizzle-orm';
import { moderateRecipe } from '$lib/server/moderation';
import { cacheDeletePattern } from '$lib/server/redis/cache';
import { logger, getRequestId } from '$lib/server/logger';
import {
	isValidCuisine,
	isValidLanguageCode,
	isValidDifficulty,
	isValidPrepTime,
	isValidCookTime,
	isValidServings,
	isValidUrl,
	VALIDATION_ERRORS,
	TIME_BOUNDS
} from '$lib/server/validation/recipe';
import { sanitizeText } from '$lib/server/validation/sanitize';

export const PUT: RequestHandler = async ({ params, request, locals }) => {
	if (!locals.user) {
		return json({ error: 'Unauthorized' }, { status: 401 });
	}

	const { id } = params;

	// Get existing recipe to verify ownership
	const [existingRecipe] = await db
		.select({
			id: recipes.id,
			authorId: recipes.authorId,
			slug: recipes.slug
		})
		.from(recipes)
		.where(eq(recipes.id, id))
		.limit(1);

	if (!existingRecipe) {
		return json({ error: 'Recipe not found' }, { status: 404 });
	}

	if (existingRecipe.authorId !== locals.user.id) {
		return json({ error: 'You can only edit your own recipes' }, { status: 403 });
	}

	const data = await request.json();
	const {
		title,
		description,
		cuisine,
		tag,
		language,
		difficulty,
		prepTime,
		cookTime,
		servings,
		photoUrl,
		ingredients: ingredientList,
		steps: stepList,
		tips: tipList = [],
		equipment: equipmentList = []
	} = data;

	// Validate required fields
	if (typeof title !== 'string' || title.trim().length === 0) {
		return json({ error: 'Title is required' }, { status: 400 });
	}

	if (title.trim().length > 200) {
		return json({ error: 'Title must be 200 characters or less' }, { status: 400 });
	}

	if (typeof description !== 'string' || description.trim().length === 0) {
		return json({ error: 'Description is required' }, { status: 400 });
	}

	if (description.trim().length > 2000) {
		return json({ error: 'Description must be 2000 characters or less' }, { status: 400 });
	}

	if (typeof cuisine !== 'string' || cuisine.trim().length === 0) {
		return json({ error: 'Cuisine is required' }, { status: 400 });
	}

	if (!isValidCuisine(cuisine.trim())) {
		return json({ error: VALIDATION_ERRORS.INVALID_CUISINE }, { status: 400 });
	}

	if (typeof tag !== 'string' || tag.trim().length === 0) {
		return json({ error: 'Tag is required' }, { status: 400 });
	}

	if (tag.trim().length > 50) {
		return json({ error: 'Tag must be 50 characters or less' }, { status: 400 });
	}

	if (typeof language !== 'string' || language.trim().length === 0) {
		return json({ error: 'Language is required' }, { status: 400 });
	}

	if (!isValidLanguageCode(language.trim())) {
		return json({ error: VALIDATION_ERRORS.INVALID_LANGUAGE }, { status: 400 });
	}

	if (difficulty && !isValidDifficulty(difficulty)) {
		return json({ error: VALIDATION_ERRORS.INVALID_DIFFICULTY }, { status: 400 });
	}

	if (typeof prepTime !== 'number' || !isValidPrepTime(prepTime)) {
		if (typeof prepTime !== 'number' || prepTime < TIME_BOUNDS.MIN_PREP_TIME) {
			return json({ error: VALIDATION_ERRORS.PREP_TIME_TOO_LOW }, { status: 400 });
		}
		return json({ error: VALIDATION_ERRORS.PREP_TIME_TOO_HIGH }, { status: 400 });
	}

	if (typeof cookTime !== 'number' || !isValidCookTime(cookTime)) {
		if (typeof cookTime !== 'number' || cookTime < TIME_BOUNDS.MIN_COOK_TIME) {
			return json({ error: VALIDATION_ERRORS.COOK_TIME_TOO_LOW }, { status: 400 });
		}
		return json({ error: VALIDATION_ERRORS.COOK_TIME_TOO_HIGH }, { status: 400 });
	}

	if (servings !== undefined && !isValidServings(servings)) {
		if (servings < TIME_BOUNDS.MIN_SERVINGS) {
			return json({ error: VALIDATION_ERRORS.SERVINGS_TOO_LOW }, { status: 400 });
		}
		return json({ error: VALIDATION_ERRORS.SERVINGS_TOO_HIGH }, { status: 400 });
	}

	if (!Array.isArray(ingredientList) || ingredientList.length < 2) {
		return json({ error: 'At least 2 ingredients are required' }, { status: 400 });
	}

	for (const ing of ingredientList) {
		if (!ing.ingredientKey || typeof ing.ingredientKey !== 'string') {
			return json({ error: 'All ingredients must be selected from our database' }, { status: 400 });
		}
		if (!ing.amount || parseFloat(ing.amount) <= 0) {
			return json({ error: 'All ingredients must have a valid amount' }, { status: 400 });
		}
		if (!ing.unit || typeof ing.unit !== 'string' || ing.unit.trim().length === 0) {
			return json({ error: 'All ingredients must have a unit' }, { status: 400 });
		}
		if (ing.notes && typeof ing.notes === 'string' && ing.notes.trim().length > 200) {
			return json({ error: 'Ingredient notes must be 200 characters or less' }, { status: 400 });
		}
	}

	if (!Array.isArray(stepList) || stepList.length < 1) {
		return json({ error: 'At least 1 instruction is required' }, { status: 400 });
	}

	for (const step of stepList) {
		if (!step.instruction || typeof step.instruction !== 'string' || step.instruction.trim().length === 0) {
			return json({ error: 'All instructions must have content' }, { status: 400 });
		}
		if (step.instruction.trim().length > 2000) {
			return json({ error: 'Each instruction must be 2000 characters or less' }, { status: 400 });
		}
	}

	// Validate tips (optional, max 5)
	if (!Array.isArray(tipList)) {
		return json({ error: 'Tips must be an array' }, { status: 400 });
	}
	if (tipList.length > 5) {
		return json({ error: 'Maximum 5 tips allowed' }, { status: 400 });
	}
	for (const tip of tipList) {
		if (!tip.content || typeof tip.content !== 'string' || tip.content.trim().length === 0) {
			return json({ error: 'Each tip must have content' }, { status: 400 });
		}
		if (tip.content.trim().length > 500) {
			return json({ error: 'Tips must be 500 characters or less' }, { status: 400 });
		}
	}

	// Validate equipment (optional)
	if (!Array.isArray(equipmentList)) {
		return json({ error: 'Equipment must be an array' }, { status: 400 });
	}
	for (const eq of equipmentList) {
		if (!eq.name || typeof eq.name !== 'string' || eq.name.trim().length === 0) {
			return json({ error: 'Each equipment item must have a name' }, { status: 400 });
		}
		if (eq.name.trim().length > 200) {
			return json({ error: 'Equipment name must be 200 characters or less' }, { status: 400 });
		}
		if (eq.notes && typeof eq.notes === 'string' && eq.notes.trim().length > 200) {
			return json({ error: 'Equipment notes must be 200 characters or less' }, { status: 400 });
		}
	}

	// Validate photoUrl if provided (and not null)
	if (photoUrl && typeof photoUrl === 'string' && !isValidUrl(photoUrl)) {
		return json({ error: 'Invalid photo URL' }, { status: 400 });
	}

	// Sanitize all text inputs to prevent XSS
	const sanitizedTitle = sanitizeText(title);
	const sanitizedDescription = sanitizeText(description);
	const sanitizedTag = sanitizeText(tag);

	// Moderate content before updating
	const stepInstructions = stepList.map((s: any) => sanitizeText(s.instruction));
	const moderation = await moderateRecipe(sanitizedTitle, sanitizedDescription, stepInstructions);

	if (moderation.flagged) {
		return json(
			{ error: moderation.message || 'Your content has been flagged for review. Please revise and try again.' },
			{ status: 400 }
		);
	}

	try {
		// Use transaction to ensure all updates succeed or none do
		await db.transaction(async (tx) => {
			// Update recipe (keep the same slug)
			// Explicitly update search_vector for full-text search (defensive - trigger should also do this)
			await tx
				.update(recipes)
				.set({
					title: sanitizedTitle,
					description: sanitizedDescription,
					cuisine: cuisine.trim(), // Validated against whitelist, safe
					tag: sanitizedTag,
					language: language.trim(), // Validated against whitelist, safe
					difficulty: difficulty || 'medium',
					prepTimeMinutes: prepTime,
					cookTimeMinutes: cookTime,
					servings: servings || 4,
					photoUrl: photoUrl !== undefined ? photoUrl : sql`${recipes.photoUrl}`,
					searchVector: sql`
						setweight(to_tsvector('english', ${sanitizedTitle}), 'A') ||
						setweight(to_tsvector('english', ${sanitizedDescription}), 'B') ||
						setweight(to_tsvector('english', ${cuisine.trim()}), 'C') ||
						setweight(to_tsvector('english', ${sanitizedTag}), 'C')
					`,
					updatedAt: new Date()
				})
				.where(eq(recipes.id, id));

			// Delete existing ingredients, steps, tips, and equipment
			await tx.delete(ingredients).where(eq(ingredients.recipeId, id));
			await tx.delete(steps).where(eq(steps.recipeId, id));
			await tx.delete(tips).where(eq(tips.recipeId, id));
			await tx.delete(equipment).where(eq(equipment.recipeId, id));

			// Insert new ingredients (sanitize user-provided text)
			const ingredientValues = ingredientList.map((ing: any, index: number) => ({
				recipeId: id,
				ingredientKey: ing.ingredientKey.trim(),
				name: sanitizeText(ing.name),
				amount: String(parseFloat(ing.amount)),
				unit: ing.unit.trim(),
				sortOrder: index,
				notes: ing.notes ? sanitizeText(ing.notes) : null
			}));

			if (ingredientValues.length > 0) {
				await tx.insert(ingredients).values(ingredientValues);
			}

			// Insert new steps (already sanitized above)
			const stepValues = stepList.map((step: any, index: number) => ({
				recipeId: id,
				stepNumber: index + 1,
				instruction: sanitizeText(step.instruction)
			}));

			if (stepValues.length > 0) {
				await tx.insert(steps).values(stepValues);
			}

			// Insert new tips (sanitize content)
			if (tipList.length > 0) {
				const tipValues = tipList.map((tip: any, index: number) => ({
					recipeId: id,
					sortOrder: index,
					content: sanitizeText(tip.content)
				}));
				await tx.insert(tips).values(tipValues);
			}

			// Insert new equipment (sanitize name and notes)
			if (equipmentList.length > 0) {
				const equipmentValues = equipmentList.map((eq: any, index: number) => ({
					recipeId: id,
					equipmentKey: eq.equipmentKey?.trim() || null,
					name: sanitizeText(eq.name),
					notes: eq.notes ? sanitizeText(eq.notes) : null,
					sortOrder: index
				}));
				await tx.insert(equipment).values(equipmentValues);
			}
		});

		// Invalidate caches since a recipe was updated
		await Promise.all([
			cacheDeletePattern('trending:*'),
			cacheDeletePattern('discover:*'),
			cacheDeletePattern('homepage:*')
		]);

		return json({ success: true, slug: existingRecipe.slug });
	} catch (error) {
		logger.error('Failed to update recipe', error, { recipeId: id });
		return json({ error: 'Failed to update recipe', requestId: getRequestId() }, { status: 500 });
	}
};

export const DELETE: RequestHandler = async ({ params, locals }) => {
	if (!locals.user) {
		return json({ error: 'Unauthorized' }, { status: 401 });
	}

	const { id } = params;

	// Get existing recipe to verify ownership
	const [existingRecipe] = await db
		.select({
			id: recipes.id,
			authorId: recipes.authorId
		})
		.from(recipes)
		.where(eq(recipes.id, id))
		.limit(1);

	if (!existingRecipe) {
		return json({ error: 'Recipe not found' }, { status: 404 });
	}

	if (existingRecipe.authorId !== locals.user.id) {
		return json({ error: 'You can only delete your own recipes' }, { status: 403 });
	}

	try {
		// Delete recipe (ingredients, steps, votes, comments will cascade)
		await db.delete(recipes).where(eq(recipes.id, id));

		// Invalidate caches since a recipe was deleted
		await Promise.all([
			cacheDeletePattern('trending:*'),
			cacheDeletePattern('discover:*'),
			cacheDeletePattern('homepage:*')
		]);

		return json({ success: true });
	} catch (error) {
		logger.error('Failed to delete recipe', error, { recipeId: id });
		return json({ error: 'Failed to delete recipe', requestId: getRequestId() }, { status: 500 });
	}
};
