import { S3Client, PutObjectCommand, DeleteObjectCommand } from '@aws-sdk/client-s3';
import { env } from '$env/dynamic/private';

function getStorageClient(): S3Client | null {
	if (!env.R2_ACCOUNT_ID || !env.R2_ACCESS_KEY_ID || !env.R2_SECRET_ACCESS_KEY) {
		return null;
	}

	return new S3Client({
		region: 'auto',
		endpoint: `https://${env.R2_ACCOUNT_ID}.r2.cloudflarestorage.com`,
		credentials: {
			accessKeyId: env.R2_ACCESS_KEY_ID,
			secretAccessKey: env.R2_SECRET_ACCESS_KEY
		}
	});
}

export interface UploadResult {
	success: boolean;
	url?: string;
	error?: string;
}

/**
 * Upload an image to R2 storage
 * @param file - The file buffer to upload
 * @param filename - The filename to use (will be prefixed with folder)
 * @param folder - The folder to store in (e.g., 'recipes', 'profiles')
 * @param contentType - The MIME type of the file
 */
export async function uploadImage(
	file: Buffer | Uint8Array,
	filename: string,
	folder: 'recipes' | 'profiles',
	contentType: string
): Promise<UploadResult> {
	const client = getStorageClient();

	// If no storage configured, return a placeholder (development mode)
	if (!client) {
		console.warn('R2 not configured, skipping upload');
		return { success: false, error: 'Storage not configured' };
	}

	const bucket = env.R2_BUCKET || 'justcook-images';
	const key = `${folder}/${filename}`;

	try {
		await client.send(
			new PutObjectCommand({
				Bucket: bucket,
				Key: key,
				Body: file,
				ContentType: contentType,
				CacheControl: 'public, max-age=31536000, immutable'
			})
		);

		// Construct the public URL
		const publicUrl = env.R2_PUBLIC_URL
			? `${env.R2_PUBLIC_URL}/${key}`
			: `https://${bucket}.${env.R2_ACCOUNT_ID}.r2.cloudflarestorage.com/${key}`;

		return { success: true, url: publicUrl };
	} catch (error) {
		console.error('Failed to upload to R2:', error);
		return { success: false, error: 'Upload failed' };
	}
}

/**
 * Delete an image from R2 storage
 * @param url - The full URL of the image to delete
 */
export async function deleteImage(url: string): Promise<boolean> {
	const client = getStorageClient();

	if (!client) {
		return false;
	}

	const bucket = env.R2_BUCKET || 'justcook-images';

	// Extract the key from the URL
	let key: string;
	if (env.R2_PUBLIC_URL && url.startsWith(env.R2_PUBLIC_URL)) {
		key = url.slice(env.R2_PUBLIC_URL.length + 1);
	} else {
		// Try to extract from standard R2 URL format
		const match = url.match(/\.r2\.cloudflarestorage\.com\/(.+)$/);
		if (match) {
			key = match[1];
		} else {
			console.warn('Could not extract key from URL:', url);
			return false;
		}
	}

	try {
		await client.send(
			new DeleteObjectCommand({
				Bucket: bucket,
				Key: key
			})
		);
		return true;
	} catch (error) {
		console.error('Failed to delete from R2:', error);
		return false;
	}
}

/**
 * Generate a unique filename for an upload
 */
export function generateFilename(originalName: string, userId: string): string {
	const timestamp = Date.now();
	const random = Math.random().toString(36).substring(2, 8);
	const extension = originalName.split('.').pop()?.toLowerCase() || 'jpg';
	return `${userId}-${timestamp}-${random}.${extension}`;
}

/**
 * Validate an image file
 */
export function validateImage(file: { size: number; type: string }): { valid: boolean; error?: string } {
	const maxSize = 5 * 1024 * 1024; // 5MB
	const allowedTypes = ['image/jpeg', 'image/png', 'image/webp', 'image/gif'];

	if (file.size > maxSize) {
		return { valid: false, error: 'Image must be less than 5MB' };
	}

	if (!allowedTypes.includes(file.type)) {
		return { valid: false, error: 'Image must be JPEG, PNG, WebP, or GIF' };
	}

	return { valid: true };
}
