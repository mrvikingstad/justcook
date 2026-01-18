import { drizzle } from 'drizzle-orm/postgres-js';
import postgres from 'postgres';
import { DATABASE_URL } from '$env/static/private';
import * as schema from './schema';

const client = postgres(DATABASE_URL, {
	max: 25,
	idle_timeout: 30,
	connect_timeout: 10,
	prepare: true,
	// Query timeout: 30 seconds max per statement to prevent hanging queries
	// This protects against slow queries exhausting the connection pool
	options: {
		statement_timeout: '30000' // 30 seconds in milliseconds
	}
});

export const db = drizzle(client, { schema });
