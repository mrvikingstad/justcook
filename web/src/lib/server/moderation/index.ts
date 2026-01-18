import OpenAI from 'openai';
import { env } from '$env/dynamic/private';
import { dev } from '$app/environment';
import { db } from '$lib/server/db';
import { moderationQueue } from '$lib/server/db/schema';
import { logger } from '$lib/server/logger';
import { rateLimit } from '$lib/server/rateLimit';

/**
 * Lightweight pre-filter to catch obvious spam before calling OpenAI API.
 * This reduces API costs and provides instant feedback for obvious violations.
 */
interface PreFilterResult {
	flagged: boolean;
	reason?: string;
	message?: string;
}

function preFilterContent(text: string): PreFilterResult {
	if (!text || text.trim().length === 0) {
		return { flagged: false };
	}

	const trimmed = text.trim();

	// Check for excessive caps (more than 70% uppercase in text over 20 chars)
	if (trimmed.length > 20) {
		const letters = trimmed.replace(/[^a-zA-Z]/g, '');
		if (letters.length > 10) {
			const upperCount = (letters.match(/[A-Z]/g) || []).length;
			const capsRatio = upperCount / letters.length;
			if (capsRatio > 0.7) {
				return {
					flagged: true,
					reason: 'excessive_caps',
					message: 'Please avoid using excessive capital letters.'
				};
			}
		}
	}

	// Check for repeated characters (same char 5+ times in a row)
	if (/(.)\1{4,}/i.test(trimmed)) {
		return {
			flagged: true,
			reason: 'repeated_characters',
			message: 'Please avoid excessive repeated characters.'
		};
	}

	// Check for excessive punctuation (more than 10 punctuation marks in a row)
	if (/[!?.]{10,}/.test(trimmed)) {
		return {
			flagged: true,
			reason: 'excessive_punctuation',
			message: 'Please avoid excessive punctuation.'
		};
	}

	// Check for URL spam (more than 3 URLs in content)
	const urlPattern = /https?:\/\/[^\s]+/gi;
	const urls = trimmed.match(urlPattern) || [];
	if (urls.length > 3) {
		return {
			flagged: true,
			reason: 'url_spam',
			message: 'Please avoid including too many links in your content.'
		};
	}

	// Check for suspicious URL patterns (common spam domains)
	const suspiciousPatterns = [
		/bit\.ly/i,
		/tinyurl/i,
		/t\.co/i,
		/goo\.gl/i,
		/click here/i,
		/free money/i,
		/act now/i,
		/limited time/i,
		/buy now/i,
		/earn \$?\d+/i
	];

	for (const pattern of suspiciousPatterns) {
		if (pattern.test(trimmed)) {
			return {
				flagged: true,
				reason: 'suspicious_content',
				message: 'Your content contains patterns commonly associated with spam. Please revise and try again.'
			};
		}
	}

	// Check for excessive emoji spam (more than 15 emojis)
	const emojiPattern = /[\u{1F600}-\u{1F64F}\u{1F300}-\u{1F5FF}\u{1F680}-\u{1F6FF}\u{1F1E0}-\u{1F1FF}\u{2600}-\u{26FF}\u{2700}-\u{27BF}]/gu;
	const emojis = trimmed.match(emojiPattern) || [];
	if (emojis.length > 15) {
		return {
			flagged: true,
			reason: 'excessive_emojis',
			message: 'Please reduce the number of emojis in your content.'
		};
	}

	return { flagged: false };
}

/**
 * Moderation fallback behavior when API is unavailable:
 * - 'allow': Allow content through (ONLY available in development)
 * - 'queue': Allow but queue for manual review
 * - 'reject': Reject content until moderation is available
 *
 * SECURITY: 'allow' is intentionally disabled in production to prevent
 * unmoderated content from being published.
 */
type ModerationFallback = 'allow' | 'queue' | 'reject';

