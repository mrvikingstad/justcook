import { S3Client, PutObjectCommand, DeleteObjectCommand } from '@aws-sdk/client-s3';
import { env } from '$env/dynamic/private';
import { logger } from '$lib/server/logger';

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
		logger.warn('R2 not configured, skipping upload');
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
		logger.error('Failed to upload to R2', error, { folder, filename });
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
			logger.warn('Could not extract key from URL:', url);
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
		logger.error('Failed to delete from R2', error, { url });
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
 * Validate an image file (basic metadata check)
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

/**
 * Image format magic bytes signatures
 */
const IMAGE_SIGNATURES = {
	jpeg: {
		bytes: [0xff, 0xd8, 0xff],
		offset: 0
	},
	png: {
		bytes: [0x89, 0x50, 0x4e, 0x47, 0x0d, 0x0a, 0x1a, 0x0a],
		offset: 0
	},
	gif: {
		bytes: [0x47, 0x49, 0x46, 0x38], // GIF8
		offset: 0
	},
	webp: {
		riff: [0x52, 0x49, 0x46, 0x46], // RIFF at offset 0
		webp: [0x57, 0x45, 0x42, 0x50], // WEBP at offset 8
		offset: 0
	}
} as const;

/**
 * Check if bytes at a given offset match a signature
 */
function matchesSignature(data: Uint8Array, signature: readonly number[], offset: number): boolean {
	if (data.length < offset + signature.length) {
		return false;
	}
	for (let i = 0; i < signature.length; i++) {
		if (data[offset + i] !== signature[i]) {
			return false;
		}
	}
	return true;
}

/**
 * Detect image type from magic bytes
 */
function detectImageType(data: Uint8Array): string | null {
	// Check JPEG
	if (matchesSignature(data, IMAGE_SIGNATURES.jpeg.bytes, 0)) {
		return 'image/jpeg';
	}

	// Check PNG
	if (matchesSignature(data, IMAGE_SIGNATURES.png.bytes, 0)) {
		return 'image/png';
	}

	// Check GIF
	if (matchesSignature(data, IMAGE_SIGNATURES.gif.bytes, 0)) {
		return 'image/gif';
	}

	// Check WebP (RIFF....WEBP format)
	if (
		matchesSignature(data, IMAGE_SIGNATURES.webp.riff, 0) &&
		matchesSignature(data, IMAGE_SIGNATURES.webp.webp, 8)
	) {
		return 'image/webp';
	}

	return null;
}

/**
 * Validate image magic bytes to prevent file type spoofing
 * This should be called AFTER basic validation and BEFORE upload
 */
export function validateImageMagicBytes(
	data: Uint8Array,
	claimedType: string
): { valid: boolean; detectedType: string | null; error?: string } {
	const detectedType = detectImageType(data);

	if (!detectedType) {
		return {
			valid: false,
			detectedType: null,
			error: 'File does not appear to be a valid image'
		};
	}

	// Optionally verify that detected type matches claimed type
	// We allow some flexibility (e.g., image/jpg vs image/jpeg)
	const normalizeType = (type: string) => type.replace('image/jpg', 'image/jpeg');
	const normalizedClaimed = normalizeType(claimedType);
	const normalizedDetected = normalizeType(detectedType);

	if (normalizedClaimed !== normalizedDetected) {
		return {
			valid: false,
			detectedType,
			error: `File content (${detectedType}) does not match claimed type (${claimedType})`
		};
	}

	return { valid: true, detectedType };
}
