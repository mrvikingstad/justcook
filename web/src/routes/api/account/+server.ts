import { json, error } from '@sveltejs/kit';
import type { RequestHandler } from './$types';
import { db } from '$lib/server/db';
import { user, session, account, follows, recipes, votes, comments } from '$lib/server/db/schema';
import { eq, or, inArray, and } from 'drizzle-orm';
import { logger, getRequestId } from '$lib/server/logger';
import { auditAccount, auditAuthFailure } from '$lib/server/logger/audit';
import { auth } from '$lib/server/auth';

// Check if user has a password account (for UI to show correct deletion form)
export const GET: RequestHandler = async ({ locals }) => {
	if (!locals.user) {
		throw error(401, 'Not authenticated');
	}

	const credentialAccount = await db
		.select({ id: account.id })
		.from(account)
		.where(and(eq(account.userId, locals.user.id), eq(account.providerId, 'credential')))
		.limit(1);

	return json({ hasPassword: credentialAccount.length > 0 });
};

export const DELETE: RequestHandler = async ({ request, locals, cookies, getClientAddress }) => {
	if (!locals.user) {
		throw error(401, 'Not authenticated');
	}

	const userId = locals.user.id;
	const userEmail = locals.user.email;
	const { confirmation, password } = await request.json();

	// Get client IP for audit logging
	const ip = request.headers.get('cf-connecting-ip') ||
		request.headers.get('x-real-ip') ||
		request.headers.get('x-forwarded-for')?.split(',')[0]?.trim() ||
		getClientAddress();

	const auditContext = {
		userId,
		email: userEmail,
		ip,
		requestId: getRequestId(),
		userAgent: request.headers.get('user-agent') || undefined
	};

	// Check if user has a credential (password) account
	const credentialAccount = await db
		.select({ id: account.id })
		.from(account)
		.where(and(eq(account.userId, userId), eq(account.providerId, 'credential')))
		.limit(1);

	const hasPasswordAccount = credentialAccount.length > 0;

	if (hasPasswordAccount) {
		// For password accounts, require password verification
		if (!password || typeof password !== 'string') {
			throw error(400, 'Password is required to delete your account');
		}

		// Verify password by attempting to sign in
		try {
			const signInResult = await auth.api.signInEmail({
				body: {
					email: userEmail,
					password
				}
			});
			if (!signInResult) {
				// Audit failed password verification for deletion attempt
				auditAuthFailure('auth.login.failed', {
					...auditContext,
					metadata: { reason: 'invalid_password_for_deletion' }
				});
				throw error(400, 'Incorrect password');
			}
		} catch (err) {
			// Audit failed password verification for deletion attempt
			auditAuthFailure('auth.login.failed', {
				...auditContext,
				metadata: { reason: 'invalid_password_for_deletion' }
			});
			throw error(400, 'Incorrect password');
		}
	} else {
		// For OAuth-only accounts, require typing "DELETE" to confirm
		if (confirmation !== 'DELETE') {
			throw error(400, 'Please type DELETE to confirm account deletion');
		}
	}

	try {
		// Use a transaction to ensure all-or-nothing deletion
		await db.transaction(async (tx) => {
			// Delete in order to respect foreign key constraints
			// Note: Many of these have ON DELETE CASCADE, but being explicit is safer

			// 1. Delete user's votes
			await tx.delete(votes).where(eq(votes.userId, userId));

			// 2. Delete user's comments
			await tx.delete(comments).where(eq(comments.userId, userId));

			// 3. Delete follows (both directions)
			await tx.delete(follows).where(
				or(
					eq(follows.followerId, userId),
					eq(follows.followingId, userId)
				)
			);

			// 4. Delete votes and comments on user's recipes (before deleting recipes)
			// Using subquery to avoid N+1 queries
			const userRecipeIds = tx
				.select({ id: recipes.id })
				.from(recipes)
				.where(eq(recipes.authorId, userId));

			await tx.delete(votes).where(inArray(votes.recipeId, userRecipeIds));
			await tx.delete(comments).where(inArray(comments.recipeId, userRecipeIds));

			// 5. Delete user's recipes (cascades to ingredients, steps)
			await tx.delete(recipes).where(eq(recipes.authorId, userId));

			// 6. Delete sessions
			await tx.delete(session).where(eq(session.userId, userId));

			// 7. Delete accounts (OAuth links, credentials)
			await tx.delete(account).where(eq(account.userId, userId));

			// 8. Finally, delete the user
			await tx.delete(user).where(eq(user.id, userId));
		});

		// Clear session cookie (outside transaction - not a DB operation)
		cookies.delete('better-auth.session_token', { path: '/' });

		// Audit successful account deletion
		auditAccount('account.deleted', 'success', auditContext);

		return json({ success: true });
	} catch (err) {
		// Audit failed account deletion
		auditAccount('account.deleted', 'failure', {
			...auditContext,
			metadata: { error: err instanceof Error ? err.message : 'Unknown error' }
		});
		logger.error('Account deletion failed', err, { userId });
		throw error(500, 'Failed to delete account. Please try again.');
	}
};
