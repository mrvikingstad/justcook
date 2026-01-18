/**
 * Script to sync migration history with the database
 * This marks all migrations in the journal as applied in the drizzle migrations table
 *
 * Run with: bun run scripts/sync-migrations.ts
 */

import postgres from 'postgres';
import journal from '../drizzle/meta/_journal.json';

const DATABASE_URL = process.env.DATABASE_URL;

if (!DATABASE_URL) {
	console.error('DATABASE_URL environment variable is required');
	process.exit(1);
}

const sql = postgres(DATABASE_URL);

async function syncMigrations() {
	console.log('Syncing migration history...\n');

	// Ensure the drizzle schema and migrations table exist
	await sql`CREATE SCHEMA IF NOT EXISTS drizzle`;
	await sql`
		CREATE TABLE IF NOT EXISTS drizzle.__drizzle_migrations (
			id SERIAL PRIMARY KEY,
			hash TEXT NOT NULL,
			created_at BIGINT
		)
	`;

	// Get existing migrations from the database
	const existing = await sql<{ hash: string }[]>`
		SELECT hash FROM drizzle.__drizzle_migrations
	`;
	const existingHashes = new Set(existing.map(m => m.hash));

	console.log(`Found ${existingHashes.size} migrations in database`);
	console.log(`Found ${journal.entries.length} migrations in journal\n`);

	// Insert missing migrations
	let inserted = 0;
	for (const entry of journal.entries) {
		const hash = entry.tag;
		if (!existingHashes.has(hash)) {
			await sql`
				INSERT INTO drizzle.__drizzle_migrations (hash, created_at)
				VALUES (${hash}, ${entry.when})
			`;
			console.log(`✓ Marked as applied: ${hash}`);
			inserted++;
		} else {
			console.log(`  Already tracked: ${hash}`);
		}
	}

	console.log(`\nDone! Inserted ${inserted} migration records.`);

	await sql.end();
}

syncMigrations().catch(err => {
	console.error('Error:', err);
	process.exit(1);
});
