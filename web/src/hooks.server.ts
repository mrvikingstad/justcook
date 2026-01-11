import type { Handle } from '@sveltejs/kit';
import { building } from '$app/environment';
import { auth } from '$lib/server/auth';
import { svelteKitHandler } from 'better-auth/svelte-kit';

export const handle: Handle = async ({ event, resolve }) => {
	// Handle BetterAuth API routes
	if (event.url.pathname.startsWith('/api/auth')) {
		console.log('[Hooks] Auth request:', event.request.method, event.url.pathname);
		const response = await svelteKitHandler({ event, resolve, auth, building });
		console.log('[Hooks] Auth response status:', response.status);
		return response;
	}

	// Get session for all other routes
	const session = await auth.api.getSession({
		headers: event.request.headers
	});

	event.locals.user = session?.user ?? null;
	event.locals.session = session?.session ?? null;

	return resolve(event);
};
