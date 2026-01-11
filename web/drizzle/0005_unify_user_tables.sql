-- Migration to unify on Better Auth's user table
-- This removes the legacy users table and updates all references to use Better Auth's user table

-- Step 1: Drop all foreign key constraints from tables referencing the old users table
ALTER TABLE "chef_profiles" DROP CONSTRAINT IF EXISTS "chef_profiles_user_id_users_id_fk";
ALTER TABLE "recipes" DROP CONSTRAINT IF EXISTS "recipes_author_id_users_id_fk";
ALTER TABLE "votes" DROP CONSTRAINT IF EXISTS "votes_user_id_users_id_fk";
ALTER TABLE "comments" DROP CONSTRAINT IF EXISTS "comments_user_id_users_id_fk";
ALTER TABLE "follows" DROP CONSTRAINT IF EXISTS "follows_follower_id_users_id_fk";
ALTER TABLE "follows" DROP CONSTRAINT IF EXISTS "follows_following_id_users_id_fk";

-- Step 2: Change column types from uuid to text
ALTER TABLE "chef_profiles" ALTER COLUMN "user_id" TYPE text USING "user_id"::text;
ALTER TABLE "recipes" ALTER COLUMN "author_id" TYPE text USING "author_id"::text;
ALTER TABLE "votes" ALTER COLUMN "user_id" TYPE text USING "user_id"::text;
ALTER TABLE "comments" ALTER COLUMN "user_id" TYPE text USING "user_id"::text;
ALTER TABLE "follows" ALTER COLUMN "follower_id" TYPE text USING "follower_id"::text;
ALTER TABLE "follows" ALTER COLUMN "following_id" TYPE text USING "following_id"::text;

-- Step 3: Add foreign key constraints to Better Auth's user table
ALTER TABLE "chef_profiles" ADD CONSTRAINT "chef_profiles_user_id_user_id_fk"
  FOREIGN KEY ("user_id") REFERENCES "user"("id") ON DELETE CASCADE;
ALTER TABLE "recipes" ADD CONSTRAINT "recipes_author_id_user_id_fk"
  FOREIGN KEY ("author_id") REFERENCES "user"("id") ON DELETE CASCADE;
ALTER TABLE "votes" ADD CONSTRAINT "votes_user_id_user_id_fk"
  FOREIGN KEY ("user_id") REFERENCES "user"("id") ON DELETE CASCADE;
ALTER TABLE "comments" ADD CONSTRAINT "comments_user_id_user_id_fk"
  FOREIGN KEY ("user_id") REFERENCES "user"("id") ON DELETE CASCADE;
ALTER TABLE "follows" ADD CONSTRAINT "follows_follower_id_user_id_fk"
  FOREIGN KEY ("follower_id") REFERENCES "user"("id") ON DELETE CASCADE;
ALTER TABLE "follows" ADD CONSTRAINT "follows_following_id_user_id_fk"
  FOREIGN KEY ("following_id") REFERENCES "user"("id") ON DELETE CASCADE;

-- Step 4: Drop the legacy users table and related tables
DROP TABLE IF EXISTS "sessions" CASCADE;
DROP TABLE IF EXISTS "oauth_accounts" CASCADE;
DROP TABLE IF EXISTS "magic_links" CASCADE;
DROP TABLE IF EXISTS "users" CASCADE;
