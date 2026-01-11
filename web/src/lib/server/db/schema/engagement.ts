import { pgTable, uuid, integer, text, timestamp, unique } from 'drizzle-orm/pg-core';
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
	(table) => [unique().on(table.userId, table.recipeId)]
);

export const comments = pgTable('comments', {
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
});

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
	(table) => [unique().on(table.userId, table.recipeId)]
);
