CREATE INDEX "follows_following_created_idx" ON "follows" USING btree ("following_id","created_at");--> statement-breakpoint
CREATE INDEX "comments_recipe_created_idx" ON "comments" USING btree ("recipe_id","created_at");--> statement-breakpoint
CREATE INDEX "votes_created_at_idx" ON "votes" USING btree ("created_at");--> statement-breakpoint
CREATE INDEX "votes_recipe_created_idx" ON "votes" USING btree ("recipe_id","created_at");--> statement-breakpoint
CREATE INDEX "moderation_queue_status_created_idx" ON "moderation_queue" USING btree ("status","created_at");