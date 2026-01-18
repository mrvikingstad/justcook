import type { Handle } from '@sveltejs/kit';
import { building, dev } from '$app/environment';

// Validate environment variables at startup (before other imports that depend on them)
import '$lib/server/startup';

import { auth } from '$lib/server/auth';
import { svelteKitHandler } from 'better-auth/svelte-kit';
import { rateLimit, getRateLimitType } from '$lib/server/rateLimit';
import { logger, requestContext, generateRequestId, getRequestId } from '$lib/server/logger';
import { auditAuthFailure } from '$lib/server/logger/audit';
import { initSentry, captureException, setUser } from '$lib/server/sentry';

// Initialize Sentry on server startup
initSentry();

/**
 * Request body size limits (in bytes)
 * Protects against DoS attacks via large payloads
 */
const BODY_SIZE_LIMITS = {
	upload: 10 * 1024 * 1024,  // 10MB for file uploads (includes multipart overhead)
	api: 1 * 1024 * 1024,      // 1MB for regular API requests
	auth: 100 * 1024           // 100KB for auth requests (login, register, etc.)
} as const;

/**
 * Get the appropriate body size limit for a request
 */
function getBodySizeLimit(pathname: string): number {
	if (pathname.startsWith('/api/upload')) {
		return BODY_SIZE_LIMITS.upload;
	}
	if (pathname.startsWith('/api/auth')) {
		return BODY_SIZE_LIMITS.auth;
	}
	return BODY_SIZE_LIMITS.api;
}

/**
 * Content Security Policy
 * Protects against XSS and other injection attacks
 */
const cspDirectives = [
	"default-src 'self'",
	"script-src 'self' 'unsafe-inline'", // SvelteKit uses inline scripts for hydration
	"style-src 'self' 'unsafe-inline' https://fonts.googleapis.com", // Svelte uses inline styles + Google Fonts
	"img-src 'self' https: data: blob:", // Allow images from HTTPS sources, data URIs, and blobs
	"font-src 'self' https://fonts.gstatic.com", // Google Fonts served from gstatic
	"connect-src 'self'", // API calls
	"frame-src 'none'",
	"object-src 'none'",
	"base-uri 'self'",
	"form-action 'self'",
	"frame-ancestors 'none'",
	"upgrade-insecure-requests"
].join('; ');

/**
 * Security headers applied to all responses
 */
const securityHeaders: Record<string, string> = {
	'X-Frame-Options': 'DENY',
	'X-Content-Type-Options': 'nosniff',
	'Referrer-Policy': 'strict-origin-when-cross-origin',
	'Permissions-Policy': 'camera=(), microphone=(), geolocation=()',
	'X-XSS-Protection': '1; mode=block',
	'Content-Security-Policy': cspDirectives
};

/**
 * Add security headers to a response
 */
function addSecurityHeaders(response: Response): Response {
	const newHeaders = new Headers(response.headers);
	for (const [key, value] of Object.entries(securityHeaders)) {
		if (!newHeaders.has(key)) {
			newHeaders.set(key, value);
		}
	}
	// Add HSTS only in production
	if (!dev) {
		newHeaders.set('Strict-Transport-Security', 'max-age=31536000; includeSubDomains');
	}
	return new Response(response.body, {
		status: response.status,
		statusText: response.statusText,
		headers: newHeaders
	});
}

/**
 * Validate Origin header for CSRF protection
 * Returns true if the request is safe (same-origin or safe method)
 *
 * SECURITY: All state-changing requests MUST have a valid Origin or Referer header,
 * OR be from a trusted non-browser client (identified by X-Requested-With header).
 */
function validateOrigin(event: Parameters<Handle>[0]['event']): boolean {
	const { request, url } = event;

	// Safe methods don't need CSRF protection
	if (['GET', 'HEAD', 'OPTIONS'].includes(request.method)) {
		return true;
	}

	const origin = request.headers.get('origin');
	const host = url.host;

	// Check for valid Origin header first
	if (origin) {
		try {
			const originUrl = new URL(origin);
			return originUrl.host === host;
		} catch {
			return false;
		}
	}

	// Fallback to Referer header
	const referer = request.headers.get('referer');
	if (referer) {
		try {
			const refererUrl = new URL(referer);
			return refererUrl.host === host;
		} catch {
			return false;
		}
	}

	// No Origin or Referer header - this could be:
	// 1. A malicious cross-origin request (browsers always send Origin for cross-origin)
	// 2. A same-origin fetch with redirect: 'manual' (rare edge case)
	// 3. A non-browser client (mobile app, API client, curl, etc.)
	//
	// For non-browser clients (like the Android app), allow requests that include
	// a custom header that browsers can't set in cross-origin requests.
	// The X-Requested-With header is protected by CORS preflight.
	const requestedWith = request.headers.get('x-requested-with');
	if (requestedWith === 'JustCookApp') {
		return true;
	}

	// Reject all other requests without Origin/Referer
	// This protects against CSRF attacks while allowing legitimate API clients
	return false;
}

/**
 * Get client IP with support for proxies (Cloudflare, Railway, etc.)
 */
function getClientIP(event: Parameters<Handle>[0]['event']): string {
	// Check common proxy headers in order of preference
	const cfConnectingIP = event.request.headers.get('cf-connecting-ip');
	if (cfConnectingIP) return cfConnectingIP;

	const xRealIP = event.request.headers.get('x-real-ip');
	if (xRealIP) return xRealIP;

	const xForwardedFor = event.request.headers.get('x-forwarded-for');
	if (xForwardedFor) {
		// x-forwarded-for can be a comma-separated list; use the first IP
		return xForwardedFor.split(',')[0].trim();
	}

	// Fallback to SvelteKit's getClientAddress
	return event.getClientAddress();
}

