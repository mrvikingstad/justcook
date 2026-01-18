/**
 * Redis client configuration
 * Uses Upstash Redis for serverless-compatible distributed caching and rate limiting
 */

import { Redis } from '@upstash/redis';
import { dev } from '$app/environment';
import { env } from '$env/dynamic/private';
import { logger } from '$lib/server/logger';

/**
 * Check if Redis is configured
 */
export const isRedisConfigured = Boolean(env.UPSTASH_REDIS_REST_URL && env.UPSTASH_REDIS_REST_TOKEN);

/**
 * Redis client instance
 * Returns null if Redis is not configured (allows graceful fallback)
 */
export const redis: Redis | null = isRedisConfigured
	? new Redis({
			url: env.UPSTASH_REDIS_REST_URL!,
			token: env.UPSTASH_REDIS_REST_TOKEN!
		})
	: null;

/**
 * Log Redis configuration status on startup
 */
if (dev) {
	if (isRedisConfigured) {
		logger.info('Redis connected to Upstash');
	} else {
		logger.info('Redis not configured - using in-memory fallback');
	}
}
