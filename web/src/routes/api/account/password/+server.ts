import { json, error } from '@sveltejs/kit';
import type { RequestHandler } from './$types';
import { auth } from '$lib/server/auth';
import { db } from '$lib/server/db';
import { session } from '$lib/server/db/schema';
import { and, eq, ne } from 'drizzle-orm';
import { logger, getRequestId } from '$lib/server/logger';
import { auditAuthSuccess, auditAuthFailure, auditSession } from '$lib/server/logger/audit';

function validatePasswordComplexity(password: string): { valid: boolean; message?: string } {
	if (password.length < 8) {
		return { valid: false, message: 'Password must be at least 8 characters' };
	}
	if (password.length > 128) {
		return { valid: false, message: 'Password is too long' };
	}
	if (!/[a-z]/.test(password)) {
		return { valid: false, message: 'Password must contain at least one lowercase letter' };
	}
	if (!/[A-Z]/.test(password)) {
		return { valid: false, message: 'Password must contain at least one uppercase letter' };
	}
	if (!/[0-9]/.test(password)) {
		return { valid: false, message: 'Password must contain at least one number' };
	}
	return { valid: true };
}

export const PUT: RequestHandler = async ({ request, locals, cookies, getClientAddress }) => {
	if (!locals.user) {
		throw error(401, 'Not authenticated');
	}

	const { currentPassword, newPassword } = await request.json();

	// Get client IP for audit logging
	const ip = request.headers.get('cf-connecting-ip') ||
		request.headers.get('x-real-ip') ||
		request.headers.get('x-forwarded-for')?.split(',')[0]?.trim() ||
		getClientAddress();

	const auditContext = {
		userId: locals.user.id,
		email: locals.user.email,
		ip,
		requestId: getRequestId(),
		userAgent: request.headers.get('user-agent') || undefined
	};

	if (!currentPassword || typeof currentPassword !== 'string') {
		throw error(400, 'Current password is required');
	}

	if (!newPassword || typeof newPassword !== 'string') {
		throw error(400, 'New password is required');
	}

	const complexity = validatePasswordComplexity(newPassword);
	if (!complexity.valid) {
		throw error(400, complexity.message!);
	}

	try {
		// Get current session token to preserve it
		const currentSessionToken = cookies.get('better-auth.session_token');

		// Use Better Auth's changePassword API
		const result = await auth.api.changePassword({
			body: {
				currentPassword,
				newPassword
			},
			headers: {
				cookie: currentSessionToken
					? `better-auth.session_token=${currentSessionToken}`
					: ''
			}
		});

		if (!result) {
			auditAuthFailure('auth.password.changed', {
				...auditContext,
				metadata: { reason: 'change_password_api_failed' }
			});
			throw error(400, 'Failed to change password');
		}

		// Audit successful password change
		auditAuthSuccess('auth.password.changed', auditContext);

		// Invalidate all other sessions for security
		// Keep the current session so the user doesn't get logged out
		if (currentSessionToken) {
			try {
				const deletedSessions = await db
					.delete(session)
					.where(
						and(
							eq(session.userId, locals.user.id),
							ne(session.token, currentSessionToken)
						)
					)
					.returning({ id: session.id });

				if (deletedSessions.length > 0) {
					// Audit session invalidation
					auditSession('session.invalidated_all', {
						...auditContext,
						sessionsAffected: deletedSessions.length,
						metadata: { reason: 'password_change' }
					});
				}
			} catch (sessionError) {
				// Log but don't fail the password change if session cleanup fails
				logger.error('Failed to invalidate sessions after password change', sessionError, {
					userId: locals.user.id
				});
			}
		}

		return json({ success: true });
	} catch (err: unknown) {
		const message = err instanceof Error ? err.message : 'Failed to change password';
		// Check for common error messages
		if (message.includes('incorrect') || message.includes('invalid')) {
			// Audit failed password verification
			auditAuthFailure('auth.password.changed', {
				...auditContext,
				metadata: { reason: 'incorrect_current_password' }
			});
			throw error(400, 'Current password is incorrect');
		}
		throw error(400, message);
	}
};
