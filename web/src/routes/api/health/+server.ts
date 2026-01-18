import { json } from '@sveltejs/kit';
import type { RequestHandler } from './$types';
import { db } from '$lib/server/db';
import { sql } from 'drizzle-orm';

export const GET: RequestHandler = async () => {
	const health = {
		status: 'healthy',
		timestamp: new Date().toISOString(),
		checks: {
			database: false
		}
	};

	try {
		await db.execute(sql`SELECT 1`);
		health.checks.database = true;
	} catch {
		health.status = 'unhealthy';
	}

	const statusCode = health.status === 'healthy' ? 200 : 503;

	return json(health, { status: statusCode });
};
