import { json } from '@sveltejs/kit';
import type { RequestHandler } from './$types';
import { db } from '$lib/server/db';
import { comments, recipes, user } from '$lib/server/db/schema';
import { eq, sql, desc } from 'drizzle-orm';
import { moderateComment } from '$lib/server/moderation';

export const GET: RequestHandler = async ({ url, locals }) => {
	const recipeId = url.searchParams.get('recipeId');
	const slug = url.searchParams.get('slug');

	let targetRecipeId = recipeId;

	// If slug provided, lookup recipe ID
	if (!targetRecipeId && slug) {
		const [recipe] = await db
			.select({ id: recipes.id, authorId: recipes.authorId })
			.from(recipes)
			.where(eq(recipes.slug, slug))
			.limit(1);

		if (!recipe) {
			return json({ error: 'Recipe not found' }, { status: 404 });
		}
		targetRecipeId = recipe.id;
	}

	if (!targetRecipeId) {
		return json({ error: 'recipeId or slug is required' }, { status: 400 });
	}

	// Get recipe author for marking author comments
	const [recipe] = await db
		.select({ authorId: recipes.authorId })
		.from(recipes)
		.where(eq(recipes.id, targetRecipeId))
		.limit(1);

	// Get comments with author info
	const commentList = await db
		.select({
			id: comments.id,
			content: comments.content,
			createdAt: comments.createdAt,
			userId: comments.userId,
			userName: user.name,
			userImage: user.image,
			fullName: user.fullName,
			photoUrl: user.photoUrl
		})
		.from(comments)
		.leftJoin(user, eq(comments.userId, user.id))
		.where(eq(comments.recipeId, targetRecipeId))
		.orderBy(desc(comments.createdAt));

	return json({
		comments: commentList.map((c) => ({
			id: c.id,
			content: c.content,
			createdAt: c.createdAt,
			authorId: c.userId,
			authorName: c.fullName || c.userName || 'Anonymous',
			authorAvatar: c.photoUrl || c.userImage || null,
			isRecipeAuthor: c.userId === recipe?.authorId,
			isOwn: locals.user?.id === c.userId
		}))
	});
};

export const POST: RequestHandler = async ({ request, locals }) => {
	if (!locals.user) {
		return json({ error: 'Unauthorized' }, { status: 401 });
	}

	const { recipeId, content } = await request.json();

	if (!recipeId || typeof recipeId !== 'string') {
		return json({ error: 'Invalid recipe ID' }, { status: 400 });
	}

	if (!content || typeof content !== 'string' || content.trim().length === 0) {
		return json({ error: 'Comment content is required' }, { status: 400 });
	}

	if (content.trim().length > 2000) {
		return json({ error: 'Comment is too long (max 2000 characters)' }, { status: 400 });
	}

	// Moderate comment before posting
	const moderation = await moderateComment(content);

	if (moderation.flagged) {
		return json(
			{ error: moderation.message || 'Your comment has been flagged. Please revise and try again.' },
			{ status: 400 }
		);
	}

	try {
		// Check if recipe exists
		const [recipe] = await db
			.select({ id: recipes.id, authorId: recipes.authorId })
			.from(recipes)
			.where(eq(recipes.id, recipeId))
			.limit(1);

		if (!recipe) {
			return json({ error: 'Recipe not found' }, { status: 404 });
		}

		// Insert comment
		const [newComment] = await db
			.insert(comments)
			.values({
				recipeId,
				userId: locals.user.id,
				content: content.trim()
			})
			.returning({
				id: comments.id,
				content: comments.content,
				createdAt: comments.createdAt,
				userId: comments.userId
			});

		// Increment comment count on recipe
		await db
			.update(recipes)
			.set({
				commentCount: sql`${recipes.commentCount} + 1`
			})
			.where(eq(recipes.id, recipeId));

		// Get user info for response
		const [userInfo] = await db
			.select({
				userName: user.name,
				userImage: user.image,
				fullName: user.fullName,
				photoUrl: user.photoUrl
			})
			.from(user)
			.where(eq(user.id, locals.user.id))
			.limit(1);

		return json({
			success: true,
			comment: {
				id: newComment.id,
				content: newComment.content,
				createdAt: newComment.createdAt,
				authorId: newComment.userId,
				authorName: userInfo?.fullName || userInfo?.userName || 'Anonymous',
				authorAvatar: userInfo?.photoUrl || userInfo?.userImage || null,
				isRecipeAuthor: newComment.userId === recipe.authorId,
				isOwn: true
			}
		});
	} catch (error: unknown) {
		console.error('Failed to post comment:', error);
		return json({ error: 'Failed to post comment' }, { status: 500 });
	}
};

export const DELETE: RequestHandler = async ({ request, locals }) => {
	if (!locals.user) {
		return json({ error: 'Unauthorized' }, { status: 401 });
	}

	const { commentId } = await request.json();

	if (!commentId || typeof commentId !== 'string') {
		return json({ error: 'Invalid comment ID' }, { status: 400 });
	}

	try {
		// Get comment to verify ownership and get recipeId
		const [comment] = await db
			.select({
				id: comments.id,
				userId: comments.userId,
				recipeId: comments.recipeId
			})
			.from(comments)
			.where(eq(comments.id, commentId))
			.limit(1);

		if (!comment) {
			return json({ error: 'Comment not found' }, { status: 404 });
		}

		if (comment.userId !== locals.user.id) {
			return json({ error: 'Cannot delete another user\'s comment' }, { status: 403 });
		}

		// Delete comment
		await db.delete(comments).where(eq(comments.id, commentId));

		// Decrement comment count on recipe
		await db
			.update(recipes)
			.set({
				commentCount: sql`GREATEST(${recipes.commentCount} - 1, 0)`
			})
			.where(eq(recipes.id, comment.recipeId));

		return json({ success: true });
	} catch (error: unknown) {
		console.error('Failed to delete comment:', error);
		return json({ error: 'Failed to delete comment' }, { status: 500 });
	}
};
