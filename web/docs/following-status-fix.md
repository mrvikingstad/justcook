# Following Status Fix

This document describes the fix for the following status bug on chef profile pages.

## Problem

The chef profile page (`/chef/[username]`) always showed the "Follow" button regardless of whether the current user was actually following that chef. The `isFollowing` value was hardcoded to `false`.

**Location:** `src/routes/chef/[username]/+page.server.ts:113`

```typescript
// Before (broken)
isFollowing: false, // TODO: Check if current user is following
```

## Solution

Added a database query to check if the current user is following the chef being viewed.

### Code Changes

**File:** `src/routes/chef/[username]/+page.server.ts`

```typescript
// Check if current user is following this chef
let isFollowing = false;
if (locals.user && locals.user.id !== foundUser.id) {
    const followResult = await db
        .select({ id: follows.id })
        .from(follows)
        .where(and(
            eq(follows.followerId, locals.user.id),
            eq(follows.followingId, foundUser.id)
        ))
        .limit(1);
    isFollowing = followResult.length > 0;
}
```

### Logic

1. Default `isFollowing` to `false`
2. Only check if:
   - User is logged in (`locals.user` exists)
   - User is not viewing their own profile (can't follow yourself)
3. Query the `follows` table for a matching row
4. If a row exists, the user is following

## Database Schema

The `follows` table structure:

```sql
CREATE TABLE follows (
    id UUID PRIMARY KEY,
    follower_id TEXT NOT NULL REFERENCES user(id),
    following_id TEXT NOT NULL REFERENCES user(id),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    UNIQUE(follower_id, following_id)
);
```

- `follower_id`: The user who is following (the current user)
- `following_id`: The user being followed (the chef being viewed)

## UI Behavior

The page component (`+page.svelte`) was already correctly implemented:

- Initializes state from server data: `let isFollowing = $state(data.isFollowing)`
- Shows "Following" button when following, "Follow" when not
- `toggleFollow()` function calls `/api/follow` with POST (follow) or DELETE (unfollow)
- Updates local state and follower count on success

## Testing

1. **Logged out user:** Should see "Follow" button (if not own profile)
2. **Logged in, not following:** Should see "Follow" button
3. **Logged in, following:** Should see "Following" button
4. **Own profile:** Should not see any follow button
5. **Click Follow:** Button changes to "Following", follower count increases
6. **Click Following:** Button changes to "Follow", follower count decreases
