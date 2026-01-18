import { pgTable, uuid, integer, text, timestamp, unique, index } from 'drizzle-orm/pg-core';
import { user } from './auth';
import { recipes } from './recipes';

export const votes = pgTable(
	'votes',
	{
		id: uuid('id').primaryKey().defaultRandom(),
		recipeId: uuid('recipe_id')
			.notNull()
			.references(() => recipes.id, { onDelete: 'cascade' }),
		userId: text('user_id')
			.notNull()
			.references(() => user.id, { onDelete: 'cascade' }),
		value: integer('value').notNull(),
		createdAt: timestamp('created_at', { withTimezone: true }).defaultNow().notNull()
	},
	(table) => [
		unique().on(table.userId, table.recipeId),
		index('votes_recipe_id_idx').on(table.recipeId),
		index('votes_user_id_idx').on(table.userId),
		index('votes_created_at_idx').on(table.createdAt),
		index('votes_recipe_created_idx').on(table.recipeId, table.createdAt)
	]
);

export const comments = pgTable(
	'comments',
	{
		id: uuid('id').primaryKey().defaultRandom(),
		recipeId: uuid('recipe_id')
			.notNull()
			.references(() => recipes.id, { onDelete: 'cascade' }),
		userId: text('user_id')
			.notNull()
			.references(() => user.id, { onDelete: 'cascade' }),
		content: text('content').notNull(),
		createdAt: timestamp('created_at', { withTimezone: true }).defaultNow().notNull(),
		updatedAt: timestamp('updated_at', { withTimezone: true }).defaultNow().notNull()
	},
	(table) => [
		index('comments_recipe_id_idx').on(table.recipeId),
		index('comments_user_id_idx').on(table.userId),
		index('comments_created_at_idx').on(table.createdAt),
		// Composite index for efficient paginated comment fetching sorted by date
		index('comments_recipe_created_idx').on(table.recipeId, table.createdAt)
	]
);

export const bookmarks = pgTable(
	'bookmarks',
	{
		id: uuid('id').primaryKey().defaultRandom(),
		recipeId: uuid('recipe_id')
			.notNull()
			.references(() => recipes.id, { onDelete: 'cascade' }),
		userId: text('user_id')
			.notNull()
			.references(() => user.id, { onDelete: 'cascade' }),
		createdAt: timestamp('created_at', { withTimezone: true }).defaultNow().notNull()
	},
	(table) => [
		unique().on(table.userId, table.recipeId),
		index('bookmarks_recipe_id_idx').on(table.recipeId),
		index('bookmarks_user_id_idx').on(table.userId)
	]
);
