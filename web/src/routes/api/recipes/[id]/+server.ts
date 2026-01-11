import { json } from '@sveltejs/kit';
import type { RequestHandler } from './$types';
import { db } from '$lib/server/db';
import { recipes, ingredients, steps } from '$lib/server/db/schema';
import { eq } from 'drizzle-orm';
import { moderateRecipe } from '$lib/server/moderation';

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
		steps: stepList
	} = data;

	// Validate required fields
	if (typeof title !== 'string' || title.trim().length === 0) {
		return json({ error: 'Title is required' }, { status: 400 });
	}

	if (typeof description !== 'string' || description.trim().length === 0) {
		return json({ error: 'Description is required' }, { status: 400 });
	}

	if (typeof cuisine !== 'string' || cuisine.trim().length === 0) {
		return json({ error: 'Cuisine is required' }, { status: 400 });
	}

	if (typeof tag !== 'string' || tag.trim().length === 0) {
		return json({ error: 'Tag is required' }, { status: 400 });
	}

	if (typeof language !== 'string' || language.trim().length === 0) {
		return json({ error: 'Language is required' }, { status: 400 });
	}

	if (typeof prepTime !== 'number' || prepTime <= 0) {
		return json({ error: 'Prep time is required' }, { status: 400 });
	}

	if (typeof cookTime !== 'number' || cookTime <= 0) {
		return json({ error: 'Cook time is required' }, { status: 400 });
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
	}

	if (!Array.isArray(stepList) || stepList.length < 1) {
		return json({ error: 'At least 1 instruction is required' }, { status: 400 });
	}

	for (const step of stepList) {
		if (!step.instruction || typeof step.instruction !== 'string' || step.instruction.trim().length === 0) {
			return json({ error: 'All instructions must have content' }, { status: 400 });
		}
	}

	// Moderate content before updating
	const stepInstructions = stepList.map((s: any) => s.instruction);
	const moderation = await moderateRecipe(title, description, stepInstructions);

	if (moderation.flagged) {
		return json(
			{ error: moderation.message || 'Your content has been flagged for review. Please revise and try again.' },
			{ status: 400 }
		);
	}

	try {
		// Update recipe (keep the same slug)
		const updateData: Record<string, unknown> = {
			title: title.trim(),
			description: description.trim(),
			cuisine: cuisine.trim(),
			tag: tag.trim(),
			language: language.trim(),
			difficulty: difficulty || 'medium',
			prepTimeMinutes: prepTime,
			cookTimeMinutes: cookTime,
			servings: servings || 4,
			updatedAt: new Date()
		};

		// Only update photoUrl if explicitly provided (could be null to remove, or a new URL)
		if (photoUrl !== undefined) {
			updateData.photoUrl = photoUrl;
		}

		await db
			.update(recipes)
			.set(updateData)
			.where(eq(recipes.id, id));

		// Delete existing ingredients and steps
		await db.delete(ingredients).where(eq(ingredients.recipeId, id));
		await db.delete(steps).where(eq(steps.recipeId, id));

		// Insert new ingredients
		const ingredientValues = ingredientList.map((ing: any, index: number) => ({
			recipeId: id,
			ingredientKey: ing.ingredientKey.trim(),
			name: ing.name.trim(),
			amount: String(parseFloat(ing.amount)),
			unit: ing.unit.trim(),
			sortOrder: index,
			notes: ing.notes?.trim() || null
		}));

		if (ingredientValues.length > 0) {
			await db.insert(ingredients).values(ingredientValues);
		}

		// Insert new steps
		const stepValues = stepList.map((step: any, index: number) => ({
			recipeId: id,
			stepNumber: index + 1,
			instruction: step.instruction.trim()
		}));

		if (stepValues.length > 0) {
			await db.insert(steps).values(stepValues);
		}

		return json({ success: true, slug: existingRecipe.slug });
	} catch (error) {
		console.error('Failed to update recipe:', error);
		return json({ error: 'Failed to update recipe' }, { status: 500 });
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

		return json({ success: true });
	} catch (error) {
		console.error('Failed to delete recipe:', error);
		return json({ error: 'Failed to delete recipe' }, { status: 500 });
	}
};
