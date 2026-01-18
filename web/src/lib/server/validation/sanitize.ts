/**
 * Server-side HTML sanitization utilities
 * Prevents XSS attacks by stripping/escaping HTML from user input
 */

import DOMPurify from 'isomorphic-dompurify';

/**
 * Strip all HTML tags from text - use for plain text fields
 * (names, titles, comments that shouldn't contain any HTML)
 */
export function sanitizeText(input: string): string {
	// Remove all HTML tags, leaving only plain text
	return DOMPurify.sanitize(input, { ALLOWED_TAGS: [] }).trim();
}

/**
 * Sanitize with basic formatting allowed - use for rich text fields
 * (descriptions that might need basic formatting)
 */
export function sanitizeRichText(input: string): string {
	return DOMPurify.sanitize(input, {
		ALLOWED_TAGS: ['b', 'i', 'em', 'strong', 'br'],
		ALLOWED_ATTR: []
	}).trim();
}

/**
 * Escape HTML entities without removing tags - use when you need to display
 * user input as literal text (e.g., code snippets)
 */
export function escapeHtml(input: string): string {
	return input
		.replace(/&/g, '&amp;')
		.replace(/</g, '&lt;')
		.replace(/>/g, '&gt;')
		.replace(/"/g, '&quot;')
		.replace(/'/g, '&#039;');
}
