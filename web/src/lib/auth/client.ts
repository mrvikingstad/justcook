import { createAuthClient } from 'better-auth/svelte';
import { magicLinkClient, usernameClient } from 'better-auth/client/plugins';

export const authClient = createAuthClient({
	baseURL: typeof window !== 'undefined' ? window.location.origin : '',
	plugins: [magicLinkClient(), usernameClient()]
});

export const {
	signIn,
	signUp,
	signOut,
	useSession,
	magicLink
} = authClient;
