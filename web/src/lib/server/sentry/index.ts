import * as Sentry from '@sentry/sveltekit';
import { dev } from '$app/environment';
import { env } from '$env/dynamic/private';

let initialized = false;

/**
 * Initialize Sentry for error tracking
 * Only initializes in production or when SENTRY_DSN is explicitly set
 */
export function initSentry() {
	if (initialized) return;

	const dsn = env.SENTRY_DSN;

	if (!dsn) {
		if (!dev) {
			console.warn('[Sentry] SENTRY_DSN not set - error tracking disabled');
		}
		return;
	}

	Sentry.init({
		dsn,
		environment: dev ? 'development' : 'production',
		tracesSampleRate: dev ? 1.0 : 0.1, // Sample 10% of transactions in production
		// Don't send PII
		sendDefaultPii: false,
		// Filter out sensitive data
		beforeSend(event) {
			// Remove sensitive headers
			if (event.request?.headers) {
				delete event.request.headers['authorization'];
				delete event.request.headers['cookie'];
			}
			return event;
		}
	});

	initialized = true;
}

/**
 * Capture an exception and send to Sentry
 */
export function captureException(error: unknown, context?: Record<string, unknown>) {
	if (!initialized) {
		// Fallback to console if Sentry not initialized
		console.error('[Error]', error, context);
		return;
	}

	Sentry.captureException(error, {
		extra: context
	});
}

/**
 * Capture a message and send to Sentry
 */
export function captureMessage(message: string, level: 'info' | 'warning' | 'error' = 'info') {
	if (!initialized) {
		console.log(`[${level}]`, message);
		return;
	}

	Sentry.captureMessage(message, level);
}

/**
 * Set user context for Sentry
 */
export function setUser(user: { id: string; username?: string } | null) {
	if (!initialized) return;

	if (user) {
		Sentry.setUser({ id: user.id, username: user.username });
	} else {
		Sentry.setUser(null);
	}
}

/**
 * Add breadcrumb for debugging
 */
export function addBreadcrumb(breadcrumb: {
	category: string;
	message: string;
	level?: 'debug' | 'info' | 'warning' | 'error';
	data?: Record<string, unknown>;
}) {
	if (!initialized) return;

	Sentry.addBreadcrumb({
		category: breadcrumb.category,
		message: breadcrumb.message,
		level: breadcrumb.level || 'info',
		data: breadcrumb.data
	});
}

// Re-export Sentry for advanced usage
export { Sentry };
