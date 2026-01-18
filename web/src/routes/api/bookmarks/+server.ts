import { json } from '@sveltejs/kit';
import type { RequestHandler } from './$types';
import { db } from '$lib/server/db';
import { bookmarks, recipes } from '$lib/server/db/schema';
import { and, eq } from 'drizzle-orm';
import { logger, getRequestId } from '$lib/server/logger';

// GET - Fetch user's bookmarked recipe slugs
export const GET: RequestHandler = async ({ locals }) => {
	if (!locals.user) {
		return json({ bookmarks: [] });
	}

	try {
		const userBookmarks = await db
			.select({ slug: recipes.slug })
			.from(bookmarks)
			.innerJoin(recipes, eq(bookmarks.recipeId, recipes.id))
			.where(eq(bookmarks.userId, locals.user.id));

		return json({ bookmarks: userBookmarks.map((b) => b.slug) });
	} catch (error) {
		logger.error('Failed to fetch bookmarks', error, { userId: locals.user.id });
		return json({ error: 'Failed to fetch bookmarks', requestId: getRequestId() }, { status: 500 });
	}
};

// POST - Toggle bookmark for a recipe
export const POST: RequestHandler = async ({ request, locals }) => {
	if (!locals.user) {
		return json({ error: 'Unauthorized' }, { status: 401 });
	}

	const { recipeSlug } = await request.json();

	if (!recipeSlug || typeof recipeSlug !== 'string') {
		return json({ error: 'Invalid recipe slug' }, { status: 400 });
	}

	try {
		// Get recipe by slug
		const [recipe] = await db
			.select({ id: recipes.id })
			.from(recipes)
			.where(eq(recipes.slug, recipeSlug))
			.limit(1);

		if (!recipe) {
			return json({ error: 'Recipe not found' }, { status: 404 });
		}

		// Check if already bookmarked
		const [existing] = await db
			.select({ id: bookmarks.id })
			.from(bookmarks)
			.where(and(eq(bookmarks.userId, locals.user.id), eq(bookmarks.recipeId, recipe.id)))
			.limit(1);

		if (existing) {
			// Remove bookmark
			await db.delete(bookmarks).where(eq(bookmarks.id, existing.id));
			return json({ success: true, bookmarked: false });
		} else {
			// Add bookmark
			await db.insert(bookmarks).values({
				userId: locals.user.id,
				recipeId: recipe.id
			});
			return json({ success: true, bookmarked: true });
		}
	} catch (error) {
		logger.error('Failed to toggle bookmark', error, { userId: locals.user.id, recipeSlug });
		return json({ error: 'Failed to toggle bookmark', requestId: getRequestId() }, { status: 500 });
	}
};
