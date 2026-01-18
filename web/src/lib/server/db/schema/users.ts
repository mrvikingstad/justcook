import { pgTable, text, timestamp, primaryKey, index } from 'drizzle-orm/pg-core';
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
		pk: primaryKey({ columns: [table.followerId, table.followingId] }),
		followerIdx: index('follows_follower_id_idx').on(table.followerId),
		followingIdx: index('follows_following_id_idx').on(table.followingId),
		// Composite index for efficient follower count queries with time-based sorting
		followingCreatedIdx: index('follows_following_created_idx').on(table.followingId, table.createdAt)
	})
);
