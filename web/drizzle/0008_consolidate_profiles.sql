-- Consolidate chef_profiles into user table
-- Add new columns to user table
ALTER TABLE "user" ADD COLUMN "full_name" varchar(255);
ALTER TABLE "user" ADD COLUMN "country" varchar(100);
ALTER TABLE "user" ADD COLUMN "bio" text;
ALTER TABLE "user" ADD COLUMN "photo_url" text;
ALTER TABLE "user" ADD COLUMN "profile_tier" varchar(20) DEFAULT 'user' NOT NULL;

-- Migrate data from chef_profiles
UPDATE "user" u
SET
  full_name = cp.full_name,
  country = cp.country,
  bio = cp.bio,
  photo_url = COALESCE(cp.photo_url, u.image),
  profile_tier = cp.profile_tier
FROM "chef_profiles" cp
WHERE u.id = cp.user_id;

-- For users without chef_profiles, use existing name/image
UPDATE "user"
SET
  full_name = COALESCE(full_name, name),
  photo_url = COALESCE(photo_url, image)
WHERE full_name IS NULL;

-- Drop the chef_profiles table
DROP TABLE IF EXISTS "chef_profiles";