export const handle: Handle = async ({ event, resolve }) => {
	// Generate unique request ID for correlation
	const requestId = generateRequestId();

	// Run the entire request handling within the request context
	return requestContext.run({ requestId }, async () => {
		const startTime = Date.now();
		const { pathname } = event.url;
		const method = event.request.method;

		// Add request ID to response headers for debugging
		const addRequestId = (response: Response): Response => {
			const newHeaders = new Headers(response.headers);
			newHeaders.set('X-Request-ID', requestId);
			return new Response(response.body, {
				status: response.status,
				statusText: response.statusText,
				headers: newHeaders
			});
		};

		try {
			// CSRF protection for state-changing requests
			if (!validateOrigin(event)) {
				logger.warn('CSRF validation failed', {
					path: pathname,
					method,
					ip: getClientIP(event)
				});
				return addRequestId(addSecurityHeaders(
					new Response(JSON.stringify({ error: 'Invalid origin' }), {
						status: 403,
						headers: { 'Content-Type': 'application/json' }
					})
				));
			}

			// Request body size limit check (DoS protection)
			// Only check for requests that may have a body
			if (!['GET', 'HEAD', 'OPTIONS'].includes(method)) {
				const contentLength = event.request.headers.get('content-length');
				if (contentLength) {
					const bodySize = parseInt(contentLength, 10);
					const limit = getBodySizeLimit(pathname);
					if (!isNaN(bodySize) && bodySize > limit) {
						logger.warn('Request body too large', {
							path: pathname,
							method,
							bodySize,
							limit,
							ip: getClientIP(event)
						});
						return addRequestId(addSecurityHeaders(
							new Response(JSON.stringify({
								error: 'Request body too large',
								maxSize: limit
							}), {
								status: 413,
								headers: { 'Content-Type': 'application/json' }
							})
						));
					}
				}
			}

			// Handle BetterAuth API routes with rate limiting for auth
			if (pathname.startsWith('/api/auth')) {
				const ip = getClientIP(event);

				// Stricter rate limiting for login/signup attempts
				const isAuthAttempt =
					pathname.includes('/sign-in') ||
					pathname.includes('/sign-up') ||
					pathname.includes('/forgot-password') ||
					pathname.includes('/reset-password');

				if (isAuthAttempt && method === 'POST') {
					// Use Redis-backed auth rate limiting
					const { allowed, resetIn } = await rateLimit(`auth:${ip}`, 'auth');
					if (!allowed) {
						// Audit log the rate limit event for security monitoring
						auditAuthFailure('auth.login.rate_limited', {
							ip,
							requestId,
							userAgent: event.request.headers.get('user-agent') || undefined,
							metadata: {
								path: pathname,
								resetInSeconds: Math.ceil(resetIn / 1000)
							}
						});

						return addRequestId(addSecurityHeaders(
							new Response(
								JSON.stringify({ error: 'Too many authentication attempts. Please try again later.' }),
								{
									status: 429,
									headers: {
										'Content-Type': 'application/json',
										'Retry-After': String(Math.ceil(resetIn / 1000))
									}
								}
							)
						));
					}
				}

				const response = await svelteKitHandler({ event, resolve, auth, building });
				return addRequestId(addSecurityHeaders(response));
			}

			// Rate limit API endpoints
			if (pathname.startsWith('/api/')) {
				const ip = getClientIP(event);
				const limitType = getRateLimitType(pathname, method);
				const { allowed, resetIn } = await rateLimit(ip, limitType);

				if (!allowed) {
					logger.warn('API rate limit exceeded', { ip, path: pathname, limitType });
					return addRequestId(addSecurityHeaders(
						new Response(
							JSON.stringify({ error: 'Too many requests. Please try again later.' }),
							{
								status: 429,
								headers: {
									'Content-Type': 'application/json',
									'X-RateLimit-Remaining': '0',
									'X-RateLimit-Reset': String(Math.ceil(resetIn / 1000)),
									'Retry-After': String(Math.ceil(resetIn / 1000))
								}
							}
						)
					));
				}
			}

			// Get session for all other routes
			const session = await auth.api.getSession({
				headers: event.request.headers
			});

			event.locals.user = session?.user ?? null;
			event.locals.session = session?.session ?? null;

			// Set user context for Sentry
			if (session?.user) {
				setUser({ id: session.user.id, username: session.user.username ?? undefined });
			}

			const response = await resolve(event);
			const duration = Date.now() - startTime;

			// Log API requests (skip static assets and health checks)
			if (pathname.startsWith('/api/') && pathname !== '/api/health') {
				logger.info('Request completed', {
					method,
					path: pathname,
					status: response.status,
					duration,
					userId: session?.user?.id
				});
			}

			return addRequestId(addSecurityHeaders(response));
		} catch (error) {
			const duration = Date.now() - startTime;

			// Log and capture the error
			logger.error('Request failed', error, {
				method,
				path: pathname,
				duration
			});
			captureException(error, { requestId, method, path: pathname });

			// Return a generic error response
			return addRequestId(addSecurityHeaders(
				new Response(JSON.stringify({ error: 'Internal server error', requestId }), {
					status: 500,
					headers: { 'Content-Type': 'application/json' }
				})
			));
		}
	});
};