// Validate API key and fallback configuration on startup in production
if (!dev) {
	const fallback = env.MODERATION_FALLBACK;

	// SECURITY: 'allow' fallback is NEVER permitted in production
	// This prevents accidental bypass of content moderation
	if (fallback === 'allow') {
		throw new Error(
			'MODERATION_FALLBACK=allow is not permitted in production. ' +
			'Use "queue" to allow content through for manual review, or "reject" to block content until moderation is available.'
		);
	}

	// Warn if no API key and fallback will be used
	if (!env.OPENAI_API_KEY) {
		logger.warn('OPENAI_API_KEY is not set - content moderation will use fallback behavior', {
			fallback: fallback || 'queue'
		});

		// If fallback is not explicitly set, default to 'queue' but require acknowledgment
		if (!fallback) {
			logger.error(
				'OPENAI_API_KEY is required in production for content moderation. ' +
				'Set MODERATION_FALLBACK=queue explicitly to acknowledge content will be queued for manual review.'
			);
		}
	}
}

function getModerationFallback(): ModerationFallback {
	const fallback = env.MODERATION_FALLBACK as ModerationFallback | undefined;

	// In production, only allow 'queue' or 'reject'
	if (!dev) {
		if (fallback === 'reject') {
			return 'reject';
		}
		// Default to 'queue' in production (never 'allow')
		return 'queue';
	}

	// In development, allow all options
	if (fallback && ['allow', 'queue', 'reject'].includes(fallback)) {
		return fallback;
	}

	// Default: allow in dev only
	return 'allow';
}

function getOpenAIClient(): OpenAI | null {
	if (!env.OPENAI_API_KEY) {
		return null;
	}
	return new OpenAI({ apiKey: env.OPENAI_API_KEY });
}

export interface ModerationResult {
	flagged: boolean;
	categories: string[];
	message?: string;
	needsReview?: boolean;
	reviewReason?: string;
}

/**
 * Queue content for manual review when moderation couldn't complete
 */
export async function queueForReview(
	contentType: 'recipe' | 'comment',
	contentId: string,
	reason: string
): Promise<void> {
	try {
		await db.insert(moderationQueue).values({
			contentType,
			contentId,
			reason
		});
	} catch (error) {
		logger.error('Failed to queue content for review', error, { contentType, contentId });
	}
}

interface ModerationInput {
	title?: string;
	description?: string;
	steps?: string[];
	imageUrl?: string;
}

/**
 * Handle moderation unavailability based on fallback setting
 */
function handleModerationUnavailable(reason: string): ModerationResult {
	const fallback = getModerationFallback();

	if (fallback === 'reject') {
		return {
			flagged: true,
			categories: ['moderation_unavailable'],
			message: 'Content moderation is temporarily unavailable. Please try again later.'
		};
	}

	if (fallback === 'queue') {
		return {
			flagged: false,
			categories: [],
			needsReview: true,
			reviewReason: reason
		};
	}

	// 'allow' - only in development
	return { flagged: false, categories: [] };
}

/**
 * Moderate content using OpenAI's moderation API
 * Returns whether content is flagged and which categories triggered
 * @param input - The content to moderate
 * @param userId - Optional user ID for rate limiting (if not provided, moderation is not rate limited)
 */
