import { json } from '@sveltejs/kit';
import type { RequestHandler } from './$types';
import { db } from '$lib/server/db';
import { comments, recipes, user } from '$lib/server/db/schema';
import { eq, sql, desc, count } from 'drizzle-orm';
import { moderateComment, queueForReview } from '$lib/server/moderation';
import { sanitizeText } from '$lib/server/validation/sanitize';
import { logger, getRequestId } from '$lib/server/logger';

const DEFAULT_LIMIT = 20;
const MAX_LIMIT = 50;

export const GET: RequestHandler = async ({ url, locals }) => {
	const recipeId = url.searchParams.get('recipeId');
	const slug = url.searchParams.get('slug');
	const page = Math.max(1, parseInt(url.searchParams.get('page') || '1', 10));
	const limit = Math.min(MAX_LIMIT, Math.max(1, parseInt(url.searchParams.get('limit') || String(DEFAULT_LIMIT), 10)));
	const offset = (page - 1) * limit;

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

	// Get total comment count for pagination
	const [countResult] = await db
		.select({ total: count() })
		.from(comments)
		.where(eq(comments.recipeId, targetRecipeId));
	const totalCount = countResult?.total ?? 0;

	// Get paginated comments with author info
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
		.orderBy(desc(comments.createdAt))
		.limit(limit)
		.offset(offset);

	const totalPages = Math.ceil(totalCount / limit);

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
		})),
		pagination: {
			page,
			limit,
			totalCount,
			totalPages,
			hasMore: page < totalPages
		}
	});
};

export const POST: RequestHandler = async ({ request, locals }) => {
	if (!locals.user) {
		return json({ error: 'Unauthorized' }, { status: 401 });
	}

	const userId = locals.user.id;
	const { recipeId, content } = await request.json();

	if (!recipeId || typeof recipeId !== 'string') {
		return json({ error: 'Invalid recipe ID' }, { status: 400 });
	}

	if (!content || typeof content !== 'string' || content.trim().length === 0) {
		return json({ error: 'Comment content is required' }, { status: 400 });
	}

	// Sanitize content to prevent XSS
	const sanitizedContent = sanitizeText(content);

	if (sanitizedContent.length < 2) {
		return json({ error: 'Comment is too short (min 2 characters)' }, { status: 400 });
	}

	if (sanitizedContent.length > 2000) {
		return json({ error: 'Comment is too long (max 2000 characters)' }, { status: 400 });
	}

	// Moderate comment before posting
	const moderation = await moderateComment(sanitizedContent, userId);

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

		// Insert comment and increment count in a transaction
		const newComment = await db.transaction(async (tx) => {
			// Lock the recipe row first with FOR UPDATE to prevent race conditions
			await tx
				.select({ id: recipes.id })
				.from(recipes)
				.where(eq(recipes.id, recipeId))
				.for('update');

			const [comment] = await tx
				.insert(comments)
				.values({
					recipeId,
					userId,
					content: sanitizedContent
				})
				.returning({
					id: comments.id,
					content: comments.content,
					createdAt: comments.createdAt,
					userId: comments.userId
				});

			// Increment comment count atomically (row is already locked)
			await tx
				.update(recipes)
				.set({
					commentCount: sql`${recipes.commentCount} + 1`
				})
				.where(eq(recipes.id, recipeId));

			return comment;
		});

		// Get user info for response (outside transaction, not critical)
		const [userInfo] = await db
			.select({
				userName: user.name,
				userImage: user.image,
				fullName: user.fullName,
				photoUrl: user.photoUrl
			})
			.from(user)
			.where(eq(user.id, userId))
			.limit(1);

		// Queue for manual review if moderation couldn't complete
		if (moderation.needsReview && moderation.reviewReason) {
			await queueForReview('comment', newComment.id, moderation.reviewReason);
		}

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
		logger.error('Failed to post comment', error, { recipeId, userId });
		return json({ error: 'Failed to post comment', requestId: getRequestId() }, { status: 500 });
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

		// Delete comment and decrement count in a transaction
		await db.transaction(async (tx) => {
			// Lock the recipe row first with FOR UPDATE to prevent race conditions
			await tx
				.select({ id: recipes.id })
				.from(recipes)
				.where(eq(recipes.id, comment.recipeId))
				.for('update');

			await tx.delete(comments).where(eq(comments.id, commentId));

			// Decrement comment count atomically (row is already locked)
			await tx
				.update(recipes)
				.set({
					commentCount: sql`GREATEST(${recipes.commentCount} - 1, 0)`
				})
				.where(eq(recipes.id, comment.recipeId));
		});

		return json({ success: true });
	} catch (error: unknown) {
		logger.error('Failed to delete comment', error, { commentId, userId: locals.user.id });
		return json({ error: 'Failed to delete comment', requestId: getRequestId() }, { status: 500 });
	}
};
