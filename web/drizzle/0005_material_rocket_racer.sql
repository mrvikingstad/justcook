ALTER TABLE "magic_links" DISABLE ROW LEVEL SECURITY;--> statement-breakpoint
ALTER TABLE "oauth_accounts" DISABLE ROW LEVEL SECURITY;--> statement-breakpoint
ALTER TABLE "sessions" DISABLE ROW LEVEL SECURITY;--> statement-breakpoint
ALTER TABLE "users" DISABLE ROW LEVEL SECURITY;--> statement-breakpoint
DROP TABLE "magic_links" CASCADE;--> statement-breakpoint
DROP TABLE "oauth_accounts" CASCADE;--> statement-breakpoint
DROP TABLE "sessions" CASCADE;--> statement-breakpoint
DROP TABLE "users" CASCADE;--> statement-breakpoint
ALTER TABLE "chef_profiles" DROP CONSTRAINT "chef_profiles_user_id_users_id_fk";
--> statement-breakpoint
ALTER TABLE "follows" DROP CONSTRAINT "follows_follower_id_users_id_fk";
--> statement-breakpoint
ALTER TABLE "follows" DROP CONSTRAINT "follows_following_id_users_id_fk";
--> statement-breakpoint
ALTER TABLE "recipes" DROP CONSTRAINT "recipes_author_id_users_id_fk";
--> statement-breakpoint
ALTER TABLE "comments" DROP CONSTRAINT "comments_user_id_users_id_fk";
--> statement-breakpoint
ALTER TABLE "votes" DROP CONSTRAINT "votes_user_id_users_id_fk";
--> statement-breakpoint
ALTER TABLE "chef_profiles" ALTER COLUMN "user_id" SET DATA TYPE text;--> statement-breakpoint
ALTER TABLE "follows" ALTER COLUMN "follower_id" SET DATA TYPE text;--> statement-breakpoint
ALTER TABLE "follows" ALTER COLUMN "following_id" SET DATA TYPE text;--> statement-breakpoint
ALTER TABLE "recipes" ALTER COLUMN "author_id" SET DATA TYPE text;--> statement-breakpoint
ALTER TABLE "comments" ALTER COLUMN "user_id" SET DATA TYPE text;--> statement-breakpoint
ALTER TABLE "votes" ALTER COLUMN "user_id" SET DATA TYPE text;--> statement-breakpoint
ALTER TABLE "user" ADD COLUMN "username_changed_at" timestamp;--> statement-breakpoint
ALTER TABLE "chef_profiles" ADD CONSTRAINT "chef_profiles_user_id_user_id_fk" FOREIGN KEY ("user_id") REFERENCES "public"."user"("id") ON DELETE cascade ON UPDATE no action;--> statement-breakpoint
ALTER TABLE "follows" ADD CONSTRAINT "follows_follower_id_user_id_fk" FOREIGN KEY ("follower_id") REFERENCES "public"."user"("id") ON DELETE cascade ON UPDATE no action;--> statement-breakpoint
ALTER TABLE "follows" ADD CONSTRAINT "follows_following_id_user_id_fk" FOREIGN KEY ("following_id") REFERENCES "public"."user"("id") ON DELETE cascade ON UPDATE no action;--> statement-breakpoint
ALTER TABLE "recipes" ADD CONSTRAINT "recipes_author_id_user_id_fk" FOREIGN KEY ("author_id") REFERENCES "public"."user"("id") ON DELETE cascade ON UPDATE no action;--> statement-breakpoint
ALTER TABLE "comments" ADD CONSTRAINT "comments_user_id_user_id_fk" FOREIGN KEY ("user_id") REFERENCES "public"."user"("id") ON DELETE cascade ON UPDATE no action;--> statement-breakpoint
ALTER TABLE "votes" ADD CONSTRAINT "votes_user_id_user_id_fk" FOREIGN KEY ("user_id") REFERENCES "public"."user"("id") ON DELETE cascade ON UPDATE no action;