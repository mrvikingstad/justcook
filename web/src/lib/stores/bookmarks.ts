import { writable, get } from 'svelte/store';
import { browser } from '$app/environment';

// Local storage key for anonymous bookmarks
const LOCAL_STORAGE_KEY = 'bookmarks';

// Initialize from localStorage for immediate display
// Wrapped in try-catch to handle corrupted localStorage data
let stored: string[] = [];
if (browser) {
	try {
		stored = JSON.parse(localStorage.getItem(LOCAL_STORAGE_KEY) || '[]');
		if (!Array.isArray(stored)) {
			stored = [];
		}
	} catch {
		// Corrupted localStorage data - clear it and start fresh
		localStorage.removeItem(LOCAL_STORAGE_KEY);
		stored = [];
	}
}
export const bookmarks = writable<string[]>(stored);

// Track if we've synced with server
let isAuthenticated = false;
let hasSyncedWithServer = false;

// Save to localStorage as backup
bookmarks.subscribe((value) => {
	if (browser) {
		localStorage.setItem(LOCAL_STORAGE_KEY, JSON.stringify(value));
	}
});

// Initialize bookmarks from server (call this on app load when user is authenticated)
export async function initBookmarks(authenticated: boolean) {
	isAuthenticated = authenticated;

	if (!browser) return;

	if (authenticated && !hasSyncedWithServer) {
		try {
			const response = await fetch('/api/bookmarks');
			if (response.ok) {
				const data = await response.json();
				bookmarks.set(data.bookmarks || []);
				hasSyncedWithServer = true;
			}
		} catch {
			// Silently fail - user will see stale localStorage data
		}
	} else if (!authenticated) {
		// Reset to localStorage for logged out users
		let local: string[] = [];
		try {
			const parsed = JSON.parse(localStorage.getItem(LOCAL_STORAGE_KEY) || '[]');
			local = Array.isArray(parsed) ? parsed : [];
		} catch {
			localStorage.removeItem(LOCAL_STORAGE_KEY);
		}
		bookmarks.set(local);
		hasSyncedWithServer = false;
	}
}

// Toggle bookmark - syncs with server if authenticated
export async function toggleBookmark(recipeSlug: string): Promise<boolean> {
	const currentBookmarks = get(bookmarks);
	const isCurrentlyBookmarked = currentBookmarks.includes(recipeSlug);

	// Optimistic update
	if (isCurrentlyBookmarked) {
		bookmarks.update((list) => list.filter((slug) => slug !== recipeSlug));
	} else {
		bookmarks.update((list) => [...list, recipeSlug]);
	}

	// If authenticated, sync with server
	if (isAuthenticated) {
		try {
			const response = await fetch('/api/bookmarks', {
				method: 'POST',
				headers: { 'Content-Type': 'application/json' },
				body: JSON.stringify({ recipeSlug })
			});

			if (!response.ok) {
				// Revert on error
				if (isCurrentlyBookmarked) {
					bookmarks.update((list) => [...list, recipeSlug]);
				} else {
					bookmarks.update((list) => list.filter((slug) => slug !== recipeSlug));
				}
				return isCurrentlyBookmarked;
			}

			const data = await response.json();
			return data.bookmarked;
		} catch {
			// Revert on error
			if (isCurrentlyBookmarked) {
				bookmarks.update((list) => [...list, recipeSlug]);
			} else {
				bookmarks.update((list) => list.filter((slug) => slug !== recipeSlug));
			}
			return isCurrentlyBookmarked;
		}
	}

	return !isCurrentlyBookmarked;
}

export function isBookmarked(recipeSlug: string, bookmarkList: string[]): boolean {
	return bookmarkList.includes(recipeSlug);
}
