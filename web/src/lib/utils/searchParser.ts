/**
 * Parses a search query string to extract hashtags and text portions
 *
 * @example
 * parseSearchQuery("Soup #italian #vegetarian")
 * // Returns: { text: "Soup", hashtags: ["italian", "vegetarian"] }
 *
 * parseSearchQuery("#pizza #vegan")
 * // Returns: { text: "", hashtags: ["pizza", "vegan"] }
 *
 * parseSearchQuery("chicken pasta")
 * // Returns: { text: "chicken pasta", hashtags: [] }
 */
export interface ParsedSearchQuery {
	text: string; // Non-hashtag portion, trimmed
	hashtags: string[]; // Lowercase hashtag values without # symbol
}

export function parseSearchQuery(query: string): ParsedSearchQuery {
	if (!query || typeof query !== 'string') {
		return { text: '', hashtags: [] };
	}

	// Regex to match hashtags: # followed by word characters (letters, numbers, underscore)
	// Also supports hyphens within hashtags for things like "gluten-free"
	const hashtagRegex = /#([\w-]+)/g;

	const hashtags: string[] = [];
	let match;

	// Extract all hashtags
	while ((match = hashtagRegex.exec(query)) !== null) {
		const tag = match[1].toLowerCase();
		// Avoid duplicates
		if (!hashtags.includes(tag)) {
			hashtags.push(tag);
		}
	}

	// Remove hashtags from the query to get the text portion
	const text = query
		.replace(hashtagRegex, '') // Remove hashtags
		.replace(/\s+/g, ' ') // Normalize whitespace
		.trim();

	return { text, hashtags };
}
