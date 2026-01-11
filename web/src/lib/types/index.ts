export interface User {
	id: string;
	email: string;
	name: string | null;
	avatarUrl: string | null;
}

export interface Recipe {
	id: string;
	authorId: string;
	title: string;
	slug: string;
	description: string | null;
	photoUrl: string | null;
	prepTimeMinutes: number | null;
	cookTimeMinutes: number | null;
	difficulty: 'easy' | 'medium' | 'hard' | null;
	language: string;
	servings: number;
	voteScore: number;
	commentCount: number;
	isPublished: boolean;
	publishedAt: Date | null;
	createdAt: Date;
	updatedAt: Date;
}

export interface Ingredient {
	id: string;
	recipeId: string;
	name: string;
	amount: number | null;
	unit: string | null;
	sortOrder: number;
	notes: string | null;
}

export interface Step {
	id: string;
	recipeId: string;
	stepNumber: number;
	instruction: string;
}

export interface Category {
	id: string;
	name: string;
	slug: string;
}

export interface Comment {
	id: string;
	recipeId: string;
	userId: string;
	content: string;
	createdAt: Date;
	updatedAt: Date;
	user?: Pick<User, 'id' | 'name' | 'avatarUrl'>;
}

export interface RecipeWithAuthor extends Recipe {
	author: Pick<User, 'id' | 'name' | 'avatarUrl'>;
}

export interface RecipeWithDetails extends RecipeWithAuthor {
	ingredients: Ingredient[];
	steps: Step[];
	categories: Category[];
}
