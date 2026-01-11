import { drizzle } from 'drizzle-orm/postgres-js';
import postgres from 'postgres';
import * as schema from '../src/lib/server/db/schema';

const DATABASE_URL = process.env.DATABASE_URL;
if (!DATABASE_URL) {
	console.error('DATABASE_URL environment variable is required');
	process.exit(1);
}

const client = postgres(DATABASE_URL);
const db = drizzle(client, { schema });

// Generate a simple ID (similar to Better Auth's ID format)
function generateId(): string {
	return crypto.randomUUID().replace(/-/g, '').slice(0, 24);
}

async function seed() {
	console.log('Seeding database...\n');

	// Clear existing data
	console.log('Clearing existing data...');
	await db.delete(schema.follows);
	await db.delete(schema.votes);
	await db.delete(schema.comments);
	await db.delete(schema.steps);
	await db.delete(schema.ingredients);
	await db.delete(schema.recipes);
	// Clear Better Auth users (but preserve any real accounts - filter by test emails)
	await client`DELETE FROM "user" WHERE email LIKE '%@example.com'`;

	// Create users in Better Auth's user table with profile fields
	// Profile tiers: 'user' (default), 'author' (5+ recipes, 100+ upvotes), 'chef' (10+ recipes, 2000+ upvotes)
	console.log('Creating users with profiles...');
	const usersData = [
		{
			id: generateId(),
			email: 'priya@example.com',
			name: 'Priya Sharma',
			username: 'priya_sharma',
			fullName: 'Priya Sharma',
			country: 'India',
			bio: 'Home cook passionate about sharing authentic Indian recipes. Born in Punjab, now living in London. I believe good food brings people together.',
			profileTier: 'chef'
		},
		{
			id: generateId(),
			email: 'marcus@example.com',
			name: 'Marcus Chen',
			username: 'marcus_chen',
			fullName: 'Marcus Chen',
			country: 'Taiwan',
			bio: "Professional chef specializing in fusion cuisine. Trained in Paris, inspired by my grandmother's traditional Taiwanese cooking.",
			profileTier: 'chef'
		},
		{
			id: generateId(),
			email: 'sofia@example.com',
			name: 'Sofia Rossi',
			username: 'sofia_rossi',
			fullName: 'Sofia Rossi',
			country: 'Italy',
			bio: 'Third-generation pasta maker from Bologna. Sharing family recipes passed down through generations.',
			profileTier: 'user'
		},
		{
			id: generateId(),
			email: 'yuki@example.com',
			name: 'Yuki Tanaka',
			username: 'yuki_tanaka',
			fullName: 'Yuki Tanaka',
			country: 'Japan',
			bio: 'Japanese home cook exploring the art of simple, seasonal cooking. Less is more.',
			profileTier: 'user'
		},
		{
			id: generateId(),
			email: 'erik@example.com',
			name: 'Erik Lindqvist',
			username: 'erik_lindqvist',
			fullName: 'Erik Lindqvist',
			country: 'Sweden',
			bio: 'Swedish chef bringing Nordic comfort food to the world. Specializing in traditional Scandinavian recipes with a modern twist.',
			profileTier: 'author'
		},
		{
			id: generateId(),
			email: 'claire@example.com',
			name: 'Claire Dubois',
			username: 'claire_dubois',
			fullName: 'Claire Dubois',
			country: 'France',
			bio: 'Classically trained French chef. Bringing the art of French cuisine from Lyon to your kitchen.',
			profileTier: 'author'
		},
		{ id: generateId(), email: 'test@example.com', name: 'Test User', username: 'testuser', fullName: null, country: null, bio: null, profileTier: 'user' },
		// Extra users for follows/votes
		{ id: generateId(), email: 'user1@example.com', name: 'Anna M.', username: 'foodie_anna', fullName: null, country: null, bio: null, profileTier: 'user' },
		{ id: generateId(), email: 'user2@example.com', name: 'Bob K.', username: 'chef_bob', fullName: null, country: null, bio: null, profileTier: 'user' },
		{ id: generateId(), email: 'user3@example.com', name: 'Clara J.', username: 'home_cook_clara', fullName: null, country: null, bio: null, profileTier: 'user' },
		{ id: generateId(), email: 'user4@example.com', name: 'David L.', username: 'spice_lover', fullName: null, country: null, bio: null, profileTier: 'user' },
		{ id: generateId(), email: 'user5@example.com', name: 'Emma R.', username: 'pasta_queen', fullName: null, country: null, bio: null, profileTier: 'user' },
		{ id: generateId(), email: 'user6@example.com', name: 'Frank T.', username: 'umami_fan', fullName: null, country: null, bio: null, profileTier: 'user' },
		{ id: generateId(), email: 'user7@example.com', name: 'Grace H.', username: 'veggie_vida', fullName: null, country: null, bio: null, profileTier: 'user' },
		{ id: generateId(), email: 'user8@example.com', name: 'Henry W.', username: 'baker_henry', fullName: null, country: null, bio: null, profileTier: 'user' },
		{ id: generateId(), email: 'user9@example.com', name: 'Ivy C.', username: 'grill_master', fullName: null, country: null, bio: null, profileTier: 'user' },
		{ id: generateId(), email: 'user10@example.com', name: 'Jack N.', username: 'sushi_sam', fullName: null, country: null, bio: null, profileTier: 'user' },
		{ id: generateId(), email: 'user11@example.com', name: 'Katie P.', username: 'curry_katie', fullName: null, country: null, bio: null, profileTier: 'user' },
		{ id: generateId(), email: 'user12@example.com', name: 'Leo M.', username: 'taco_tim', fullName: null, country: null, bio: null, profileTier: 'user' },
		{ id: generateId(), email: 'user13@example.com', name: 'Mia S.', username: 'noodle_nancy', fullName: null, country: null, bio: null, profileTier: 'user' },
		{ id: generateId(), email: 'user14@example.com', name: 'Noah B.', username: 'bbq_brian', fullName: null, country: null, bio: null, profileTier: 'user' },
		{ id: generateId(), email: 'user15@example.com', name: 'Olivia D.', username: 'dessert_dan', fullName: null, country: null, bio: null, profileTier: 'user' }
	];

	// Insert users into Better Auth's user table with profile fields
	for (const userData of usersData) {
		await client`
			INSERT INTO "user" (id, name, email, username, email_verified, full_name, country, bio, profile_tier, created_at, updated_at)
			VALUES (${userData.id}, ${userData.name}, ${userData.email}, ${userData.username}, false, ${userData.fullName}, ${userData.country}, ${userData.bio}, ${userData.profileTier}, NOW(), NOW())
		`;
	}

	const [priya, marcus, sofia, yuki, erik, claire, testUser, ...extraUsers] = usersData;

	// Create recipes with recent dates for trending
	console.log('Creating recipes...');
	const now = new Date();
	const daysAgo = (days: number) => new Date(now.getTime() - days * 24 * 60 * 60 * 1000);

	const recipesData = [
		// Priya's recipes (trending - recent)
		{
			authorId: priya.id,
			title: 'Butter Chicken',
			slug: 'butter-chicken',
			description: 'Creamy, rich tomato-based curry with tender chicken pieces. A beloved classic from North India.',
			photoUrl: '/recipes/Butter-Chicken.jpg',
			prepTimeMinutes: 20,
			cookTimeMinutes: 40,
			difficulty: 'medium',
			cuisine: 'Indian',
			tag: 'curry',
			language: 'en',
			servings: 4,
			voteScore: 16,
			isPublished: true,
			publishedAt: daysAgo(5)
		},
		{
			authorId: priya.id,
			title: 'Dal Makhani',
			slug: 'dal-makhani',
			description: 'Slow-cooked black lentils in a creamy, spiced tomato sauce. Rich and deeply satisfying.',
			photoUrl: null,
			prepTimeMinutes: 15,
			cookTimeMinutes: 120,
			difficulty: 'medium',
			cuisine: 'Indian',
			tag: 'vegetarian',
			language: 'en',
			servings: 6,
			voteScore: 9,
			isPublished: true,
			publishedAt: daysAgo(12)
		},
		{
			authorId: priya.id,
			title: 'Palak Paneer',
			slug: 'palak-paneer',
			description: 'Fresh spinach curry with soft paneer cubes. A vegetarian delight.',
			photoUrl: null,
			prepTimeMinutes: 15,
			cookTimeMinutes: 25,
			difficulty: 'easy',
			cuisine: 'Indian',
			tag: 'vegetarian',
			language: 'en',
			servings: 4,
			voteScore: 0,
			isPublished: true,
			publishedAt: daysAgo(20)
		},
		// Marcus's recipes
		{
			authorId: marcus.id,
			title: 'Taiwanese Beef Noodle Soup',
			slug: 'taiwanese-beef-noodle-soup',
			description: "Soul-warming braised beef in aromatic broth with chewy noodles. Taiwan's national dish.",
			photoUrl: null,
			prepTimeMinutes: 30,
			cookTimeMinutes: 180,
			difficulty: 'hard',
			cuisine: 'Taiwanese',
			tag: 'soup',
			language: 'en',
			servings: 4,
			voteScore: 14,
			isPublished: true,
			publishedAt: daysAgo(8)
		},
		{
			authorId: marcus.id,
			title: 'Gua Bao',
			slug: 'gua-bao',
			description: 'Fluffy steamed buns filled with braised pork belly, pickled mustard greens, and fresh cilantro.',
			photoUrl: null,
			prepTimeMinutes: 45,
			cookTimeMinutes: 60,
			difficulty: 'medium',
			cuisine: 'Taiwanese',
			tag: 'comfort',
			language: 'en',
			servings: 8,
			voteScore: 6,
			isPublished: true,
			publishedAt: daysAgo(15)
		},
		// Sofia's recipes
		{
			authorId: sofia.id,
			title: 'Tagliatelle al Ragù',
			slug: 'tagliatelle-al-ragu',
			description: "The authentic Bolognese ragù, slow-simmered for hours. My grandmother's recipe.",
			photoUrl: null,
			prepTimeMinutes: 30,
			cookTimeMinutes: 240,
			difficulty: 'medium',
			cuisine: 'Italian',
			tag: 'pasta',
			language: 'en',
			servings: 6,
			voteScore: 20,
			isPublished: true,
			publishedAt: daysAgo(45)
		},
		{
			authorId: sofia.id,
			title: 'Cacio e Pepe',
			slug: 'cacio-e-pepe',
			description: 'Three ingredients, infinite satisfaction. The art of Roman simplicity.',
			photoUrl: '/recipes/cacio-e-pepe.jpg',
			prepTimeMinutes: 5,
			cookTimeMinutes: 15,
			difficulty: 'medium',
			cuisine: 'Italian',
			tag: 'pasta',
			language: 'en',
			servings: 2,
			voteScore: 13,
			isPublished: true,
			publishedAt: daysAgo(40)
		},
		// Yuki's recipes
		{
			authorId: yuki.id,
			title: 'Miso Soup',
			slug: 'miso-soup',
			description: 'Simple, nourishing miso soup with tofu and wakame. The foundation of Japanese breakfast.',
			photoUrl: '/recipes/miso-soup.jpg',
			prepTimeMinutes: 5,
			cookTimeMinutes: 10,
			difficulty: 'easy',
			cuisine: 'Japanese',
			tag: 'soup',
			language: 'en',
			servings: 4,
			voteScore: 19,
			isPublished: true,
			publishedAt: daysAgo(50)
		},
		{
			authorId: yuki.id,
			title: 'Oyakodon',
			slug: 'oyakodon',
			description: 'Chicken and egg rice bowl. Comfort food at its finest.',
			photoUrl: null,
			prepTimeMinutes: 10,
			cookTimeMinutes: 15,
			difficulty: 'easy',
			cuisine: 'Japanese',
			tag: 'quick',
			language: 'en',
			servings: 2,
			voteScore: 20,
			isPublished: true,
			publishedAt: daysAgo(55)
		},
		// Erik's recipes
		{
			authorId: erik.id,
			title: 'Swedish Meatballs',
			slug: 'swedish-meatballs',
			description: 'Classic köttbullar with creamy gravy and lingonberry. The ultimate Scandinavian comfort food.',
			photoUrl: '/recipes/swedish-meatballs.webp',
			prepTimeMinutes: 30,
			cookTimeMinutes: 25,
			difficulty: 'medium',
			cuisine: 'Swedish',
			tag: 'comfort',
			language: 'en',
			servings: 4,
			voteScore: 10,
			isPublished: true,
			publishedAt: daysAgo(10)
		},
		// Claire's recipes
		{
			authorId: claire.id,
			title: 'Beef Bourguignon',
			slug: 'beef-bourguignon',
			description: 'French braised beef in red wine with mushrooms and pearl onions. A timeless classic from Burgundy.',
			photoUrl: '/recipes/beef-bourguignon.jpg',
			prepTimeMinutes: 45,
			cookTimeMinutes: 180,
			difficulty: 'hard',
			cuisine: 'French',
			tag: 'slow-cook',
			language: 'en',
			servings: 6,
			voteScore: 13,
			isPublished: true,
			publishedAt: daysAgo(18)
		}
	];

	const insertedRecipes = await db.insert(schema.recipes).values(recipesData).returning();

	// Create ingredients for Butter Chicken
	console.log('Creating ingredients...');
	const butterChicken = insertedRecipes.find((r) => r.slug === 'butter-chicken')!;
	await db.insert(schema.ingredients).values([
		{ recipeId: butterChicken.id, name: 'chicken thighs', amount: '800', unit: 'g', sortOrder: 1 },
		{ recipeId: butterChicken.id, name: 'yogurt', amount: '200', unit: 'ml', sortOrder: 2 },
		{ recipeId: butterChicken.id, name: 'garam masala', amount: '2', unit: 'tbsp', sortOrder: 3 },
		{ recipeId: butterChicken.id, name: 'tomato puree', amount: '400', unit: 'g', sortOrder: 4 },
		{ recipeId: butterChicken.id, name: 'heavy cream', amount: '200', unit: 'ml', sortOrder: 5 },
		{ recipeId: butterChicken.id, name: 'butter', amount: '100', unit: 'g', sortOrder: 6 },
		{ recipeId: butterChicken.id, name: 'garlic', amount: '4', unit: 'cloves', sortOrder: 7 },
		{ recipeId: butterChicken.id, name: 'ginger', amount: '2', unit: 'inch', sortOrder: 8 },
		{ recipeId: butterChicken.id, name: 'kashmiri chili powder', amount: '1', unit: 'tbsp', sortOrder: 9 },
		{ recipeId: butterChicken.id, name: 'salt', amount: '1', unit: 'tsp', sortOrder: 10 }
	]);

	// Create steps for Butter Chicken
	console.log('Creating steps...');
	await db.insert(schema.steps).values([
		{ recipeId: butterChicken.id, stepNumber: 1, instruction: 'Cut chicken into bite-sized pieces. Marinate with yogurt, half the garam masala, and salt for at least 30 minutes.' },
		{ recipeId: butterChicken.id, stepNumber: 2, instruction: 'Heat butter in a large pan. Add minced garlic and ginger, sauté until fragrant.' },
		{ recipeId: butterChicken.id, stepNumber: 3, instruction: 'Add the marinated chicken and cook until browned on all sides.' },
		{ recipeId: butterChicken.id, stepNumber: 4, instruction: 'Add tomato puree, remaining garam masala, and kashmiri chili powder. Simmer for 20 minutes.' },
		{ recipeId: butterChicken.id, stepNumber: 5, instruction: 'Stir in heavy cream and simmer for another 10 minutes until sauce thickens.' },
		{ recipeId: butterChicken.id, stepNumber: 6, instruction: 'Adjust seasoning and serve hot with naan or rice.' }
	]);

	// Add ingredients for Cacio e Pepe
	const cacioEPepe = insertedRecipes.find((r) => r.slug === 'cacio-e-pepe')!;
	await db.insert(schema.ingredients).values([
		{ recipeId: cacioEPepe.id, name: 'spaghetti', amount: '200', unit: 'g', sortOrder: 1 },
		{ recipeId: cacioEPepe.id, name: 'pecorino romano', amount: '150', unit: 'g', sortOrder: 2 },
		{ recipeId: cacioEPepe.id, name: 'black pepper', amount: '2', unit: 'tbsp', sortOrder: 3 }
	]);

	await db.insert(schema.steps).values([
		{ recipeId: cacioEPepe.id, stepNumber: 1, instruction: 'Bring a large pot of salted water to boil. Cook spaghetti until al dente, reserving 2 cups pasta water.' },
		{ recipeId: cacioEPepe.id, stepNumber: 2, instruction: 'Toast freshly cracked black pepper in a dry pan until fragrant.' },
		{ recipeId: cacioEPepe.id, stepNumber: 3, instruction: 'Finely grate the pecorino romano.' },
		{ recipeId: cacioEPepe.id, stepNumber: 4, instruction: 'Add pasta to the pepper with a ladle of pasta water. Toss vigorously.' },
		{ recipeId: cacioEPepe.id, stepNumber: 5, instruction: 'Remove from heat, add pecorino gradually while tossing to create a creamy sauce. Add more pasta water if needed.' }
	]);

	// Create follows with recent timestamps for trending chefs
	console.log('Creating follows...');
	const recentFollows = [
		// Sofia gets the most new followers this week (8)
		...extraUsers.slice(0, 8).map((u) => ({
			followerId: u.id,
			followingId: sofia.id,
			createdAt: daysAgo(Math.floor(Math.random() * 5))
		})),
		// Priya gets 6 new followers
		...extraUsers.slice(0, 6).map((u) => ({
			followerId: u.id,
			followingId: priya.id,
			createdAt: daysAgo(Math.floor(Math.random() * 6))
		})),
		// Marcus gets 4 new followers
		...extraUsers.slice(0, 4).map((u) => ({
			followerId: u.id,
			followingId: marcus.id,
			createdAt: daysAgo(Math.floor(Math.random() * 5))
		})),
		// Yuki gets 2 new followers
		...extraUsers.slice(0, 2).map((u) => ({
			followerId: u.id,
			followingId: yuki.id,
			createdAt: daysAgo(Math.floor(Math.random() * 4))
		})),
		// Test user follows
		{ followerId: testUser.id, followingId: priya.id, createdAt: daysAgo(2) },
		{ followerId: testUser.id, followingId: marcus.id, createdAt: daysAgo(3) },
		{ followerId: testUser.id, followingId: sofia.id, createdAt: daysAgo(1) }
	];

	// Remove duplicates (same follower-following pair)
	const uniqueFollows = recentFollows.filter(
		(f, i, arr) =>
			arr.findIndex((x) => x.followerId === f.followerId && x.followingId === f.followingId) === i
	);
	await db.insert(schema.follows).values(uniqueFollows);

	// Create votes with mix of upvotes and downvotes
	console.log('Creating votes...');
	const allUsers = [testUser, priya, marcus, sofia, yuki, erik, claire, ...extraUsers];
	const tagliatelle = insertedRecipes.find((r) => r.slug === 'tagliatelle-al-ragu')!;
	const misoSoup = insertedRecipes.find((r) => r.slug === 'miso-soup')!;
	const oyakodon = insertedRecipes.find((r) => r.slug === 'oyakodon')!;
	const swedishMeatballs = insertedRecipes.find((r) => r.slug === 'swedish-meatballs')!;
	const beefBourguignon = insertedRecipes.find((r) => r.slug === 'beef-bourguignon')!;
	const dalMakhani = insertedRecipes.find((r) => r.slug === 'dal-makhani')!;
	const guaBao = insertedRecipes.find((r) => r.slug === 'gua-bao')!;
	const beefNoodleSoup = insertedRecipes.find((r) => r.slug === 'taiwanese-beef-noodle-soup')!;

	const voteData = [
		// Butter Chicken - 18 upvotes, 2 downvotes (90%)
		...allUsers.slice(0, 20).map((u, i) => ({
			recipeId: butterChicken.id,
			userId: u.id,
			value: i < 18 ? 1 : -1
		})),
		// Cacio e Pepe - 14 upvotes, 1 downvote (93%)
		...allUsers.slice(0, 15).map((u, i) => ({
			recipeId: cacioEPepe.id,
			userId: u.id,
			value: i < 14 ? 1 : -1
		})),
		// Tagliatelle - 22 upvotes, 2 downvotes (91% ratio)
		...allUsers.slice(0, 24).map((u, i) => ({
			recipeId: tagliatelle.id,
			userId: u.id,
			value: i < 22 ? 1 : -1
		})),
		// Miso Soup - 22 upvotes, 3 downvotes (88% ratio)
		...allUsers.slice(0, 25).map((u, i) => ({
			recipeId: misoSoup.id,
			userId: u.id,
			value: i < 22 ? 1 : -1
		})),
		// Oyakodon - 21 upvotes, 1 downvote (95% ratio)
		...allUsers.slice(0, 22).map((u, i) => ({
			recipeId: oyakodon.id,
			userId: u.id,
			value: i < 21 ? 1 : -1
		})),
		// Swedish Meatballs - 12 upvotes, 2 downvotes (86%)
		...allUsers.slice(0, 14).map((u, i) => ({
			recipeId: swedishMeatballs.id,
			userId: u.id,
			value: i < 12 ? 1 : -1
		})),
		// Beef Bourguignon - 16 upvotes, 3 downvotes (84%)
		...allUsers.slice(0, 19).map((u, i) => ({
			recipeId: beefBourguignon.id,
			userId: u.id,
			value: i < 16 ? 1 : -1
		})),
		// Dal Makhani - 10 upvotes, 1 downvote (91%)
		...allUsers.slice(0, 11).map((u, i) => ({
			recipeId: dalMakhani.id,
			userId: u.id,
			value: i < 10 ? 1 : -1
		})),
		// Gua Bao - 8 upvotes, 2 downvotes (80%)
		...allUsers.slice(0, 10).map((u, i) => ({
			recipeId: guaBao.id,
			userId: u.id,
			value: i < 8 ? 1 : -1
		})),
		// Taiwanese Beef Noodle Soup - 15 upvotes, 1 downvote (94%)
		...allUsers.slice(0, 16).map((u, i) => ({
			recipeId: beefNoodleSoup.id,
			userId: u.id,
			value: i < 15 ? 1 : -1
		}))
	];

	// Remove duplicates and filter out self-votes from recipe authors
	const uniqueVotes = voteData.filter((v, i, arr) => {
		const recipe = insertedRecipes.find((r) => r.id === v.recipeId);
		if (recipe && recipe.authorId === v.userId) return false;
		return arr.findIndex((x) => x.recipeId === v.recipeId && x.userId === v.userId) === i;
	});
	await db.insert(schema.votes).values(uniqueVotes);

	console.log('\nSeeding complete!');
	console.log('Created:');
	console.log('  - 22 users with profile data (in Better Auth user table)');
	console.log('  - 11 recipes (with images where available)');
	console.log('  - Ingredients and steps for select recipes');
	console.log(`  - ${uniqueFollows.length} follow relationships (recent for trending)`);
	console.log(`  - ${uniqueVotes.length} votes (for discover section)`);
	console.log('\nVisit the home page to see trending chefs and discover recipes!');

	await client.end();
}

seed().catch((err) => {
	console.error('Seeding failed:', err);
	process.exit(1);
});
