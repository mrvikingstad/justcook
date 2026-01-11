import { json, error } from '@sveltejs/kit';
import type { RequestHandler } from './$types';
import { auth } from '$lib/server/auth';

export const PUT: RequestHandler = async ({ request, locals, cookies }) => {
	if (!locals.user) {
		throw error(401, 'Not authenticated');
	}

	const { currentPassword, newPassword } = await request.json();

	if (!currentPassword || typeof currentPassword !== 'string') {
		throw error(400, 'Current password is required');
	}

	if (!newPassword || typeof newPassword !== 'string') {
		throw error(400, 'New password is required');
	}

	if (newPassword.length < 8) {
		throw error(400, 'New password must be at least 8 characters');
	}

	if (newPassword.length > 128) {
		throw error(400, 'New password is too long');
	}

	try {
		// Use Better Auth's changePassword API
		const result = await auth.api.changePassword({
			body: {
				currentPassword,
				newPassword
			},
			headers: {
				cookie: cookies.get('better-auth.session_token')
					? `better-auth.session_token=${cookies.get('better-auth.session_token')}`
					: ''
			}
		});

		if (!result) {
			throw error(400, 'Failed to change password');
		}

		return json({ success: true });
	} catch (err: unknown) {
		const message = err instanceof Error ? err.message : 'Failed to change password';
		// Check for common error messages
		if (message.includes('incorrect') || message.includes('invalid')) {
			throw error(400, 'Current password is incorrect');
		}
		throw error(400, message);
	}
};
