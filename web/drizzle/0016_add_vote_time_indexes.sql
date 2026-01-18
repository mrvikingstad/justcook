-- Add indexes for time-based vote queries (trending, weekly, monthly sorting)
-- These indexes improve performance for queries that filter votes by creation date

CREATE INDEX IF NOT EXISTS "votes_created_at_idx" ON "votes" ("created_at");
CREATE INDEX IF NOT EXISTS "votes_recipe_created_idx" ON "votes" ("recipe_id", "created_at");
