/**
 * Redis caching utilities for expensive queries
 * Provides type-safe caching with automatic serialization and TTL
 */

import { redis, isRedisConfigured } from './index';
import { dev } from '$app/environment';
import { logger } from '$lib/server/logger';

/**
 * Cache configuration for different data types
 */
export const CACHE_TTL = {
	// Short-lived caches (1-2 minutes)
	trending: 60, // 1 minute - trending data changes frequently
	discover: 120, // 2 minutes - discovery feed

	// Medium-lived caches (5-15 minutes)
	chefProfile: 300, // 5 minutes - chef stats
	recipeStats: 300, // 5 minutes - vote counts, comment counts

	// Long-lived caches (1 hour+)
	categories: 3600, // 1 hour - rarely changes
	staticData: 86400 // 24 hours - cuisines, tags, etc.
} as const;

type CacheTTLKey = keyof typeof CACHE_TTL;

/**
 * In-memory cache fallback when Redis is not available
 */
interface InMemoryCacheEntry<T> {
	data: T;
	expiresAt: number;
}

const inMemoryCache = new Map<string, InMemoryCacheEntry<unknown>>();

// Cleanup expired entries every minute
if (typeof setInterval !== 'undefined') {
	setInterval(() => {
		const now = Date.now();
		for (const [key, entry] of inMemoryCache.entries()) {
			if (now > entry.expiresAt) {
				inMemoryCache.delete(key);
			}
		}
	}, 60000);
}

/**
 * Get a value from cache
 * @param key - Cache key
 * @returns Cached value or null if not found/expired
 */
export async function cacheGet<T>(key: string): Promise<T | null> {
	if (redis) {
		try {
			const value = await redis.get<T>(key);
			return value;
		} catch (error) {
			logger.debug('Redis get error', { key, error: String(error) });
			return null;
		}
	}

	// In-memory fallback
	const entry = inMemoryCache.get(key) as InMemoryCacheEntry<T> | undefined;
	if (!entry || Date.now() > entry.expiresAt) {
		inMemoryCache.delete(key);
		return null;
	}
	return entry.data;
}

/**
 * Set a value in cache
 * @param key - Cache key
 * @param value - Value to cache (must be JSON-serializable)
 * @param ttlSeconds - Time to live in seconds
 */
export async function cacheSet<T>(key: string, value: T, ttlSeconds: number): Promise<void> {
	if (redis) {
		try {
			await redis.set(key, value, { ex: ttlSeconds });
		} catch (error) {
			logger.debug('Redis set error', { key, error: String(error) });
		}
		return;
	}

	// In-memory fallback
	inMemoryCache.set(key, {
		data: value,
		expiresAt: Date.now() + ttlSeconds * 1000
	});
}

/**
 * Delete a value from cache
 * @param key - Cache key to delete
 */
export async function cacheDelete(key: string): Promise<void> {
	if (redis) {
		try {
			await redis.del(key);
		} catch (error) {
			logger.debug('Redis delete error', { key, error: String(error) });
		}
		return;
	}

	// In-memory fallback
	inMemoryCache.delete(key);
}

/**
 * Delete all keys matching a pattern
 * Uses SCAN for non-blocking iteration (safe for production at scale)
 * @param pattern - Pattern to match (e.g., "recipe:*")
 */
export async function cacheDeletePattern(pattern: string): Promise<void> {
	if (redis) {
		try {
			// Use SCAN for non-blocking iteration instead of KEYS
			// SCAN doesn't block Redis and is safe for large key sets
			const keysToDelete: string[] = [];
			let cursor = '0';

			do {
				// Scan in batches of 100 keys
				const result = await redis.scan(cursor, {
					match: pattern,
					count: 100
				});
				cursor = String(result[0]);
				keysToDelete.push(...result[1]);
			} while (cursor !== '0');

			// Delete keys in batches to avoid memory issues
			if (keysToDelete.length > 0) {
				// Delete in batches of 100 to avoid hitting limits
				for (let i = 0; i < keysToDelete.length; i += 100) {
					const batch = keysToDelete.slice(i, i + 100);
					await redis.del(...batch);
				}
			}
		} catch (error) {
			logger.debug('Redis delete pattern error', { pattern, error: String(error) });
		}
		return;
	}

	// In-memory fallback - delete matching keys
	const regex = new RegExp('^' + pattern.replace(/\*/g, '.*') + '$');
	for (const key of inMemoryCache.keys()) {
		if (regex.test(key)) {
			inMemoryCache.delete(key);
		}
	}
}

