-- Add full-text search support for recipes
-- This migration adds a tsvector column with a GIN index for fast text search

-- Add the search vector column
ALTER TABLE "recipes" ADD COLUMN IF NOT EXISTS "search_vector" tsvector;

-- Create GIN index for fast full-text search
CREATE INDEX IF NOT EXISTS "recipes_search_idx" ON "recipes" USING GIN ("search_vector");

-- Create function to update search vector
CREATE OR REPLACE FUNCTION recipes_search_vector_update() RETURNS trigger AS $$
BEGIN
  NEW.search_vector :=
    setweight(to_tsvector('english', COALESCE(NEW.title, '')), 'A') ||
    setweight(to_tsvector('english', COALESCE(NEW.description, '')), 'B') ||
    setweight(to_tsvector('english', COALESCE(NEW.cuisine, '')), 'C') ||
    setweight(to_tsvector('english', COALESCE(NEW.tag, '')), 'C');
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Create trigger to automatically update search vector on insert/update
DROP TRIGGER IF EXISTS recipes_search_vector_trigger ON "recipes";
CREATE TRIGGER recipes_search_vector_trigger
  BEFORE INSERT OR UPDATE OF title, description, cuisine, tag
  ON "recipes"
  FOR EACH ROW
  EXECUTE FUNCTION recipes_search_vector_update();

-- Backfill existing recipes with search vectors
UPDATE "recipes" SET search_vector =
  setweight(to_tsvector('english', COALESCE(title, '')), 'A') ||
  setweight(to_tsvector('english', COALESCE(description, '')), 'B') ||
  setweight(to_tsvector('english', COALESCE(cuisine, '')), 'C') ||
  setweight(to_tsvector('english', COALESCE(tag, '')), 'C')
WHERE search_vector IS NULL;
