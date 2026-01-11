-- Add profile_tier column to chef_profiles
ALTER TABLE "chef_profiles" ADD COLUMN "profile_tier" varchar(10) NOT NULL DEFAULT 'user';

-- Migrate existing verified users to author tier
UPDATE "chef_profiles" SET "profile_tier" = 'author' WHERE "is_verified" = true;

-- Drop the old is_verified column
ALTER TABLE "chef_profiles" DROP COLUMN "is_verified";
