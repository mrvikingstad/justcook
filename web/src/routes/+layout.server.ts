import type { LayoutServerLoad } from './$types';
import { db } from '$lib/server/db';
import { user } from '$lib/server/db/schema';
import { eq } from 'drizzle-orm';
import { getCountry } from '$lib/data/countries';

export const load: LayoutServerLoad = async ({ locals }) => {
	let userLanguageCode: string | null = null;

	// Derive user's language from their country setting
	if (locals.user?.id) {
		const [userData] = await db
			.select({ country: user.country })
			.from(user)
			.where(eq(user.id, locals.user.id))
			.limit(1);

		if (userData?.country) {
			const countryDef = getCountry(userData.country);
			userLanguageCode = countryDef?.language || null;
		}
	}

	return {
		user: locals.user,
		userLanguageCode
	};
};
