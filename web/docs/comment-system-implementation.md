# Comment System Implementation

This document describes the implementation of the comment system for JustCook recipes.

## Overview

Users can post comments on recipes, view all comments, and delete their own comments. The comment count on recipes is automatically updated.

## Files Created

- `src/routes/recipes/[slug]/comments/+page.server.ts` - Server-side data loading
- `src/routes/api/comments/+server.ts` - POST/DELETE API endpoints

## Files Modified

- `src/routes/recipes/[slug]/comments/+page.svelte` - Updated to use real data instead of mock data

---

## Database Schema

The comment system uses the existing `comments` table:

```sql
CREATE TABLE comments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    recipe_id UUID NOT NULL REFERENCES recipes(id) ON DELETE CASCADE,
    user_id TEXT NOT NULL REFERENCES user(id) ON DELETE CASCADE,
    content TEXT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW() NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW() NOT NULL
);
```

---

## API Endpoints

### `POST /api/comments`

Creates a new comment on a recipe.

**Request Body:**
```json
{
    "recipeId": "uuid-string",
    "content": "Comment text here"
}
```

**Response:**
```json
{
    "success": true,
    "comment": {
        "id": "uuid",
        "content": "Comment text",
        "createdAt": "2024-01-01T00:00:00Z",
        "authorId": "user-id",
        "authorName": "John Doe",
        "authorAvatar": "url-or-null",
        "isRecipeAuthor": false,
        "isOwn": true
    }
}
```

**Validation:**
- Content is required and cannot be empty
- Max length: 2000 characters
- Recipe must exist

**Side Effects:**
- Increments `commentCount` on the recipe

---

### `DELETE /api/comments`

Deletes a comment (owner only).

**Request Body:**
```json
{
    "commentId": "uuid-string"
}
```

**Response:**
```json
{
    "success": true
}
```

**Authorization:**
- User can only delete their own comments

**Side Effects:**
- Decrements `commentCount` on the recipe (with floor of 0)

---

## Server-Side Loading

The comments page loads:

1. **Recipe info** - Title, slug, author ID for breadcrumb and author badge
2. **Comments** - All comments for the recipe, ordered by newest first
3. **User info** - For each comment, joined with user/chefProfiles tables

```typescript
// Data structure returned
{
    recipe: {
        id: string,
        title: string,
        slug: string,
        authorId: string,
        authorName: string
    },
    comments: Array<{
        id: string,
        content: string,
        createdAt: Date,
        authorId: string,
        authorName: string,
        authorAvatar: string | null,
        isRecipeAuthor: boolean,
        isOwn: boolean
    }>,
    isLoggedIn: boolean,
    currentUserId: string | null
}
```

---

## Page Component Features

### Comment Display
- Shows author name with avatar (or fallback initial)
- "Author" badge for recipe author's comments
- Relative timestamps ("2h ago", "yesterday", etc.)
- Delete button for user's own comments

### Comment Form
- Only visible when logged in
- Shows login prompt for unauthenticated users
- Disabled submit until content is entered
- Error display on failed submission

### State Management
- Comments stored in reactive `$state`
- New comments added to top of list immediately
- Deleted comments removed from list immediately
- No page reload required

---

## Security

1. **Authentication required** - POST and DELETE require login
2. **Authorization** - Users can only delete their own comments
3. **Input validation** - Content length limit (2000 chars)
4. **Recipe validation** - Comment must be on existing recipe

---

## Testing

### Post a Comment
1. Navigate to any recipe, click "X comments" link
2. If not logged in, see "Sign in to leave a comment"
3. If logged in, enter comment text and click "Post"
4. Comment appears at top of list immediately

### Delete a Comment
1. Post a comment (or view one you've posted)
2. Click "Delete" button on your comment
3. Confirm in dialog
4. Comment is removed from list

### Recipe Author Badge
1. Recipe author comments on their own recipe
2. "Author" badge appears next to their name

---

## Future Improvements

1. **Comment editing** - Allow users to edit their comments
2. **Reply threads** - Nested replies to comments (UI exists but not wired)
3. **Comment voting** - Upvote/downvote comments (UI exists but not wired)
4. **Pagination** - Load more comments for recipes with many comments
5. **Real-time updates** - WebSocket for live comment updates
