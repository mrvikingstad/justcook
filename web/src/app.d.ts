// See https://svelte.dev/docs/kit/types#app.d.ts
import type { Session } from '$lib/server/auth';

declare global {
	namespace App {
		interface Locals {
			user: Session['user'] | null;
			session: Session['session'] | null;
		}
		interface PageData {
			user: Session['user'] | null;
		}
	}
}

export {};
