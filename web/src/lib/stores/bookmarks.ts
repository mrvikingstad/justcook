import { writable, get } from 'svelte/store';
import { browser } from '$app/environment';

// Local storage key for anonymous bookmarks
const LOCAL_STORAGE_KEY = 'bookmarks';

// Initialize from localStorage for immediate display
const stored = browser ? JSON.parse(localStorage.getItem(LOCAL_STORAGE_KEY) || '[]') : [];
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
		} catch (error) {
			console.error('Failed to fetch bookmarks:', error);
		}
	} else if (!authenticated) {
		// Reset to localStorage for logged out users
		const local = JSON.parse(localStorage.getItem(LOCAL_STORAGE_KEY) || '[]');
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
		} catch (error) {
			console.error('Failed to toggle bookmark:', error);
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
