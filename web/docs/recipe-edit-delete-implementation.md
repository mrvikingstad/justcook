# Recipe Edit/Delete Implementation

This document describes the implementation of recipe editing and deletion for JustCook.

## Overview

Recipe authors can now edit and delete their own recipes. The recipe detail page shows timestamps for when the recipe was published and last edited.

## Files Created

- `src/routes/api/recipes/[id]/+server.ts` - PUT/DELETE API endpoints
- `src/routes/recipes/[slug]/edit/+page.server.ts` - Edit page data loader
- `src/routes/recipes/[slug]/edit/+page.svelte` - Edit page component

## Files Modified

- `src/routes/recipes/[slug]/+page.server.ts` - Added timestamps and isOwn flag
- `src/routes/recipes/[slug]/+page.svelte` - Added timestamps display, edit/delete buttons

---

## API Endpoints

### `PUT /api/recipes/[id]`

Updates an existing recipe.

**Authorization:** User must be the recipe author.

**Request Body:**
```json
{
    "title": "Updated Recipe Title",
    "description": "Updated description",
    "cuisine": "Italian",
    "tag": "pasta",
    "difficulty": "medium",
    "prepTime": 20,
    "cookTime": 40,
    "servings": 4,
    "ingredients": [
        {
            "name": "Spaghetti",
            "ingredientKey": "spaghetti",
            "amount": "500",
            "unit": "g",
            "notes": ""
        }
    ],
    "steps": [
        { "instruction": "Boil water in a large pot." }
    ]
}
```

**Response:**
```json
{
    "success": true,
    "slug": "updated-recipe-title"
}
```

**Validation:**
- Title, description, cuisine, tag are required and non-empty
- Prep time and cook time must be positive numbers
- At least 2 ingredients required (with ingredientKey, amount, unit)
- At least 1 step required with non-empty instruction

**Side Effects:**
- Sets `updatedAt` to current timestamp
- Deletes existing ingredients/steps and inserts new ones

---

### `DELETE /api/recipes/[id]`

Deletes a recipe and all associated data.

**Authorization:** User must be the recipe author.

**Response:**
```json
{
    "success": true
}
```

**Side Effects:**
- Cascading delete removes ingredients, steps, votes, and comments

---

## Recipe Detail Page Changes

### Timestamps Display

The recipe detail page now shows:
- **Published date**: When the recipe was first published
- **Edited date**: When the recipe was last edited (only shows if edited more than 1 minute after publishing)

```svelte
<div class="timestamps">
    {#if recipe.publishedAt}
        <span class="timestamp">
            <Calendar size={14} />
            Published {formatDate(recipe.publishedAt)}
        </span>
    {/if}
    {#if recipe.updatedAt}
        <span class="timestamp edited">
            (Edited {formatDate(recipe.updatedAt)})
        </span>
    {/if}
</div>
```

### Owner Actions

When viewing your own recipe, you see:
- **Edit Recipe** button - navigates to `/recipes/[slug]/edit`
- **Delete** button - shows confirmation dialog, then deletes

```svelte
{#if data.isOwn}
    <div class="owner-actions">
        <a href="/recipes/{recipe.slug}/edit" class="owner-btn edit">
            <Pencil size={16} />
            Edit Recipe
        </a>
        <button class="owner-btn delete" onclick={handleDelete} disabled={isDeleting}>
            <Trash2 size={16} />
            {isDeleting ? 'Deleting...' : 'Delete'}
        </button>
    </div>
{/if}
```

---

## Edit Page

The edit page (`/recipes/[slug]/edit`) pre-populates the form with existing recipe data.

### Server-Side Loading

```typescript
// Checks:
// 1. User is logged in
// 2. Recipe exists
// 3. User is the author

return {
    recipe: {
        id, title, slug, description, photoUrl,
        prepTimeMinutes, cookTimeMinutes, difficulty,
        cuisine, tag, servings,
        ingredients: [...],
        steps: [...]
    }
};
```

### Form Pre-population

- All text fields filled with existing values
- Dropdowns set to current selections
- Ingredients and steps arrays populated with existing data
- Photo displayed (editing not yet supported)

### Submission

- Uses PUT to `/api/recipes/[id]` instead of POST
- Redirects to recipe detail page on success
- Shows error message on failure

---

## Security

1. **Authentication required** - All operations require login
2. **Authorization** - Only recipe authors can edit/delete their recipes
3. **Server-side validation** - All required fields validated on server
4. **Cascading deletes** - Database handles cleanup of related data

---

## Data Flow

### Editing a Recipe

1. User clicks "Edit Recipe" on their recipe
2. Edit page loads with pre-populated form
3. User makes changes
4. Form validates client-side (requirements checklist)
5. On submit, PUT request sent to `/api/recipes/[id]`
6. Server validates ownership and data
7. Recipe updated, ingredients/steps replaced
8. User redirected to updated recipe

### Deleting a Recipe

1. User clicks "Delete" on their recipe
2. Confirmation dialog appears
3. On confirm, DELETE request sent to `/api/recipes/[id]`
4. Server validates ownership
5. Recipe and all related data deleted
6. User redirected to recipes list

---

## Timestamps Logic

The "Edited" timestamp only shows when:
```typescript
const wasEdited = recipe.publishedAt && recipe.updatedAt &&
    (recipe.updatedAt.getTime() - recipe.publishedAt.getTime() > 60000);
```

This prevents showing "Edited" for recipes that were just published or had minor immediate corrections.

---

## Future Improvements

1. **Photo editing** - Allow changing/removing recipe photo
2. **Draft saving** - Auto-save edits before publishing
3. **Revision history** - Track all changes to a recipe
4. **Unpublish option** - Hide recipe without deleting
5. **Bulk operations** - Delete/archive multiple recipes
