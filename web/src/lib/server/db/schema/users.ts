import { pgTable, text, timestamp, primaryKey } from 'drizzle-orm/pg-core';
import { user } from './auth';

// Re-export ProfileTier and TIER_REQUIREMENTS from auth for backwards compatibility
export { TIER_REQUIREMENTS } from './auth';
export type { ProfileTier } from './auth';

// Follows - linked to Better Auth users
export const follows = pgTable(
	'follows',
	{
		followerId: text('follower_id')
			.notNull()
			.references(() => user.id, { onDelete: 'cascade' }),
		followingId: text('following_id')
			.notNull()
			.references(() => user.id, { onDelete: 'cascade' }),
		createdAt: timestamp('created_at', { withTimezone: true }).defaultNow().notNull()
	},
	(table) => ({
		pk: primaryKey({ columns: [table.followerId, table.followingId] })
	})
);
