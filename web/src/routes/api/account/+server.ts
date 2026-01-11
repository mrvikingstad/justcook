import { json, error } from '@sveltejs/kit';
import type { RequestHandler } from './$types';
import { db } from '$lib/server/db';
import { user, session, account, follows, recipes, votes, comments } from '$lib/server/db/schema';
import { eq, or } from 'drizzle-orm';

export const DELETE: RequestHandler = async ({ request, locals, cookies }) => {
	if (!locals.user) {
		throw error(401, 'Not authenticated');
	}

	const { confirmation } = await request.json();

	// Require typing "DELETE" to confirm
	if (confirmation !== 'DELETE') {
		throw error(400, 'Please type DELETE to confirm account deletion');
	}

	const userId = locals.user.id;

	try {
		// Delete in order to respect foreign key constraints
		// Note: Many of these have ON DELETE CASCADE, but being explicit is safer

		// 1. Delete user's votes
		await db.delete(votes).where(eq(votes.userId, userId));

		// 2. Delete user's comments
		await db.delete(comments).where(eq(comments.userId, userId));

		// 3. Delete follows (both directions)
		await db.delete(follows).where(
			or(
				eq(follows.followerId, userId),
				eq(follows.followingId, userId)
			)
		);

		// 4. Delete votes on user's recipes (before deleting recipes)
		const userRecipes = await db
			.select({ id: recipes.id })
			.from(recipes)
			.where(eq(recipes.authorId, userId));

		for (const recipe of userRecipes) {
			await db.delete(votes).where(eq(votes.recipeId, recipe.id));
			await db.delete(comments).where(eq(comments.recipeId, recipe.id));
		}

		// 5. Delete user's recipes (cascades to ingredients, steps)
		await db.delete(recipes).where(eq(recipes.authorId, userId));

		// 6. Delete sessions
		await db.delete(session).where(eq(session.userId, userId));

		// 7. Delete accounts (OAuth links, credentials)
		await db.delete(account).where(eq(account.userId, userId));

		// 8. Finally, delete the user
		await db.delete(user).where(eq(user.id, userId));

		// Clear session cookie
		cookies.delete('better-auth.session_token', { path: '/' });

		return json({ success: true });
	} catch (err) {
		console.error('Account deletion error:', err);
		throw error(500, 'Failed to delete account. Please try again.');
	}
};
