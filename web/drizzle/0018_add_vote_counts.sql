-- Add separate upvotes and downvotes columns to recipes for O(1) vote count updates
-- This avoids the O(n) recalculation of all votes on every vote action

ALTER TABLE "recipes" ADD COLUMN "upvotes" integer DEFAULT 0 NOT NULL;
ALTER TABLE "recipes" ADD COLUMN "downvotes" integer DEFAULT 0 NOT NULL;

-- Backfill existing vote counts from the votes table
UPDATE "recipes" r
SET
    "upvotes" = COALESCE((SELECT COUNT(*) FROM "votes" v WHERE v."recipe_id" = r."id" AND v."value" > 0), 0),
    "downvotes" = COALESCE((SELECT COUNT(*) FROM "votes" v WHERE v."recipe_id" = r."id" AND v."value" < 0), 0);
