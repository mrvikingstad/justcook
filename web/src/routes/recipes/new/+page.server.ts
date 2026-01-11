import { redirect } from '@sveltejs/kit';
import type { PageServerLoad } from './$types';
import { db } from '$lib/server/db';
import { user } from '$lib/server/db/schema';
import { eq } from 'drizzle-orm';
import { getCountryByName } from '$lib/data/countries';

export const load: PageServerLoad = async ({ locals }) => {
	if (!locals.user) {
		throw redirect(302, '/auth/login?redirect=/recipes/new');
	}

	// Fetch user's country from their profile
	const [userData] = await db
		.select({ country: user.country })
		.from(user)
		.where(eq(user.id, locals.user.id))
		.limit(1);

	// Derive language code from country
	let userLanguageCode: string | null = null;
	if (userData?.country) {
		const countryDef = getCountryByName(userData.country);
		userLanguageCode = countryDef?.language || null;
	}

	return {
		user: locals.user,
		userLanguageCode
	};
};
