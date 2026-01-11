import { writable, get } from 'svelte/store';
import { browser } from '$app/environment';

export type LanguageMode = 'global' | 'local';

const STORAGE_KEY = 'languageMode';

// Initialize from localStorage
const stored = browser ? (localStorage.getItem(STORAGE_KEY) as LanguageMode) : null;
export const languageMode = writable<LanguageMode>(stored || 'global');

// User's local language code (derived from country, set by layout)
export const userLanguageCode = writable<string | null>(null);

// Persist to localStorage
languageMode.subscribe((value) => {
	if (browser) {
		localStorage.setItem(STORAGE_KEY, value);
	}
});

// Toggle between global and local
export function toggleLanguageMode() {
	languageMode.update((mode) => (mode === 'global' ? 'local' : 'global'));
}

// Get the effective language code based on current mode
export function getEffectiveLanguage(): string {
	const mode = get(languageMode);
	const userLang = get(userLanguageCode);

	if (mode === 'local' && userLang) {
		return userLang;
	}
	return 'en'; // Global = English
}

// Initialize user language from layout data
export function initUserLanguage(langCode: string | null) {
	userLanguageCode.set(langCode);
}
