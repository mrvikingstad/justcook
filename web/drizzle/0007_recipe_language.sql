-- Add language column to recipes table
ALTER TABLE "recipes" ADD COLUMN "language" varchar(10);

-- Set default language for existing recipes
UPDATE "recipes" SET "language" = 'en' WHERE "language" IS NULL;

-- Make column required
ALTER TABLE "recipes" ALTER COLUMN "language" SET NOT NULL;
