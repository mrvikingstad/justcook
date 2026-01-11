-- Create bookmarks table
CREATE TABLE IF NOT EXISTS "bookmarks" (
	"id" uuid PRIMARY KEY DEFAULT gen_random_uuid() NOT NULL,
	"recipe_id" uuid NOT NULL,
	"user_id" text NOT NULL,
	"created_at" timestamp with time zone DEFAULT now() NOT NULL,
	CONSTRAINT "bookmarks_user_id_recipe_id_unique" UNIQUE("user_id", "recipe_id")
);

-- Add foreign key constraints
ALTER TABLE "bookmarks" ADD CONSTRAINT "bookmarks_recipe_id_recipes_id_fk" FOREIGN KEY ("recipe_id") REFERENCES "public"."recipes"("id") ON DELETE cascade ON UPDATE no action;
ALTER TABLE "bookmarks" ADD CONSTRAINT "bookmarks_user_id_user_id_fk" FOREIGN KEY ("user_id") REFERENCES "public"."user"("id") ON DELETE cascade ON UPDATE no action;

-- Create index for faster user bookmark queries
CREATE INDEX IF NOT EXISTS "bookmarks_user_id_idx" ON "bookmarks" ("user_id");
