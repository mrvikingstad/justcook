/**
 * Startup validation for environment variables and secrets
 * This module validates all required configuration at server startup,
 * failing fast with clear error messages rather than failing later at runtime.
 */

import { DATABASE_URL, BETTER_AUTH_SECRET, GOOGLE_CLIENT_ID, GOOGLE_CLIENT_SECRET, RESEND_API_KEY } from '$env/static/private';
import { PUBLIC_APP_URL } from '$env/static/public';
import { env } from '$env/dynamic/private';
import { dev, building } from '$app/environment';

interface ValidationResult {
	valid: boolean;
	errors: string[];
	warnings: string[];
}

/**
 * Validate all environment variables and secrets
 */
function validateEnvironment(): ValidationResult {
	const errors: string[] = [];
	const warnings: string[] = [];

	// === REQUIRED SECRETS (always required) ===

	if (!DATABASE_URL) {
		errors.push('DATABASE_URL is required - database connection will fail');
	}

	if (!BETTER_AUTH_SECRET) {
		errors.push('BETTER_AUTH_SECRET is required - authentication will fail');
	} else if (BETTER_AUTH_SECRET.length < 32) {
		errors.push(
			`BETTER_AUTH_SECRET must be at least 32 characters (currently ${BETTER_AUTH_SECRET.length}). ` +
			'Generate one with: openssl rand -base64 32'
		);
	}

	// === PRODUCTION-REQUIRED SECRETS ===

	if (!dev) {
		if (!PUBLIC_APP_URL) {
			errors.push('PUBLIC_APP_URL is required in production - OAuth callbacks will fail');
		}

		if (!RESEND_API_KEY) {
			errors.push('RESEND_API_KEY is required in production - email verification/password reset will fail');
		}

		if (!GOOGLE_CLIENT_ID || !GOOGLE_CLIENT_SECRET) {
			errors.push('GOOGLE_CLIENT_ID and GOOGLE_CLIENT_SECRET are required in production - Google OAuth will fail');
		}
	}

	// === OPTIONAL SECRETS (warn if missing) ===

	// Apple OAuth (optional)
	if (!env.APPLE_CLIENT_ID || !env.APPLE_CLIENT_SECRET) {
		warnings.push('Apple OAuth not configured (APPLE_CLIENT_ID, APPLE_CLIENT_SECRET) - Apple sign-in disabled');
	}

	// R2 Storage (optional - uploads will fail without it)
	if (!env.R2_ACCOUNT_ID || !env.R2_ACCESS_KEY_ID || !env.R2_SECRET_ACCESS_KEY) {
		warnings.push('R2 storage not configured (R2_ACCOUNT_ID, R2_ACCESS_KEY_ID, R2_SECRET_ACCESS_KEY) - file uploads disabled');
	}

	// Redis (optional - falls back to in-memory)
	if (!env.UPSTASH_REDIS_REST_URL || !env.UPSTASH_REDIS_REST_TOKEN) {
		warnings.push('Redis not configured (UPSTASH_REDIS_REST_URL, UPSTASH_REDIS_REST_TOKEN) - using in-memory rate limiting/caching');
	}

	// OpenAI (optional - moderation will use fallback behavior)
	if (!env.OPENAI_API_KEY) {
		warnings.push('OpenAI not configured (OPENAI_API_KEY) - content moderation using fallback behavior');
	}

	// Sentry (optional - error tracking disabled)
	if (!env.SENTRY_DSN && !dev) {
		warnings.push('Sentry not configured (SENTRY_DSN) - error tracking disabled');
	}

	return {
		valid: errors.length === 0,
		errors,
		warnings
	};
}

/**
 * Run startup validation and throw if critical errors found
 * Called once when the server starts
 */
export function validateStartup(): void {
	// Skip validation during build
	if (building) {
		return;
	}

	const result = validateEnvironment();

	// Log warnings
	if (result.warnings.length > 0) {
		console.warn('\n⚠️  Startup warnings:');
		result.warnings.forEach(w => console.warn(`   - ${w}`));
		console.warn('');
	}

	// Throw on errors
	if (!result.valid) {
		console.error('\n❌ Startup validation failed:');
		result.errors.forEach(e => console.error(`   - ${e}`));
		console.error('\nServer cannot start with missing required configuration.\n');
		throw new Error(`Startup validation failed: ${result.errors.join('; ')}`);
	}

	// Success message in dev
	if (dev) {
		console.log('✅ Environment validation passed');
	}
}

// Run validation immediately when this module is imported
validateStartup();
