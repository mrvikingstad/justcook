/**
 * Recipe input validation utilities
 * Provides whitelist validation for cuisine, language, difficulty, and time bounds
 */

import { cuisines } from '$lib/data/cuisines';
import { languages } from '$lib/data/languages';

// Valid difficulty values
export const VALID_DIFFICULTIES = ['easy', 'medium', 'hard'] as const;
export type Difficulty = (typeof VALID_DIFFICULTIES)[number];

// Time bounds (in minutes)
export const TIME_BOUNDS = {
	MIN_PREP_TIME: 1,
	MAX_PREP_TIME: 1440, // 24 hours
	MIN_COOK_TIME: 0, // Some recipes don't require cooking
	MAX_COOK_TIME: 4320, // 72 hours (for slow-cooked dishes)
	MIN_SERVINGS: 1,
	MAX_SERVINGS: 100
} as const;

// Create Sets for O(1) lookup
const cuisineSet = new Set(cuisines.map((c) => c.toLowerCase()));
const languageCodeSet = new Set(languages.map((l) => l.code.toLowerCase()));
const difficultySet = new Set(VALID_DIFFICULTIES);

/**
 * Validates that a cuisine is in the allowed list (case-insensitive)
 */
export function isValidCuisine(cuisine: string): boolean {
	return cuisineSet.has(cuisine.toLowerCase());
}

/**
 * Validates that a language code is in the allowed list
 */
export function isValidLanguageCode(code: string): boolean {
	return languageCodeSet.has(code.toLowerCase());
}

/**
 * Validates that a difficulty is in the allowed list
 */
export function isValidDifficulty(difficulty: string): difficulty is Difficulty {
	return difficultySet.has(difficulty as Difficulty);
}

/**
 * Validates prep time is within acceptable bounds
 */
export function isValidPrepTime(minutes: number): boolean {
	return (
		typeof minutes === 'number' &&
		Number.isFinite(minutes) &&
		minutes >= TIME_BOUNDS.MIN_PREP_TIME &&
		minutes <= TIME_BOUNDS.MAX_PREP_TIME
	);
}

/**
 * Validates cook time is within acceptable bounds
 */
export function isValidCookTime(minutes: number): boolean {
	return (
		typeof minutes === 'number' &&
		Number.isFinite(minutes) &&
		minutes >= TIME_BOUNDS.MIN_COOK_TIME &&
		minutes <= TIME_BOUNDS.MAX_COOK_TIME
	);
}

/**
 * Validates servings is within acceptable bounds
 */
export function isValidServings(servings: number): boolean {
	return (
		typeof servings === 'number' &&
		Number.isInteger(servings) &&
		servings >= TIME_BOUNDS.MIN_SERVINGS &&
		servings <= TIME_BOUNDS.MAX_SERVINGS
	);
}

/**
 * Validates a URL is a valid HTTP/HTTPS URL
 */
export function isValidUrl(urlString: string): boolean {
	try {
		const url = new URL(urlString);
		return url.protocol === 'http:' || url.protocol === 'https:';
	} catch {
		return false;
	}
}

/**
 * Recipe validation error messages
 */
export const VALIDATION_ERRORS = {
	INVALID_CUISINE: `Invalid cuisine. Must be one of: ${cuisines.slice(0, 5).join(', ')}...`,
	INVALID_LANGUAGE: 'Invalid language code',
	INVALID_DIFFICULTY: `Invalid difficulty. Must be one of: ${VALID_DIFFICULTIES.join(', ')}`,
	PREP_TIME_TOO_LOW: `Prep time must be at least ${TIME_BOUNDS.MIN_PREP_TIME} minute`,
	PREP_TIME_TOO_HIGH: `Prep time cannot exceed ${TIME_BOUNDS.MAX_PREP_TIME} minutes (24 hours)`,
	COOK_TIME_TOO_LOW: `Cook time cannot be negative`,
	COOK_TIME_TOO_HIGH: `Cook time cannot exceed ${TIME_BOUNDS.MAX_COOK_TIME} minutes (72 hours)`,
	SERVINGS_TOO_LOW: `Servings must be at least ${TIME_BOUNDS.MIN_SERVINGS}`,
	SERVINGS_TOO_HIGH: `Servings cannot exceed ${TIME_BOUNDS.MAX_SERVINGS}`
} as const;
