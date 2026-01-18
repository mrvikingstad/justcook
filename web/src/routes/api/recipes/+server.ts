import { json } from '@sveltejs/kit';
import type { RequestHandler } from './$types';
import { db } from '$lib/server/db';
import { recipes, ingredients, steps, tips, equipment, user, votes } from '$lib/server/db/schema';
import { eq, and, ilike, sql, desc, or } from 'drizzle-orm';
import { moderateRecipe, queueForReview } from '$lib/server/moderation';
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

const MIN_SEARCH_LENGTH = 2; // Minimum characters for full-text search
const MAX_SEARCH_LENGTH = 500; // Maximum characters for search query

export const GET: RequestHandler = async ({ url }) => {
	// Limit search query length to prevent abuse
	const query = (url.searchParams.get('q') || '').slice(0, MAX_SEARCH_LENGTH);
	const cuisine = url.searchParams.get('cuisine');
	const difficulty = url.searchParams.get('difficulty');
	const tag = url.searchParams.get('tag');
	const lang = url.searchParams.get('lang') || 'en';
	const page = parseInt(url.searchParams.get('page') || '1');
	const limit = Math.min(parseInt(url.searchParams.get('limit') || '20'), 50);
	const offset = (page - 1) * limit;

	// Build where conditions
	const conditions = [eq(recipes.isPublished, true)];

	// Use full-text search for queries with sufficient length
	if (query && query.length >= MIN_SEARCH_LENGTH) {
		// Full-text search using GIN-indexed search_vector
		conditions.push(
			sql`${recipes.searchVector} @@ plainto_tsquery('english', ${query})`
		);
	} else if (query && query.length > 0) {
		// For short queries, use prefix matching on title only (can use index)
		conditions.push(ilike(recipes.title, `${query}%`));
	}

	if (cuisine) {
		conditions.push(eq(recipes.cuisine, cuisine));
	}

	if (difficulty) {
		conditions.push(eq(recipes.difficulty, difficulty));
	}

	if (tag) {
		conditions.push(eq(recipes.tag, tag));
	}

	if (lang) {
		conditions.push(eq(recipes.language, lang));
	}

	// Get recipes with vote counts and author info in a single query (fixes N+1)
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
			authorUsername: user.username,
			authorFullName: user.fullName,
			authorName: user.name,
			publishedAt: recipes.publishedAt,
			upvotes: sql<number>`COALESCE(SUM(CASE WHEN ${votes.value} > 0 THEN 1 ELSE 0 END), 0)::int`,
			downvotes: sql<number>`COALESCE(SUM(CASE WHEN ${votes.value} < 0 THEN 1 ELSE 0 END), 0)::int`
		})
		.from(recipes)
		.leftJoin(votes, eq(recipes.id, votes.recipeId))
		.leftJoin(user, eq(recipes.authorId, user.id))
		.where(and(...conditions))
		.groupBy(recipes.id, user.id, user.username, user.fullName, user.name)
		.orderBy(desc(recipes.publishedAt))
		.limit(limit)
		.offset(offset);

	return json({
		recipes: recipeResults.map((r) => ({
			slug: r.slug,
			title: r.title,
			description: r.description,
			photoUrl: r.photoUrl,
			authorName: r.authorFullName || r.authorName || 'Unknown',
			authorUsername: r.authorUsername || '',
			cuisine: r.cuisine,
			tag: r.tag,
			difficulty: r.difficulty,
			prepTimeMinutes: r.prepTimeMinutes,
			cookTimeMinutes: r.cookTimeMinutes,
			upvotes: r.upvotes,
			downvotes: r.downvotes,
			publishedAt: r.publishedAt
		})),
		page,
		limit
	});
};

function generateSlug(title: string): string {
	return title
		.toLowerCase()
		.trim()
		.replace(/[^\w\s-]/g, '')
		.replace(/\s+/g, '-')
		.replace(/-+/g, '-')
		.substring(0, 200);
}

