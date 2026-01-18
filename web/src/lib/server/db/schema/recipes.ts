import { pgTable, uuid, varchar, text, integer, boolean, timestamp, decimal, index, customType } from 'drizzle-orm/pg-core';
import { user } from './auth';

// Custom type for PostgreSQL tsvector (full-text search)
const tsvector = customType<{ data: string }>({
	dataType() {
		return 'tsvector';
	}
});

export const recipes = pgTable(
	'recipes',
	{
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
		upvotes: integer('upvotes').notNull().default(0),
		downvotes: integer('downvotes').notNull().default(0),
		voteScore: integer('vote_score').notNull().default(0),
		commentCount: integer('comment_count').notNull().default(0),
		isPublished: boolean('is_published').notNull().default(false),
		publishedAt: timestamp('published_at', { withTimezone: true }),
		searchVector: tsvector('search_vector'), // Full-text search vector (auto-updated by trigger)
		createdAt: timestamp('created_at', { withTimezone: true }).defaultNow().notNull(),
		updatedAt: timestamp('updated_at', { withTimezone: true }).defaultNow().notNull()
	},
	(table) => [
		index('recipes_author_id_idx').on(table.authorId),
		index('recipes_is_published_idx').on(table.isPublished),
		index('recipes_cuisine_idx').on(table.cuisine),
		index('recipes_tag_idx').on(table.tag),
		index('recipes_language_idx').on(table.language),
		index('recipes_published_at_idx').on(table.publishedAt),
		index('recipes_published_language_idx').on(table.isPublished, table.language, table.publishedAt)
	]
);

export const ingredients = pgTable(
	'ingredients',
	{
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
	},
	(table) => [index('ingredients_recipe_id_idx').on(table.recipeId)]
);

export const steps = pgTable(
	'steps',
	{
		id: uuid('id').primaryKey().defaultRandom(),
		recipeId: uuid('recipe_id')
			.notNull()
			.references(() => recipes.id, { onDelete: 'cascade' }),
		stepNumber: integer('step_number').notNull(),
		instruction: text('instruction').notNull()
	},
	(table) => [index('steps_recipe_id_idx').on(table.recipeId)]
);

export const tips = pgTable(
	'tips',
	{
		id: uuid('id').primaryKey().defaultRandom(),
		recipeId: uuid('recipe_id')
			.notNull()
			.references(() => recipes.id, { onDelete: 'cascade' }),
		sortOrder: integer('sort_order').notNull().default(0),
		content: varchar('content', { length: 500 }).notNull()
	},
	(table) => [index('tips_recipe_id_idx').on(table.recipeId)]
);

export const equipment = pgTable(
	'equipment',
	{
		id: uuid('id').primaryKey().defaultRandom(),
		recipeId: uuid('recipe_id')
			.notNull()
			.references(() => recipes.id, { onDelete: 'cascade' }),
		equipmentKey: varchar('equipment_key', { length: 100 }),
		name: varchar('name', { length: 200 }).notNull(),
		notes: varchar('notes', { length: 200 }),
		sortOrder: integer('sort_order').notNull().default(0)
	},
	(table) => [index('equipment_recipe_id_idx').on(table.recipeId)]
);
