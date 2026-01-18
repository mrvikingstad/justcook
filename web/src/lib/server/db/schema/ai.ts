import { pgTable, uuid, text, timestamp, integer, index, boolean } from 'drizzle-orm/pg-core';
import { user } from './auth';

// AI Conversations - stores metadata for each conversation
export const aiConversations = pgTable(
	'ai_conversations',
	{
		id: uuid('id').primaryKey().defaultRandom(),
		userId: text('user_id')
			.notNull()
			.references(() => user.id, { onDelete: 'cascade' }),
		title: text('title'),
		isArchived: boolean('is_archived').notNull().default(false),
		messageCount: integer('message_count').notNull().default(0),
		createdAt: timestamp('created_at', { withTimezone: true }).defaultNow().notNull(),
		updatedAt: timestamp('updated_at', { withTimezone: true }).defaultNow().notNull()
	},
	(table) => [
		index('ai_conversations_user_id_idx').on(table.userId),
		index('ai_conversations_user_updated_idx').on(table.userId, table.updatedAt)
	]
);

// AI Messages - stores individual messages in conversations
export const aiMessages = pgTable(
	'ai_messages',
	{
		id: uuid('id').primaryKey().defaultRandom(),
		conversationId: uuid('conversation_id')
			.notNull()
			.references(() => aiConversations.id, { onDelete: 'cascade' }),
		role: text('role').notNull().$type<'user' | 'assistant'>(),
		content: text('content').notNull(),
		tokenCount: integer('token_count'),
		createdAt: timestamp('created_at', { withTimezone: true }).defaultNow().notNull()
	},
	(table) => [index('ai_messages_conversation_id_idx').on(table.conversationId)]
);

// AI Usage Tracking - for rate limiting and analytics
export const aiUsage = pgTable(
	'ai_usage',
	{
		id: uuid('id').primaryKey().defaultRandom(),
		userId: text('user_id')
			.notNull()
			.references(() => user.id, { onDelete: 'cascade' }),
		tokensUsed: integer('tokens_used').notNull(),
		date: timestamp('date', { withTimezone: true }).notNull(),
		createdAt: timestamp('created_at', { withTimezone: true }).defaultNow().notNull()
	},
	(table) => [index('ai_usage_user_date_idx').on(table.userId, table.date)]
);
