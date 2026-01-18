import { json } from '@sveltejs/kit';
import type { RequestHandler } from './$types';
import { uploadImage, generateFilename, validateImage, validateImageMagicBytes } from '$lib/server/storage';
import { logger, getRequestId } from '$lib/server/logger';
import { rateLimit } from '$lib/server/rateLimit';

export const POST: RequestHandler = async ({ request, locals, getClientAddress }) => {
	if (!locals.user) {
		return json({ error: 'Unauthorized' }, { status: 401 });
	}

	// Per-user rate limiting: combine userId + IP to prevent
	// - Single user exhausting quota (userId key)
	// - Users behind same NAT sharing limits unfairly (IP key)
	const clientIp = getClientAddress();
	const rateLimitKey = `upload:${locals.user.id}:${clientIp}`;
	const { allowed, remaining, resetIn } = await rateLimit(rateLimitKey, 'upload');

	if (!allowed) {
		return json(
			{ error: 'Upload rate limit exceeded. Please try again later.' },
			{
				status: 429,
				headers: {
					'Retry-After': String(Math.ceil(resetIn / 1000)),
					'X-RateLimit-Remaining': String(remaining),
					'X-RateLimit-Reset': String(Math.ceil(resetIn / 1000))
				}
			}
		);
	}

	try {
		const formData = await request.formData();
		const file = formData.get('file') as File | null;
		const folder = formData.get('folder') as 'recipes' | 'profiles' | null;

		if (!file) {
			return json({ error: 'No file provided' }, { status: 400 });
		}

		if (!folder || (folder !== 'recipes' && folder !== 'profiles')) {
			return json({ error: 'Invalid folder. Must be "recipes" or "profiles"' }, { status: 400 });
		}

		// Validate the image
		const validation = validateImage({ size: file.size, type: file.type });
		if (!validation.valid) {
			return json({ error: validation.error }, { status: 400 });
		}

		// Convert file to buffer
		const arrayBuffer = await file.arrayBuffer();
		const buffer = new Uint8Array(arrayBuffer);

		// Validate magic bytes to prevent file type spoofing
		const magicBytesValidation = validateImageMagicBytes(buffer, file.type);
		if (!magicBytesValidation.valid) {
			return json({ error: magicBytesValidation.error }, { status: 400 });
		}

		// Generate unique filename
		const filename = generateFilename(file.name, locals.user.id);

		// Upload to R2
		const result = await uploadImage(buffer, filename, folder, file.type);

		if (!result.success) {
			return json({ error: result.error || 'Upload failed' }, { status: 500 });
		}

		return json({ success: true, url: result.url });
	} catch (error) {
		logger.error('Upload failed', error, { userId: locals.user.id });
		return json({ error: 'Failed to process upload', requestId: getRequestId() }, { status: 500 });
	}
};