async function getUniqueSlugInTransaction(
	tx: Parameters<Parameters<typeof db.transaction>[0]>[0],
	baseSlug: string
): Promise<string> {
	let slug = baseSlug;
	let counter = 1;

	while (true) {
		const [existing] = await tx
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

	const userId = locals.user.id;
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
		if (ing.notes && typeof ing.notes === 'string' && ing.notes.trim().length > 200) {
			return json({ error: 'Ingredient notes must be 200 characters or less' }, { status: 400 });
		}
	}

	if (!Array.isArray(stepList) || stepList.length < 1) {
		return json({ error: 'At least 1 instruction is required' }, { status: 400 });
	}

	// Validate each step has content and reasonable length
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

	// Validate photoUrl if provided
	if (photoUrl && typeof photoUrl === 'string' && !isValidUrl(photoUrl)) {
		return json({ error: 'Invalid photo URL' }, { status: 400 });
	}

	// Sanitize all text inputs to prevent XSS
	const sanitizedTitle = sanitizeText(title);
	const sanitizedDescription = sanitizeText(description);
	const sanitizedTag = sanitizeText(tag);

	// Moderate content before publishing
	const stepInstructions = stepList.map((s: any) => sanitizeText(s.instruction));
	const moderation = await moderateRecipe(sanitizedTitle, sanitizedDescription, stepInstructions, undefined, userId);

	if (moderation.flagged) {
		return json(
			{ error: moderation.message || 'Your content has been flagged for review. Please revise and try again.' },
			{ status: 400 }
		);
	}

	const MAX_SLUG_RETRIES = 3;
	let lastError: unknown;

	for (let attempt = 0; attempt < MAX_SLUG_RETRIES; attempt++) {
		try {
			// Use transaction to ensure all inserts succeed or none do
			// Slug generation is inside transaction to prevent race conditions
			const result = await db.transaction(async (tx) => {
				// Generate unique slug inside transaction
				const baseSlug = generateSlug(sanitizedTitle);
				const slug = await getUniqueSlugInTransaction(tx, baseSlug);

				// Insert recipe
				const [recipe] = await tx
					.insert(recipes)
					.values({
						authorId: userId,
						title: sanitizedTitle,
						slug,
						description: sanitizedDescription,
						cuisine: cuisine.trim(), // Validated against whitelist, safe
						tag: sanitizedTag,
						language: language.trim(), // Validated against whitelist, safe
						difficulty: difficulty || 'medium',
						prepTimeMinutes: prepTime,
						cookTimeMinutes: cookTime,
						servings: servings || 4,
						photoUrl: photoUrl || null,
						isPublished: true,
						publishedAt: new Date()
					})
					.returning({ id: recipes.id });

				// Insert ingredients (sanitize user-provided text)
				const ingredientValues = ingredientList.map((ing: any, index: number) => ({
					recipeId: recipe.id,
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

				// Insert steps (already sanitized above)
				const stepValues = stepList.map((step: any, index: number) => ({
					recipeId: recipe.id,
					stepNumber: index + 1,
					instruction: sanitizeText(step.instruction)
				}));

				if (stepValues.length > 0) {
					await tx.insert(steps).values(stepValues);
				}

				// Insert tips (sanitize content)
				if (tipList.length > 0) {
					const tipValues = tipList.map((tip: any, index: number) => ({
						recipeId: recipe.id,
						sortOrder: index,
						content: sanitizeText(tip.content)
					}));
					await tx.insert(tips).values(tipValues);
				}

				// Insert equipment (sanitize name and notes)
				if (equipmentList.length > 0) {
					const equipmentValues = equipmentList.map((eq: any, index: number) => ({
						recipeId: recipe.id,
						equipmentKey: eq.equipmentKey?.trim() || null,
						name: sanitizeText(eq.name),
						notes: eq.notes ? sanitizeText(eq.notes) : null,
						sortOrder: index
					}));
					await tx.insert(equipment).values(equipmentValues);
				}

				return { recipeId: recipe.id, slug };
			});

			// Queue for manual review if moderation couldn't complete (outside transaction)
			if (moderation.needsReview && moderation.reviewReason) {
				await queueForReview('recipe', result.recipeId, moderation.reviewReason);
			}

			// Invalidate caches since a new recipe was published
			await Promise.all([
				cacheDeletePattern('trending:*'),
				cacheDeletePattern('discover:*'),
				cacheDeletePattern('homepage:*')
			]);

			return json({ success: true, slug: result.slug });
		} catch (error) {
			lastError = error;
			// Check if this is a unique constraint violation on slug
			const errorMessage = String(error);
			if (errorMessage.includes('unique') && errorMessage.includes('slug')) {
				// Retry with a new slug
				continue;
			}
			// For other errors, don't retry
			break;
		}
	}

	// Log full error details for debugging
	const errorMessage = lastError instanceof Error ? lastError.message : String(lastError);
	const errorStack = lastError instanceof Error ? lastError.stack : undefined;
	logger.error('Failed to create recipe', lastError, {
		errorMessage,
		errorStack,
		userId,
		title: sanitizedTitle
	});
	return json({ error: 'Failed to create recipe', requestId: getRequestId(), debug: errorMessage }, { status: 500 });
};
