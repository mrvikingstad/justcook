import OpenAI from 'openai';
import { env } from '$env/dynamic/private';

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
}

interface ModerationInput {
	title?: string;
	description?: string;
	steps?: string[];
	imageUrl?: string;
}

/**
 * Moderate content using OpenAI's moderation API
 * Returns whether content is flagged and which categories triggered
 */
export async function moderateContent(input: ModerationInput): Promise<ModerationResult> {
	const openai = getOpenAIClient();

	// If no API key, skip moderation (development mode)
	if (!openai) {
		console.warn('OPENAI_API_KEY not set, skipping moderation');
		return { flagged: false, categories: [] };
	}

	try {
		// Build the text content to moderate
		const textParts: string[] = [];

		if (input.title) {
			textParts.push(`Title: ${input.title}`);
		}

		if (input.description) {
			textParts.push(`Description: ${input.description}`);
		}

		if (input.steps && input.steps.length > 0) {
			textParts.push(`Instructions: ${input.steps.join(' ')}`);
		}

		const textContent = textParts.join('\n');

		// Build moderation input
		const moderationInput: OpenAI.Moderations.ModerationMultiModalInput[] = [];

		if (textContent) {
			moderationInput.push({
				type: 'text',
				text: textContent
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
		console.error('Moderation API error:', error);
		// On error, allow content through but log for review
		// You could also choose to reject on error for stricter moderation
		return { flagged: false, categories: [] };
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
 */
export async function moderateComment(text: string): Promise<ModerationResult> {
	return moderateContent({ description: text });
}

/**
 * Moderate a recipe
 */
export async function moderateRecipe(
	title: string,
	description: string,
	steps: string[],
	imageUrl?: string
): Promise<ModerationResult> {
	return moderateContent({ title, description, steps, imageUrl });
}
