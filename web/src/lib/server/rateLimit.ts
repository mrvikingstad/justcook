/**
 * Distributed rate limiting with Redis
 * Falls back to in-memory rate limiting when Redis is not configured
 */

import { Ratelimit } from '@upstash/ratelimit';
import { redis, isRedisConfigured } from './redis';

export interface RateLimitResult {
	allowed: boolean;
	remaining: number;
	resetIn: number;
}

/**
 * Rate limit configurations for different endpoint types
 */
export const RATE_LIMITS = {
	// General API requests
	api: { limit: 100, windowMs: 60000 }, // 100 req/min
	// Upload endpoints (more restrictive)
	upload: { limit: 20, windowMs: 60000 }, // 20 req/min
	// Voting/commenting (prevent spam)
	engagement: { limit: 30, windowMs: 60000 }, // 30 req/min
	// Strict rate limit for sensitive operations
	strict: { limit: 10, windowMs: 60000 }, // 10 req/min
	// Auth attempts (very strict)
	auth: { limit: 5, windowMs: 15 * 60 * 1000 } // 5 per 15 min
} as const;

type RateLimitType = keyof typeof RATE_LIMITS;

/**
 * Create Redis-based rate limiters for each endpoint type
 * Uses sliding window algorithm for smooth rate limiting
 */
const redisLimiters: Record<RateLimitType, Ratelimit> | null = redis
	? {
			api: new Ratelimit({
				redis,
				limiter: Ratelimit.slidingWindow(RATE_LIMITS.api.limit, '1m'),
				prefix: 'ratelimit:api',
				analytics: true
			}),
			upload: new Ratelimit({
				redis,
				limiter: Ratelimit.slidingWindow(RATE_LIMITS.upload.limit, '1m'),
				prefix: 'ratelimit:upload',
				analytics: true
			}),
			engagement: new Ratelimit({
				redis,
				limiter: Ratelimit.slidingWindow(RATE_LIMITS.engagement.limit, '1m'),
				prefix: 'ratelimit:engagement',
				analytics: true
			}),
			strict: new Ratelimit({
				redis,
				limiter: Ratelimit.slidingWindow(RATE_LIMITS.strict.limit, '1m'),
				prefix: 'ratelimit:strict',
				analytics: true
			}),
			auth: new Ratelimit({
				redis,
				limiter: Ratelimit.slidingWindow(RATE_LIMITS.auth.limit, '15m'),
				prefix: 'ratelimit:auth',
				analytics: true
			})
		}
	: null;

/**
 * In-memory fallback for when Redis is not configured
 */
interface InMemoryRecord {
	count: number;
	resetAt: number;
}

const inMemoryStore = new Map<string, InMemoryRecord>();

function inMemoryRateLimit(
	identifier: string,
	limit: number,
	windowMs: number
): RateLimitResult {
	const now = Date.now();
	const key = identifier;
	const record = inMemoryStore.get(key);

	// First request or window expired - reset counter
	if (!record || now > record.resetAt) {
		inMemoryStore.set(key, { count: 1, resetAt: now + windowMs });
		return { allowed: true, remaining: limit - 1, resetIn: windowMs };
	}

	// Window still active - check limit
	if (record.count >= limit) {
		return { allowed: false, remaining: 0, resetIn: record.resetAt - now };
	}

	// Increment counter
	record.count++;
	return { allowed: true, remaining: limit - record.count, resetIn: record.resetAt - now };
}

// Cleanup old in-memory entries every minute
if (typeof setInterval !== 'undefined') {
	setInterval(() => {
		const now = Date.now();
		for (const [key, value] of inMemoryStore.entries()) {
			if (now > value.resetAt) {
				inMemoryStore.delete(key);
			}
		}
	}, 60000);
}

/**
 * Check if a request should be rate limited (async for Redis support)
 * @param identifier - Unique identifier for the client (e.g., IP address)
 * @param type - The type of rate limit to apply
 */
export async function rateLimit(
	identifier: string,
	type: RateLimitType = 'api'
): Promise<RateLimitResult> {
	const config = RATE_LIMITS[type];

	// Use Redis if available
	if (redisLimiters) {
		const limiter = redisLimiters[type];
		const result = await limiter.limit(identifier);

		return {
			allowed: result.success,
			remaining: result.remaining,
			resetIn: result.reset - Date.now()
		};
	}

	// Fallback to in-memory
	return inMemoryRateLimit(`${type}:${identifier}`, config.limit, config.windowMs);
}

/**
 * Get the appropriate rate limit type based on the endpoint path and method
 */
export function getRateLimitType(pathname: string, method?: string): RateLimitType {
	if (pathname.startsWith('/api/upload')) {
		return 'upload';
	}
	if (
		pathname.startsWith('/api/votes') ||
		pathname.startsWith('/api/comments') ||
		pathname.startsWith('/api/bookmarks')
	) {
		return 'engagement';
	}
	if (pathname.startsWith('/api/account')) {
		return 'strict';
	}

	// Recipe operations that modify data - use strict rate limit
	// This prevents abuse of create/delete operations
	if (pathname === '/api/recipes' && method === 'POST') {
		return 'strict';
	}

	// DELETE operations on recipes are destructive - use strict rate limit
	// Matches /api/recipes/{id} pattern for DELETE requests
	if (pathname.match(/^\/api\/recipes\/[^/]+$/) && method === 'DELETE') {
		return 'strict';
	}

	// DELETE operations on comments are also destructive
	if (pathname.startsWith('/api/comments') && method === 'DELETE') {
		return 'strict';
	}

	// AI conversation deletion
	if (pathname.match(/^\/api\/ai\/conversations\/[^/]+$/) && method === 'DELETE') {
		return 'strict';
	}

	return 'api';
}

/**
 * Check if Redis is being used for rate limiting
 */
export function isUsingRedis(): boolean {
	return isRedisConfigured;
}
