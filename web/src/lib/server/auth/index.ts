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
import { db } from '../db';
import * as schema from '../db/schema';
import { sendMagicLinkEmail, sendPasswordResetEmail, sendVerificationEmail } from './email';

// Apple OAuth is optional - only enabled if credentials are provided
const APPLE_CLIENT_ID = env.APPLE_CLIENT_ID;
const APPLE_CLIENT_SECRET = env.APPLE_CLIENT_SECRET;

export const auth = betterAuth({
	secret: BETTER_AUTH_SECRET,
	baseURL: PUBLIC_APP_URL,

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
			console.log('[Auth] sendResetPassword called for:', user.email);
			await sendPasswordResetEmail(user.email, url);
		}
	},

	emailVerification: {
		sendVerificationEmail: async ({ user, url }) => {
			console.log('[Auth] sendVerificationEmail called for:', user.email);
			console.log('[Auth] Verification URL:', url);
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
		expiresIn: 60 * 60 * 24 * 30, // 30 days
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
