-- Add performance indexes for frequently queried columns
-- These indexes address N+1 query patterns and slow lookups

-- Recipe indexes for filtering and sorting
CREATE INDEX IF NOT EXISTS "recipes_author_id_idx" ON "recipes" ("author_id");
CREATE INDEX IF NOT EXISTS "recipes_is_published_idx" ON "recipes" ("is_published");
CREATE INDEX IF NOT EXISTS "recipes_cuisine_idx" ON "recipes" ("cuisine");
CREATE INDEX IF NOT EXISTS "recipes_tag_idx" ON "recipes" ("tag");
CREATE INDEX IF NOT EXISTS "recipes_language_idx" ON "recipes" ("language");
CREATE INDEX IF NOT EXISTS "recipes_published_at_idx" ON "recipes" ("published_at" DESC);

-- Composite index for common query pattern (published recipes by language)
CREATE INDEX IF NOT EXISTS "recipes_published_language_idx" ON "recipes" ("is_published", "language", "published_at" DESC);

-- Vote indexes for aggregation queries
CREATE INDEX IF NOT EXISTS "votes_recipe_id_idx" ON "votes" ("recipe_id");
CREATE INDEX IF NOT EXISTS "votes_user_id_idx" ON "votes" ("user_id");

-- Comment indexes for recipe and user queries
CREATE INDEX IF NOT EXISTS "comments_recipe_id_idx" ON "comments" ("recipe_id");
CREATE INDEX IF NOT EXISTS "comments_user_id_idx" ON "comments" ("user_id");

-- Bookmark indexes for user and recipe lookups
CREATE INDEX IF NOT EXISTS "bookmarks_recipe_id_idx" ON "bookmarks" ("recipe_id");
CREATE INDEX IF NOT EXISTS "bookmarks_user_id_idx" ON "bookmarks" ("user_id");

-- Follow indexes for follower and following queries
CREATE INDEX IF NOT EXISTS "follows_follower_id_idx" ON "follows" ("follower_id");
CREATE INDEX IF NOT EXISTS "follows_following_id_idx" ON "follows" ("following_id");

-- Ingredient and step indexes for recipe detail queries
CREATE INDEX IF NOT EXISTS "ingredients_recipe_id_idx" ON "ingredients" ("recipe_id");
CREATE INDEX IF NOT EXISTS "steps_recipe_id_idx" ON "steps" ("recipe_id");
