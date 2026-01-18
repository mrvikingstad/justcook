/**
 * Script to apply full-text search migration
 * Run with: bun run scripts/apply-fulltext-search.ts
 */

import postgres from 'postgres';

const DATABASE_URL = process.env.DATABASE_URL;

if (!DATABASE_URL) {
	console.error('DATABASE_URL environment variable is required');
	process.exit(1);
}

const sql = postgres(DATABASE_URL);

async function applyFullTextSearch() {
	console.log('Applying full-text search migration...');

	try {
		// Add the search vector column (if not exists)
		await sql`ALTER TABLE "recipes" ADD COLUMN IF NOT EXISTS "search_vector" tsvector`;
		console.log('✓ Added search_vector column');

		// Create GIN index for fast full-text search
		await sql`CREATE INDEX IF NOT EXISTS "recipes_search_idx" ON "recipes" USING GIN ("search_vector")`;
		console.log('✓ Created GIN index');

		// Create function to update search vector
		await sql`
			CREATE OR REPLACE FUNCTION recipes_search_vector_update() RETURNS trigger AS $$
			BEGIN
			  NEW.search_vector :=
			    setweight(to_tsvector('english', COALESCE(NEW.title, '')), 'A') ||
			    setweight(to_tsvector('english', COALESCE(NEW.description, '')), 'B') ||
			    setweight(to_tsvector('english', COALESCE(NEW.cuisine, '')), 'C') ||
			    setweight(to_tsvector('english', COALESCE(NEW.tag, '')), 'C');
			  RETURN NEW;
			END;
			$$ LANGUAGE plpgsql
		`;
		console.log('✓ Created search vector update function');

		// Create trigger to automatically update search vector on insert/update
		await sql`DROP TRIGGER IF EXISTS recipes_search_vector_trigger ON "recipes"`;
		await sql`
			CREATE TRIGGER recipes_search_vector_trigger
			  BEFORE INSERT OR UPDATE OF title, description, cuisine, tag
			  ON "recipes"
			  FOR EACH ROW
			  EXECUTE FUNCTION recipes_search_vector_update()
		`;
		console.log('✓ Created search vector trigger');

		// Backfill existing recipes with search vectors
		const result = await sql`
			UPDATE "recipes" SET search_vector =
			  setweight(to_tsvector('english', COALESCE(title, '')), 'A') ||
			  setweight(to_tsvector('english', COALESCE(description, '')), 'B') ||
			  setweight(to_tsvector('english', COALESCE(cuisine, '')), 'C') ||
			  setweight(to_tsvector('english', COALESCE(tag, '')), 'C')
			WHERE search_vector IS NULL
		`;
		console.log(`✓ Backfilled ${result.count} existing recipes`);

		console.log('\n✅ Full-text search migration complete!');
	} catch (error) {
		console.error('Migration failed:', error);
		process.exit(1);
	} finally {
		await sql.end();
	}
}

applyFullTextSearch();
