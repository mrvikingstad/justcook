import { writable } from 'svelte/store';
import { browser } from '$app/environment';

type Theme = 'light' | 'dark';

const stored = browser ? (localStorage.getItem('theme') as Theme) : null;
const prefersDark = browser ? window.matchMedia('(prefers-color-scheme: dark)').matches : false;
const initial: Theme = stored || (prefersDark ? 'dark' : 'light');

export const theme = writable<Theme>(initial);

theme.subscribe((value) => {
	if (browser) {
		localStorage.setItem('theme', value);
		document.documentElement.classList.toggle('dark', value === 'dark');
	}
});

export function toggleTheme() {
	theme.update((t) => (t === 'light' ? 'dark' : 'light'));
}
