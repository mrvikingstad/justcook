import { pgTable, uuid, text, timestamp, varchar, index } from 'drizzle-orm/pg-core';

export const moderationQueue = pgTable(
	'moderation_queue',
	{
		id: uuid('id').primaryKey().defaultRandom(),
		contentType: varchar('content_type', { length: 20 }).notNull(), // 'recipe' | 'comment'
		contentId: uuid('content_id').notNull(),
		reason: varchar('reason', { length: 100 }).notNull(), // 'api_error' | 'timeout' | etc
		status: varchar('status', { length: 20 }).notNull().default('pending'), // 'pending' | 'approved' | 'rejected'
		reviewedAt: timestamp('reviewed_at', { withTimezone: true }),
		reviewedBy: text('reviewed_by'),
		createdAt: timestamp('created_at', { withTimezone: true }).defaultNow().notNull()
	},
	(table) => [
		index('moderation_queue_status_idx').on(table.status),
		index('moderation_queue_created_at_idx').on(table.createdAt),
		// Composite index for efficient querying of pending items sorted by creation date
		index('moderation_queue_status_created_idx').on(table.status, table.createdAt)
	]
);
