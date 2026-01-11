import { pgTable, uuid, varchar, primaryKey } from 'drizzle-orm/pg-core';
import { recipes } from './recipes';

export const categories = pgTable('categories', {
	id: uuid('id').primaryKey().defaultRandom(),
	name: varchar('name', { length: 100 }).notNull().unique(),
	slug: varchar('slug', { length: 110 }).notNull().unique()
});

export const recipeCategories = pgTable(
	'recipe_categories',
	{
		recipeId: uuid('recipe_id')
			.notNull()
			.references(() => recipes.id, { onDelete: 'cascade' }),
		categoryId: uuid('category_id')
			.notNull()
			.references(() => categories.id, { onDelete: 'cascade' })
	},
	(table) => [primaryKey({ columns: [table.recipeId, table.categoryId] })]
);
