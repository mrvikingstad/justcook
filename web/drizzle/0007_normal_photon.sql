DROP TABLE "chef_profiles" CASCADE;--> statement-breakpoint
ALTER TABLE "recipes" ADD COLUMN "language" varchar(10) NOT NULL;--> statement-breakpoint
ALTER TABLE "user" ADD COLUMN "full_name" varchar(255);--> statement-breakpoint
ALTER TABLE "user" ADD COLUMN "country" varchar(100);--> statement-breakpoint
ALTER TABLE "user" ADD COLUMN "bio" text;--> statement-breakpoint
ALTER TABLE "user" ADD COLUMN "photo_url" text;--> statement-breakpoint
ALTER TABLE "user" ADD COLUMN "profile_tier" varchar(20) DEFAULT 'user' NOT NULL;