export async function moderateContent(input: ModerationInput, userId?: string): Promise<ModerationResult> {
	// Build the text content to moderate (needed for both pre-filter and API)
	const textParts: string[] = [];

	if (input.title) {
		textParts.push(input.title);
	}

	if (input.description) {
		textParts.push(input.description);
	}

	if (input.steps && input.steps.length > 0) {
		textParts.push(input.steps.join(' '));
	}

	const textContent = textParts.join(' ');

	// Run lightweight pre-filter first to catch obvious spam without API call
	// This reduces costs and provides instant feedback
	if (textContent) {
		const preFilterResult = preFilterContent(textContent);
		if (preFilterResult.flagged) {
			logger.info('Content blocked by pre-filter', {
				reason: preFilterResult.reason,
				userId
			});
			return {
				flagged: true,
				categories: [preFilterResult.reason || 'spam'],
				message: preFilterResult.message || 'Your content has been flagged. Please revise and try again.'
			};
		}
	}

	const openai = getOpenAIClient();

	// If no API key, handle based on fallback setting
	if (!openai) {
		logger.warn('OPENAI_API_KEY not set, using fallback behavior');
		return handleModerationUnavailable('api_key_missing');
	}

	// Rate limit moderation API calls per user to prevent quota exhaustion
	// This protects against malicious users spamming submissions
	if (userId) {
		const { allowed } = await rateLimit(`moderation:${userId}`, 'strict');
		if (!allowed) {
			logger.warn('Moderation rate limit exceeded', { userId });
			return handleModerationUnavailable('rate_limited');
		}
	}

	try {
		// Build formatted text for API
		const formattedTextParts: string[] = [];

		if (input.title) {
			formattedTextParts.push(`Title: ${input.title}`);
		}

		if (input.description) {
			formattedTextParts.push(`Description: ${input.description}`);
		}

		if (input.steps && input.steps.length > 0) {
			formattedTextParts.push(`Instructions: ${input.steps.join(' ')}`);
		}

		const formattedTextContent = formattedTextParts.join('\n');

		// Build moderation input
		const moderationInput: OpenAI.Moderations.ModerationMultiModalInput[] = [];

		if (formattedTextContent) {
			moderationInput.push({
				type: 'text',
				text: formattedTextContent
			});
		}

		if (input.imageUrl) {
			moderationInput.push({
				type: 'image_url',
				image_url: {
					url: input.imageUrl
				}
			});
		}

		// If nothing to moderate, return clean
		if (moderationInput.length === 0) {
			return { flagged: false, categories: [] };
		}

		const response = await openai.moderations.create({
			model: 'omni-moderation-latest',
			input: moderationInput
		});

		const result = response.results[0];

		if (!result.flagged) {
			return { flagged: false, categories: [] };
		}

		// Collect flagged categories
		const flaggedCategories: string[] = [];
		const categories = result.categories;

		for (const [category, flagged] of Object.entries(categories)) {
			if (flagged) {
				flaggedCategories.push(category);
			}
		}

		// Generate user-friendly message
		const message = generateModerationMessage(flaggedCategories);

		return {
			flagged: true,
			categories: flaggedCategories,
			message
		};
	} catch (error) {
		logger.error('Moderation API error', error);
		// Handle based on fallback setting
		return handleModerationUnavailable('api_error');
	}
}

/**
 * Generate a user-friendly message based on flagged categories
 */
function generateModerationMessage(categories: string[]): string {
	if (categories.some((c) => c.startsWith('hate'))) {
		return 'Your content contains language that may be hateful or discriminatory. Please revise and try again.';
	}

	if (categories.some((c) => c.startsWith('harassment'))) {
		return 'Your content contains language that may be harassing. Please revise and try again.';
	}

	if (categories.some((c) => c.startsWith('violence'))) {
		return 'Your content contains references to violence. Please revise and try again.';
	}

	if (categories.some((c) => c.startsWith('sexual'))) {
		return 'Your content contains inappropriate sexual content. Please revise and try again.';
	}

	if (categories.some((c) => c.startsWith('self-harm'))) {
		return 'Your content contains references to self-harm. If you are struggling, please reach out for help.';
	}

	if (categories.some((c) => c.startsWith('illicit'))) {
		return 'Your content contains references to illegal activities. Please revise and try again.';
	}

	return 'Your content has been flagged for review. Please revise and try again.';
}

/**
 * Moderate a comment (text only)
 * @param text - The comment text to moderate
 * @param userId - User ID for rate limiting moderation API calls
 */
export async function moderateComment(text: string, userId?: string): Promise<ModerationResult> {
	return moderateContent({ description: text }, userId);
}

/**
 * Moderate a recipe
 * @param title - Recipe title
 * @param description - Recipe description
 * @param steps - Recipe steps/instructions
 * @param imageUrl - Optional image URL to moderate
 * @param userId - User ID for rate limiting moderation API calls
 */
export async function moderateRecipe(
	title: string,
	description: string,
	steps: string[],
	imageUrl?: string,
	userId?: string
): Promise<ModerationResult> {
	return moderateContent({ title, description, steps, imageUrl }, userId);
}
