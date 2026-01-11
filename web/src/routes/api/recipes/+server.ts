import { json } from '@sveltejs/kit';
import type { RequestHandler } from './$types';
import { db } from '$lib/server/db';
import { recipes, ingredients, steps } from '$lib/server/db/schema';
import { eq } from 'drizzle-orm';
import { moderateRecipe } from '$lib/server/moderation';

function generateSlug(title: string): string {
	return title
		.toLowerCase()
		.trim()
		.replace(/[^\w\s-]/g, '')
		.replace(/\s+/g, '-')
		.replace(/-+/g, '-')
		.substring(0, 200);
}

async function getUniqueSlug(baseSlug: string): Promise<string> {
	let slug = baseSlug;
	let counter = 1;

	while (true) {
		const [existing] = await db
			.select({ id: recipes.id })
			.from(recipes)
			.where(eq(recipes.slug, slug))
			.limit(1);

		if (!existing) {
			return slug;
		}

		slug = `${baseSlug}-${counter}`;
		counter++;
	}
}

export const POST: RequestHandler = async ({ request, locals }) => {
	if (!locals.user) {
		return json({ error: 'Unauthorized' }, { status: 401 });
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

	// Validate each ingredient has required fields
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

	// Validate each step has content
	for (const step of stepList) {
		if (!step.instruction || typeof step.instruction !== 'string' || step.instruction.trim().length === 0) {
			return json({ error: 'All instructions must have content' }, { status: 400 });
		}
	}

	// Moderate content before publishing
	const stepInstructions = stepList.map((s: any) => s.instruction);
	const moderation = await moderateRecipe(title, description, stepInstructions);

	if (moderation.flagged) {
		return json(
			{ error: moderation.message || 'Your content has been flagged for review. Please revise and try again.' },
			{ status: 400 }
		);
	}

	try {
		// Generate unique slug
		const baseSlug = generateSlug(title);
		const slug = await getUniqueSlug(baseSlug);

		// Insert recipe
		const [recipe] = await db
			.insert(recipes)
			.values({
				authorId: locals.user.id,
				title: title.trim(),
				slug,
				description: description.trim(),
				cuisine: cuisine.trim(),
				tag: tag.trim(),
				language: language.trim(),
				difficulty: difficulty || 'medium',
				prepTimeMinutes: prepTime,
				cookTimeMinutes: cookTime,
				servings: servings || 4,
				photoUrl: photoUrl || null,
				isPublished: true,
				publishedAt: new Date()
			})
			.returning({ id: recipes.id });

		// Insert ingredients
		const ingredientValues = ingredientList.map((ing: any, index: number) => ({
			recipeId: recipe.id,
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

		// Insert steps
		const stepValues = stepList.map((step: any, index: number) => ({
			recipeId: recipe.id,
			stepNumber: index + 1,
			instruction: step.instruction.trim()
		}));

		if (stepValues.length > 0) {
			await db.insert(steps).values(stepValues);
		}

		return json({ success: true, slug });
	} catch (error) {
		console.error('Failed to create recipe:', error);
		return json({ error: 'Failed to create recipe' }, { status: 500 });
	}
};
