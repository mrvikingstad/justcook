import { json } from '@sveltejs/kit';
import type { RequestHandler } from './$types';
import { uploadImage, generateFilename, validateImage } from '$lib/server/storage';

export const POST: RequestHandler = async ({ request, locals }) => {
	if (!locals.user) {
		return json({ error: 'Unauthorized' }, { status: 401 });
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

		// Generate unique filename
		const filename = generateFilename(file.name, locals.user.id);

		// Upload to R2
		const result = await uploadImage(buffer, filename, folder, file.type);

		if (!result.success) {
			return json({ error: result.error || 'Upload failed' }, { status: 500 });
		}

		return json({ success: true, url: result.url });
	} catch (error) {
		console.error('Upload error:', error);
		return json({ error: 'Failed to process upload' }, { status: 500 });
	}
};
