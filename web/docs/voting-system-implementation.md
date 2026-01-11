# Voting System Implementation

This document describes the implementation of the recipe voting system for JustCook.

## Overview

The voting system allows authenticated users to upvote or downvote recipes. Users can toggle their vote (clicking the same vote removes it) or switch between upvote and downvote.

## Files Modified/Created

### Created
- `src/routes/api/votes/+server.ts` - Vote API endpoint

### Modified
- `src/lib/components/recipe/VoteButtons.svelte` - Made interactive with API integration
- `src/routes/recipes/[slug]/+page.server.ts` - Added user vote fetching
- `src/routes/recipes/[slug]/+page.svelte` - Updated to pass new props

---

## API Endpoint

### `POST /api/votes`

Creates, updates, or removes a vote.

**Request Body:**
```json
{
  "recipeId": "uuid-string",
  "value": 1 | -1
}
```

**Behavior:**
- If no existing vote: Creates new vote
- If existing vote with same value: Removes vote (toggle off)
- If existing vote with different value: Updates to new value

**Response:**
```json
{
  "success": true,
  "userVote": 1 | -1 | null,
  "upvotes": 10,
  "downvotes": 2
}
```

**Error Responses:**
- `401 Unauthorized` - User not logged in
- `400 Bad Request` - Invalid recipeId or value
- `403 Forbidden` - User trying to vote on own recipe
- `404 Not Found` - Recipe doesn't exist
- `500 Internal Server Error` - Database error

### `DELETE /api/votes`

Removes a user's vote from a recipe.

**Request Body:**
```json
{
  "recipeId": "uuid-string"
}
```

**Response:**
```json
{
  "success": true,
  "userVote": null,
  "upvotes": 10,
  "downvotes": 2
}
```

---

## Database Schema

The voting system uses the existing `votes` table:

```sql
CREATE TABLE votes (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  recipe_id UUID NOT NULL REFERENCES recipes(id) ON DELETE CASCADE,
  user_id TEXT NOT NULL REFERENCES user(id) ON DELETE CASCADE,
  value INTEGER NOT NULL,  -- 1 for upvote, -1 for downvote
  created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW() NOT NULL,
  UNIQUE(user_id, recipe_id)
);
```

**Constraints:**
- Unique constraint on `(user_id, recipe_id)` ensures one vote per user per recipe
- Cascade delete removes votes when recipe or user is deleted

---

## Component: VoteButtons

### Props

| Prop | Type | Required | Description |
|------|------|----------|-------------|
| `recipeId` | `string` | Yes | UUID of the recipe |
| `upvotes` | `number` | Yes | Initial upvote count |
| `downvotes` | `number` | Yes | Initial downvote count |
| `userVote` | `1 \| -1 \| null` | No | User's current vote |

### State Management

The component maintains local state that updates optimistically from API responses:

```typescript
let currentUpvotes = $state(upvotes);
let currentDownvotes = $state(downvotes);
let currentUserVote = $state(userVote);
let isLoading = $state(false);
```

### Behavior

1. **Upvote button clicked:**
   - If not voted: Add upvote
   - If already upvoted: Remove vote
   - If downvoted: Switch to upvote

2. **Downvote button clicked:**
   - If not voted: Add downvote
   - If already downvoted: Remove vote
   - If upvoted: Switch to downvote

3. **Unauthenticated users:**
   - Redirected to `/auth/login` on vote attempt

### Visual Feedback

- **Upvote display**: Shows total upvotes (not net score)
- **Ratio display**: Shows upvote percentage when total votes > 0
- Active upvote: Green color (`#22c55e`)
- Active downvote: Red color (`#ef4444`)
- Loading state: Buttons disabled with reduced opacity

---

## Server-Side Data Loading

The recipe page server (`+page.server.ts`) now fetches:

1. **Vote counts** (existing):
```typescript
const voteResult = await db
  .select({
    upvotes: sql`COALESCE(SUM(CASE WHEN value > 0 THEN 1 ELSE 0 END), 0)::int`,
    downvotes: sql`COALESCE(SUM(CASE WHEN value < 0 THEN 1 ELSE 0 END), 0)::int`
  })
  .from(votes)
  .where(eq(votes.recipeId, recipe.id));
```

2. **User's vote** (new):
```typescript
let userVote: 1 | -1 | null = null;
if (currentUser) {
  const userVoteResult = await db
    .select({ value: votes.value })
    .from(votes)
    .where(and(
      eq(votes.userId, currentUser.id),
      eq(votes.recipeId, recipe.id)
    ))
    .limit(1);

  if (userVoteResult.length > 0) {
    userVote = userVoteResult[0].value as 1 | -1;
  }
}
```

---

## Security Considerations

1. **Authentication required** - All vote operations require authentication
2. **Self-voting prevented** - Users cannot vote on their own recipes
3. **One vote per user** - Database constraint prevents duplicate votes
4. **Input validation** - Vote value must be exactly `1` or `-1`

---

## Testing

### Manual Testing Steps

1. **Unauthenticated voting:**
   - Navigate to a recipe page while logged out
   - Click upvote/downvote button
   - Expected: Redirected to login page

2. **Upvote a recipe:**
   - Log in and navigate to a recipe you didn't create
   - Click upvote button
   - Expected: Upvote count increases, button turns green

3. **Toggle vote off:**
   - Click the same vote button again
   - Expected: Vote removed, count decreases, button returns to default

4. **Switch vote:**
   - With an upvote active, click downvote
   - Expected: Upvote count decreases, downvote count increases

5. **Vote on own recipe:**
   - Navigate to your own recipe
   - Click vote button
   - Expected: Error (403) - cannot vote on own recipe

---

## Future Improvements

1. **Optimistic updates** - Update UI immediately, revert on API failure
2. **Vote score syncing** - Keep `recipes.voteScore` field updated via triggers or scheduled jobs
3. **Rate limiting** - Prevent vote spam
4. **Vote analytics** - Track voting patterns for trending algorithms