/**
 * In-memory locks for stampede prevention when Redis is not available
 */
const inMemoryLocks = new Map<string, Promise<unknown>>();

/**
 * Try to acquire a distributed lock using Redis SETNX
 * @param lockKey - Key for the lock
 * @param ttlSeconds - Lock expiry time (prevents deadlocks if holder crashes)
 * @returns true if lock acquired, false otherwise
 */
async function acquireLock(lockKey: string, ttlSeconds: number): Promise<boolean> {
	if (redis) {
		try {
			// SETNX with expiry - atomic operation
			const result = await redis.set(lockKey, '1', { nx: true, ex: ttlSeconds });
			return result === 'OK';
		} catch (error) {
			logger.debug('Redis lock acquire error', { lockKey, error: String(error) });
			return false;
		}
	}
	// In-memory fallback - check if lock exists
	return !inMemoryLocks.has(lockKey);
}

/**
 * Release a distributed lock
 * @param lockKey - Key for the lock
 */
async function releaseLock(lockKey: string): Promise<void> {
	if (redis) {
		try {
			await redis.del(lockKey);
		} catch (error) {
			logger.debug('Redis lock release error', { lockKey, error: String(error) });
		}
		return;
	}
	inMemoryLocks.delete(lockKey);
}

/**
 * Sleep for a specified duration
 */
function sleep(ms: number): Promise<void> {
	return new Promise((resolve) => setTimeout(resolve, ms));
}

/**
 * Get or set a cached value with automatic refresh and stampede protection
 * Uses distributed locking to prevent thundering herd when cache expires
 *
 * @param key - Cache key
 * @param ttlKey - TTL configuration key
 * @param factory - Async function to generate the value if not cached
 * @returns Cached or freshly generated value
 */
export async function cacheGetOrSet<T>(
	key: string,
	ttlKey: CacheTTLKey,
	factory: () => Promise<T>
): Promise<T> {
	// Try to get from cache first
	const cached = await cacheGet<T>(key);
	if (cached !== null) {
		return cached;
	}

	const lockKey = `lock:${key}`;
	const ttl = CACHE_TTL[ttlKey];

	// Try to acquire lock for cache refresh
	const lockAcquired = await acquireLock(lockKey, Math.max(ttl, 30)); // Lock for at least 30s

	if (lockAcquired) {
		try {
			// Double-check cache (another request might have populated it while we waited for lock)
			const doubleCheck = await cacheGet<T>(key);
			if (doubleCheck !== null) {
				return doubleCheck;
			}

			// We have the lock - generate fresh value
			const value = await factory();

			// Cache the result
			await cacheSet(key, value, ttl);

			return value;
		} finally {
			// Always release the lock
			await releaseLock(lockKey);
		}
	}

	// Lock not acquired - another request is refreshing the cache
	// Wait briefly and retry from cache (up to 3 times)
	for (let attempt = 0; attempt < 3; attempt++) {
		await sleep(100 * (attempt + 1)); // 100ms, 200ms, 300ms backoff

		const retryCache = await cacheGet<T>(key);
		if (retryCache !== null) {
			return retryCache;
		}
	}

	// Cache still empty after retries - execute factory as fallback
	// This ensures we don't return empty data even if locking fails
	logger.warn('Cache stampede fallback triggered', { key });
	return factory();
}

/**
 * Cache key builders for consistent key naming
 */
export const cacheKeys = {
	trending: (language?: string) => `trending:${language || 'all'}`,
	discover: (language?: string, page?: number) => `discover:${language || 'all'}:${page || 1}`,
	chefProfile: (username: string) => `chef:${username}`,
	recipeStats: (recipeId: string) => `recipe:stats:${recipeId}`,
	recipeVotes: (recipeId: string) => `recipe:votes:${recipeId}`,
	categories: () => 'categories:all'
} as const;

/**
 * Check if Redis caching is available
 */
export function isCacheAvailable(): boolean {
	return isRedisConfigured;
}
