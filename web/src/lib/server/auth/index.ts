import { betterAuth } from 'better-auth';
import { drizzleAdapter } from 'better-auth/adapters/drizzle';
import { magicLink, username } from 'better-auth/plugins';
import {
	BETTER_AUTH_SECRET,
	GOOGLE_CLIENT_ID,
	GOOGLE_CLIENT_SECRET
} from '$env/static/private';
import { env } from '$env/dynamic/private';
import { PUBLIC_APP_URL } from '$env/static/public';
import { dev } from '$app/environment';
import { db } from '../db';
import * as schema from '../db/schema';
import { sendMagicLinkEmail, sendPasswordResetEmail, sendVerificationEmail } from './email';

// Apple OAuth is optional - only enabled if credentials are provided
const APPLE_CLIENT_ID = env.APPLE_CLIENT_ID;
const APPLE_CLIENT_SECRET = env.APPLE_CLIENT_SECRET;

// Ensure base URL has protocol, default to localhost in dev
function getBaseURL(): string {
	if (dev) {
		return 'http://localhost:5173';
	}

	let url = PUBLIC_APP_URL || '';

	// Validate PUBLIC_APP_URL is set in production
	if (!url) {
		throw new Error('PUBLIC_APP_URL environment variable is required in production. OAuth callbacks will fail without it.');
	}

	// Add https:// if no protocol specified
	if (!url.startsWith('http://') && !url.startsWith('https://')) {
		url = `https://${url}`;
	}

	return url;
}

export const auth = betterAuth({
	secret: BETTER_AUTH_SECRET,
	baseURL: getBaseURL(),

	database: drizzleAdapter(db, {
		provider: 'pg',
		schema: {
			user: schema.user,
			session: schema.session,
			account: schema.account,
			verification: schema.verification
		}
	}),

	emailAndPassword: {
		enabled: true,
		requireEmailVerification: true,
		sendResetPassword: async ({ user, url }) => {
			await sendPasswordResetEmail(user.email, url);
		}
	},

	emailVerification: {
		sendVerificationEmail: async ({ user, url }) => {
			await sendVerificationEmail(user.email, url);
		},
		sendOnSignUp: true,
		autoSignInAfterVerification: true
	},

	socialProviders: {
		google: {
			clientId: GOOGLE_CLIENT_ID,
			clientSecret: GOOGLE_CLIENT_SECRET
		},
		...(APPLE_CLIENT_ID && APPLE_CLIENT_SECRET
			? {
					apple: {
						clientId: APPLE_CLIENT_ID,
						clientSecret: APPLE_CLIENT_SECRET
					}
				}
			: {})
	},

	plugins: [
		magicLink({
			sendMagicLink: async ({ email, url }) => {
				await sendMagicLinkEmail(email, url);
			}
		}),
		username()
	],

	session: {
		expiresIn: 60 * 60 * 24 * 14, // 14 days (reduced from 30 for security)
		updateAge: 60 * 60 * 24 // Update session every 24 hours
	},

	account: {
		accountLinking: {
			enabled: true,
			trustedProviders: APPLE_CLIENT_ID && APPLE_CLIENT_SECRET
				? ['google', 'apple']
				: ['google']
		}
	}
});

export type Session = typeof auth.$Infer.Session;
