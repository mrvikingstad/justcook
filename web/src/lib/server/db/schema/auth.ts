import { pgTable, text, timestamp, boolean, varchar } from 'drizzle-orm/pg-core';

export type ProfileTier = 'user' | 'author' | 'chef';

export const TIER_REQUIREMENTS = {
	author: { recipes: 5, upvotes: 100 },
	chef: { recipes: 10, upvotes: 2000 }
} as const;

// Better Auth tables - these match Better Auth's expected schema
// Extended with profile fields (fullName, country, bio, photoUrl, profileTier)
export const user = pgTable('user', {
	id: text('id').primaryKey(),
	name: text('name').notNull(),
	email: text('email').notNull().unique(),
	emailVerified: boolean('email_verified').notNull().default(false),
	image: text('image'),
	username: text('username').unique(),
	displayUsername: text('display_username'),
	usernameChangedAt: timestamp('username_changed_at'),
	// Profile fields (consolidated from chef_profiles)
	fullName: varchar('full_name', { length: 255 }),
	country: varchar('country', { length: 100 }),
	bio: text('bio'),
	photoUrl: text('photo_url'),
	profileTier: varchar('profile_tier', { length: 20 }).default('user').notNull().$type<ProfileTier>(),
	createdAt: timestamp('created_at').notNull().defaultNow(),
	updatedAt: timestamp('updated_at').notNull().defaultNow()
});

export const session = pgTable('session', {
	id: text('id').primaryKey(),
	expiresAt: timestamp('expires_at').notNull(),
	token: text('token').notNull().unique(),
	createdAt: timestamp('created_at').notNull().defaultNow(),
	updatedAt: timestamp('updated_at').notNull().defaultNow(),
	ipAddress: text('ip_address'),
	userAgent: text('user_agent'),
	userId: text('user_id')
		.notNull()
		.references(() => user.id, { onDelete: 'cascade' })
});

export const account = pgTable('account', {
	id: text('id').primaryKey(),
	accountId: text('account_id').notNull(),
	providerId: text('provider_id').notNull(),
	userId: text('user_id')
		.notNull()
		.references(() => user.id, { onDelete: 'cascade' }),
	accessToken: text('access_token'),
	refreshToken: text('refresh_token'),
	idToken: text('id_token'),
	accessTokenExpiresAt: timestamp('access_token_expires_at'),
	refreshTokenExpiresAt: timestamp('refresh_token_expires_at'),
	scope: text('scope'),
	password: text('password'),
	createdAt: timestamp('created_at').notNull().defaultNow(),
	updatedAt: timestamp('updated_at').notNull().defaultNow()
});

export const verification = pgTable('verification', {
	id: text('id').primaryKey(),
	identifier: text('identifier').notNull(),
	value: text('value').notNull(),
	expiresAt: timestamp('expires_at').notNull(),
	createdAt: timestamp('created_at').notNull().defaultNow(),
	updatedAt: timestamp('updated_at').notNull().defaultNow()
});
