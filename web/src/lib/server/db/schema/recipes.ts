import { pgTable, uuid, varchar, text, integer, boolean, timestamp, decimal } from 'drizzle-orm/pg-core';
import { user } from './auth';

export const recipes = pgTable('recipes', {
	id: uuid('id').primaryKey().defaultRandom(),
	authorId: text('author_id')
		.notNull()
		.references(() => user.id, { onDelete: 'cascade' }),
	title: varchar('title', { length: 200 }).notNull(),
	slug: varchar('slug', { length: 220 }).notNull().unique(),
	description: text('description'),
	photoUrl: text('photo_url'),
	prepTimeMinutes: integer('prep_time_minutes'),
	cookTimeMinutes: integer('cook_time_minutes'),
	difficulty: varchar('difficulty', { length: 20 }),
	cuisine: varchar('cuisine', { length: 100 }),
	tag: varchar('tag', { length: 100 }),
	language: varchar('language', { length: 10 }).notNull(),
	servings: integer('servings').notNull().default(4),
	voteScore: integer('vote_score').notNull().default(0),
	commentCount: integer('comment_count').notNull().default(0),
	isPublished: boolean('is_published').notNull().default(false),
	publishedAt: timestamp('published_at', { withTimezone: true }),
	createdAt: timestamp('created_at', { withTimezone: true }).defaultNow().notNull(),
	updatedAt: timestamp('updated_at', { withTimezone: true }).defaultNow().notNull()
});

export const ingredients = pgTable('ingredients', {
	id: uuid('id').primaryKey().defaultRandom(),
	recipeId: uuid('recipe_id')
		.notNull()
		.references(() => recipes.id, { onDelete: 'cascade' }),
	ingredientKey: varchar('ingredient_key', { length: 100 }),
	name: varchar('name', { length: 200 }).notNull(),
	amount: decimal('amount', { precision: 10, scale: 2 }),
	unit: varchar('unit', { length: 50 }),
	sortOrder: integer('sort_order').notNull().default(0),
	notes: varchar('notes', { length: 200 })
});

export const steps = pgTable('steps', {
	id: uuid('id').primaryKey().defaultRandom(),
	recipeId: uuid('recipe_id')
		.notNull()
		.references(() => recipes.id, { onDelete: 'cascade' }),
	stepNumber: integer('step_number').notNull(),
	instruction: text('instruction').notNull()
});
