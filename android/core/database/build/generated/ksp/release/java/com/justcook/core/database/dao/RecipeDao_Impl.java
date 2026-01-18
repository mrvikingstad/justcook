package com.justcook.core.database.dao;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomDatabaseKt;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.justcook.core.database.converter.Converters;
import com.justcook.core.database.entity.IngredientEntity;
import com.justcook.core.database.entity.RecipeEntity;
import com.justcook.core.database.entity.StepEntity;
import java.lang.Class;
import java.lang.Double;
import java.lang.Exception;
import java.lang.IllegalStateException;
import java.lang.Integer;
import java.lang.Long;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlinx.coroutines.flow.Flow;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class RecipeDao_Impl implements RecipeDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<RecipeEntity> __insertionAdapterOfRecipeEntity;

  private final Converters __converters = new Converters();

  private final EntityInsertionAdapter<IngredientEntity> __insertionAdapterOfIngredientEntity;

  private final EntityInsertionAdapter<StepEntity> __insertionAdapterOfStepEntity;

  private final SharedSQLiteStatement __preparedStmtOfDeleteRecipeBySlug;

  private final SharedSQLiteStatement __preparedStmtOfClearTrendingFlag;

  private final SharedSQLiteStatement __preparedStmtOfClearDiscoverFlag;

  private final SharedSQLiteStatement __preparedStmtOfDeleteIngredientsByRecipeId;

  private final SharedSQLiteStatement __preparedStmtOfDeleteStepsByRecipeId;

  public RecipeDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfRecipeEntity = new EntityInsertionAdapter<RecipeEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `recipes` (`id`,`title`,`slug`,`description`,`photoUrl`,`authorId`,`authorName`,`authorUsername`,`authorProfileTier`,`cuisine`,`tag`,`difficulty`,`prepTimeMinutes`,`cookTimeMinutes`,`servings`,`upvotes`,`downvotes`,`commentCount`,`publishedAt`,`cachedAt`,`isTrending`,`isDiscover`) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final RecipeEntity entity) {
        statement.bindString(1, entity.getId());
        statement.bindString(2, entity.getTitle());
        statement.bindString(3, entity.getSlug());
        if (entity.getDescription() == null) {
          statement.bindNull(4);
        } else {
          statement.bindString(4, entity.getDescription());
        }
        if (entity.getPhotoUrl() == null) {
          statement.bindNull(5);
        } else {
          statement.bindString(5, entity.getPhotoUrl());
        }
        statement.bindString(6, entity.getAuthorId());
        statement.bindString(7, entity.getAuthorName());
        statement.bindString(8, entity.getAuthorUsername());
        statement.bindString(9, entity.getAuthorProfileTier());
        if (entity.getCuisine() == null) {
          statement.bindNull(10);
        } else {
          statement.bindString(10, entity.getCuisine());
        }
        if (entity.getTag() == null) {
          statement.bindNull(11);
        } else {
          statement.bindString(11, entity.getTag());
        }
        if (entity.getDifficulty() == null) {
          statement.bindNull(12);
        } else {
          statement.bindString(12, entity.getDifficulty());
        }
        if (entity.getPrepTimeMinutes() == null) {
          statement.bindNull(13);
        } else {
          statement.bindLong(13, entity.getPrepTimeMinutes());
        }
        if (entity.getCookTimeMinutes() == null) {
          statement.bindNull(14);
        } else {
          statement.bindLong(14, entity.getCookTimeMinutes());
        }
        statement.bindLong(15, entity.getServings());
        statement.bindLong(16, entity.getUpvotes());
        statement.bindLong(17, entity.getDownvotes());
        statement.bindLong(18, entity.getCommentCount());
        final Long _tmp = __converters.fromInstant(entity.getPublishedAt());
        if (_tmp == null) {
          statement.bindNull(19);
        } else {
          statement.bindLong(19, _tmp);
        }
        final Long _tmp_1 = __converters.fromInstant(entity.getCachedAt());
        if (_tmp_1 == null) {
          statement.bindNull(20);
        } else {
          statement.bindLong(20, _tmp_1);
        }
        final int _tmp_2 = entity.isTrending() ? 1 : 0;
        statement.bindLong(21, _tmp_2);
        final int _tmp_3 = entity.isDiscover() ? 1 : 0;
        statement.bindLong(22, _tmp_3);
      }
    };
    this.__insertionAdapterOfIngredientEntity = new EntityInsertionAdapter<IngredientEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `ingredients` (`id`,`recipeId`,`name`,`ingredientKey`,`amount`,`unit`,`notes`,`sortOrder`) VALUES (nullif(?, 0),?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final IngredientEntity entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getRecipeId());
        statement.bindString(3, entity.getName());
        if (entity.getIngredientKey() == null) {
          statement.bindNull(4);
        } else {
          statement.bindString(4, entity.getIngredientKey());
        }
        if (entity.getAmount() == null) {
          statement.bindNull(5);
        } else {
          statement.bindDouble(5, entity.getAmount());
        }
        if (entity.getUnit() == null) {
          statement.bindNull(6);
        } else {
          statement.bindString(6, entity.getUnit());
        }
        if (entity.getNotes() == null) {
          statement.bindNull(7);
        } else {
          statement.bindString(7, entity.getNotes());
        }
        statement.bindLong(8, entity.getSortOrder());
      }
    };
    this.__insertionAdapterOfStepEntity = new EntityInsertionAdapter<StepEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `steps` (`id`,`recipeId`,`stepNumber`,`instruction`) VALUES (nullif(?, 0),?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final StepEntity entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getRecipeId());
        statement.bindLong(3, entity.getStepNumber());
        statement.bindString(4, entity.getInstruction());
      }
    };
    this.__preparedStmtOfDeleteRecipeBySlug = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM recipes WHERE slug = ?";
        return _query;
      }
    };
    this.__preparedStmtOfClearTrendingFlag = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE recipes SET isTrending = 0";
        return _query;
      }
    };
    this.__preparedStmtOfClearDiscoverFlag = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE recipes SET isDiscover = 0";
        return _query;
      }
    };
    this.__preparedStmtOfDeleteIngredientsByRecipeId = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM ingredients WHERE recipeId = ?";
        return _query;
      }
    };
    this.__preparedStmtOfDeleteStepsByRecipeId = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM steps WHERE recipeId = ?";
        return _query;
      }
    };
  }

  @Override
  public Object insertRecipe(final RecipeEntity recipe,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfRecipeEntity.insert(recipe);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object insertRecipes(final List<RecipeEntity> recipes,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfRecipeEntity.insert(recipes);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object insertIngredients(final List<IngredientEntity> ingredients,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfIngredientEntity.insert(ingredients);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object insertSteps(final List<StepEntity> steps,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfStepEntity.insert(steps);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object insertRecipeWithDetails(final RecipeEntity recipe,
      final List<IngredientEntity> ingredients, final List<StepEntity> steps,
      final Continuation<? super Unit> $completion) {
    return RoomDatabaseKt.withTransaction(__db, (__cont) -> RecipeDao.DefaultImpls.insertRecipeWithDetails(RecipeDao_Impl.this, recipe, ingredients, steps, __cont), $completion);
  }

  @Override
  public Object deleteRecipeBySlug(final String slug,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteRecipeBySlug.acquire();
        int _argIndex = 1;
        _stmt.bindString(_argIndex, slug);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfDeleteRecipeBySlug.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object clearTrendingFlag(final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfClearTrendingFlag.acquire();
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfClearTrendingFlag.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object clearDiscoverFlag(final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfClearDiscoverFlag.acquire();
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfClearDiscoverFlag.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteIngredientsByRecipeId(final String recipeId,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteIngredientsByRecipeId.acquire();
        int _argIndex = 1;
        _stmt.bindString(_argIndex, recipeId);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfDeleteIngredientsByRecipeId.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteStepsByRecipeId(final String recipeId,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteStepsByRecipeId.acquire();
        int _argIndex = 1;
        _stmt.bindString(_argIndex, recipeId);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfDeleteStepsByRecipeId.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object getRecipeBySlug(final String slug,
      final Continuation<? super RecipeEntity> $completion) {
    final String _sql = "SELECT * FROM recipes WHERE slug = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, slug);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<RecipeEntity>() {
      @Override
      @Nullable
      public RecipeEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "title");
          final int _cursorIndexOfSlug = CursorUtil.getColumnIndexOrThrow(_cursor, "slug");
          final int _cursorIndexOfDescription = CursorUtil.getColumnIndexOrThrow(_cursor, "description");
          final int _cursorIndexOfPhotoUrl = CursorUtil.getColumnIndexOrThrow(_cursor, "photoUrl");
          final int _cursorIndexOfAuthorId = CursorUtil.getColumnIndexOrThrow(_cursor, "authorId");
          final int _cursorIndexOfAuthorName = CursorUtil.getColumnIndexOrThrow(_cursor, "authorName");
          final int _cursorIndexOfAuthorUsername = CursorUtil.getColumnIndexOrThrow(_cursor, "authorUsername");
          final int _cursorIndexOfAuthorProfileTier = CursorUtil.getColumnIndexOrThrow(_cursor, "authorProfileTier");
          final int _cursorIndexOfCuisine = CursorUtil.getColumnIndexOrThrow(_cursor, "cuisine");
          final int _cursorIndexOfTag = CursorUtil.getColumnIndexOrThrow(_cursor, "tag");
          final int _cursorIndexOfDifficulty = CursorUtil.getColumnIndexOrThrow(_cursor, "difficulty");
          final int _cursorIndexOfPrepTimeMinutes = CursorUtil.getColumnIndexOrThrow(_cursor, "prepTimeMinutes");
          final int _cursorIndexOfCookTimeMinutes = CursorUtil.getColumnIndexOrThrow(_cursor, "cookTimeMinutes");
          final int _cursorIndexOfServings = CursorUtil.getColumnIndexOrThrow(_cursor, "servings");
          final int _cursorIndexOfUpvotes = CursorUtil.getColumnIndexOrThrow(_cursor, "upvotes");
          final int _cursorIndexOfDownvotes = CursorUtil.getColumnIndexOrThrow(_cursor, "downvotes");
          final int _cursorIndexOfCommentCount = CursorUtil.getColumnIndexOrThrow(_cursor, "commentCount");
          final int _cursorIndexOfPublishedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "publishedAt");
          final int _cursorIndexOfCachedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "cachedAt");
          final int _cursorIndexOfIsTrending = CursorUtil.getColumnIndexOrThrow(_cursor, "isTrending");
          final int _cursorIndexOfIsDiscover = CursorUtil.getColumnIndexOrThrow(_cursor, "isDiscover");
          final RecipeEntity _result;
          if (_cursor.moveToFirst()) {
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpTitle;
            _tmpTitle = _cursor.getString(_cursorIndexOfTitle);
            final String _tmpSlug;
            _tmpSlug = _cursor.getString(_cursorIndexOfSlug);
            final String _tmpDescription;
            if (_cursor.isNull(_cursorIndexOfDescription)) {
              _tmpDescription = null;
            } else {
              _tmpDescription = _cursor.getString(_cursorIndexOfDescription);
            }
            final String _tmpPhotoUrl;
            if (_cursor.isNull(_cursorIndexOfPhotoUrl)) {
              _tmpPhotoUrl = null;
            } else {
              _tmpPhotoUrl = _cursor.getString(_cursorIndexOfPhotoUrl);
            }
            final String _tmpAuthorId;
            _tmpAuthorId = _cursor.getString(_cursorIndexOfAuthorId);
            final String _tmpAuthorName;
            _tmpAuthorName = _cursor.getString(_cursorIndexOfAuthorName);
            final String _tmpAuthorUsername;
            _tmpAuthorUsername = _cursor.getString(_cursorIndexOfAuthorUsername);
            final String _tmpAuthorProfileTier;
            _tmpAuthorProfileTier = _cursor.getString(_cursorIndexOfAuthorProfileTier);
            final String _tmpCuisine;
            if (_cursor.isNull(_cursorIndexOfCuisine)) {
              _tmpCuisine = null;
            } else {
              _tmpCuisine = _cursor.getString(_cursorIndexOfCuisine);
            }
            final String _tmpTag;
            if (_cursor.isNull(_cursorIndexOfTag)) {
              _tmpTag = null;
            } else {
              _tmpTag = _cursor.getString(_cursorIndexOfTag);
            }
            final String _tmpDifficulty;
            if (_cursor.isNull(_cursorIndexOfDifficulty)) {
              _tmpDifficulty = null;
            } else {
              _tmpDifficulty = _cursor.getString(_cursorIndexOfDifficulty);
            }
            final Integer _tmpPrepTimeMinutes;
            if (_cursor.isNull(_cursorIndexOfPrepTimeMinutes)) {
              _tmpPrepTimeMinutes = null;
            } else {
              _tmpPrepTimeMinutes = _cursor.getInt(_cursorIndexOfPrepTimeMinutes);
            }
            final Integer _tmpCookTimeMinutes;
            if (_cursor.isNull(_cursorIndexOfCookTimeMinutes)) {
              _tmpCookTimeMinutes = null;
            } else {
              _tmpCookTimeMinutes = _cursor.getInt(_cursorIndexOfCookTimeMinutes);
            }
            final int _tmpServings;
            _tmpServings = _cursor.getInt(_cursorIndexOfServings);
            final int _tmpUpvotes;
            _tmpUpvotes = _cursor.getInt(_cursorIndexOfUpvotes);
            final int _tmpDownvotes;
            _tmpDownvotes = _cursor.getInt(_cursorIndexOfDownvotes);
            final int _tmpCommentCount;
            _tmpCommentCount = _cursor.getInt(_cursorIndexOfCommentCount);
            final Instant _tmpPublishedAt;
            final Long _tmp;
            if (_cursor.isNull(_cursorIndexOfPublishedAt)) {
              _tmp = null;
            } else {
              _tmp = _cursor.getLong(_cursorIndexOfPublishedAt);
            }
            _tmpPublishedAt = __converters.toInstant(_tmp);
            final Instant _tmpCachedAt;
            final Long _tmp_1;
            if (_cursor.isNull(_cursorIndexOfCachedAt)) {
              _tmp_1 = null;
            } else {
              _tmp_1 = _cursor.getLong(_cursorIndexOfCachedAt);
            }
            final Instant _tmp_2 = __converters.toInstant(_tmp_1);
            if (_tmp_2 == null) {
              throw new IllegalStateException("Expected NON-NULL 'java.time.Instant', but it was NULL.");
            } else {
              _tmpCachedAt = _tmp_2;
            }
            final boolean _tmpIsTrending;
            final int _tmp_3;
            _tmp_3 = _cursor.getInt(_cursorIndexOfIsTrending);
            _tmpIsTrending = _tmp_3 != 0;
            final boolean _tmpIsDiscover;
            final int _tmp_4;
            _tmp_4 = _cursor.getInt(_cursorIndexOfIsDiscover);
            _tmpIsDiscover = _tmp_4 != 0;
            _result = new RecipeEntity(_tmpId,_tmpTitle,_tmpSlug,_tmpDescription,_tmpPhotoUrl,_tmpAuthorId,_tmpAuthorName,_tmpAuthorUsername,_tmpAuthorProfileTier,_tmpCuisine,_tmpTag,_tmpDifficulty,_tmpPrepTimeMinutes,_tmpCookTimeMinutes,_tmpServings,_tmpUpvotes,_tmpDownvotes,_tmpCommentCount,_tmpPublishedAt,_tmpCachedAt,_tmpIsTrending,_tmpIsDiscover);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Flow<RecipeEntity> observeRecipeBySlug(final String slug) {
    final String _sql = "SELECT * FROM recipes WHERE slug = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, slug);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"recipes"}, new Callable<RecipeEntity>() {
      @Override
      @Nullable
      public RecipeEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "title");
          final int _cursorIndexOfSlug = CursorUtil.getColumnIndexOrThrow(_cursor, "slug");
          final int _cursorIndexOfDescription = CursorUtil.getColumnIndexOrThrow(_cursor, "description");
          final int _cursorIndexOfPhotoUrl = CursorUtil.getColumnIndexOrThrow(_cursor, "photoUrl");
          final int _cursorIndexOfAuthorId = CursorUtil.getColumnIndexOrThrow(_cursor, "authorId");
          final int _cursorIndexOfAuthorName = CursorUtil.getColumnIndexOrThrow(_cursor, "authorName");
          final int _cursorIndexOfAuthorUsername = CursorUtil.getColumnIndexOrThrow(_cursor, "authorUsername");
          final int _cursorIndexOfAuthorProfileTier = CursorUtil.getColumnIndexOrThrow(_cursor, "authorProfileTier");
          final int _cursorIndexOfCuisine = CursorUtil.getColumnIndexOrThrow(_cursor, "cuisine");
          final int _cursorIndexOfTag = CursorUtil.getColumnIndexOrThrow(_cursor, "tag");
          final int _cursorIndexOfDifficulty = CursorUtil.getColumnIndexOrThrow(_cursor, "difficulty");
          final int _cursorIndexOfPrepTimeMinutes = CursorUtil.getColumnIndexOrThrow(_cursor, "prepTimeMinutes");
          final int _cursorIndexOfCookTimeMinutes = CursorUtil.getColumnIndexOrThrow(_cursor, "cookTimeMinutes");
          final int _cursorIndexOfServings = CursorUtil.getColumnIndexOrThrow(_cursor, "servings");
          final int _cursorIndexOfUpvotes = CursorUtil.getColumnIndexOrThrow(_cursor, "upvotes");
          final int _cursorIndexOfDownvotes = CursorUtil.getColumnIndexOrThrow(_cursor, "downvotes");
          final int _cursorIndexOfCommentCount = CursorUtil.getColumnIndexOrThrow(_cursor, "commentCount");
          final int _cursorIndexOfPublishedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "publishedAt");
          final int _cursorIndexOfCachedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "cachedAt");
          final int _cursorIndexOfIsTrending = CursorUtil.getColumnIndexOrThrow(_cursor, "isTrending");
          final int _cursorIndexOfIsDiscover = CursorUtil.getColumnIndexOrThrow(_cursor, "isDiscover");
          final RecipeEntity _result;
          if (_cursor.moveToFirst()) {
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpTitle;
            _tmpTitle = _cursor.getString(_cursorIndexOfTitle);
            final String _tmpSlug;
            _tmpSlug = _cursor.getString(_cursorIndexOfSlug);
            final String _tmpDescription;
            if (_cursor.isNull(_cursorIndexOfDescription)) {
              _tmpDescription = null;
            } else {
              _tmpDescription = _cursor.getString(_cursorIndexOfDescription);
            }
            final String _tmpPhotoUrl;
            if (_cursor.isNull(_cursorIndexOfPhotoUrl)) {
              _tmpPhotoUrl = null;
            } else {
              _tmpPhotoUrl = _cursor.getString(_cursorIndexOfPhotoUrl);
            }
            final String _tmpAuthorId;
            _tmpAuthorId = _cursor.getString(_cursorIndexOfAuthorId);
            final String _tmpAuthorName;
            _tmpAuthorName = _cursor.getString(_cursorIndexOfAuthorName);
            final String _tmpAuthorUsername;
            _tmpAuthorUsername = _cursor.getString(_cursorIndexOfAuthorUsername);
            final String _tmpAuthorProfileTier;
            _tmpAuthorProfileTier = _cursor.getString(_cursorIndexOfAuthorProfileTier);
            final String _tmpCuisine;
            if (_cursor.isNull(_cursorIndexOfCuisine)) {
              _tmpCuisine = null;
            } else {
              _tmpCuisine = _cursor.getString(_cursorIndexOfCuisine);
            }
            final String _tmpTag;
            if (_cursor.isNull(_cursorIndexOfTag)) {
              _tmpTag = null;
            } else {
              _tmpTag = _cursor.getString(_cursorIndexOfTag);
            }
            final String _tmpDifficulty;
            if (_cursor.isNull(_cursorIndexOfDifficulty)) {
              _tmpDifficulty = null;
            } else {
              _tmpDifficulty = _cursor.getString(_cursorIndexOfDifficulty);
            }
            final Integer _tmpPrepTimeMinutes;
            if (_cursor.isNull(_cursorIndexOfPrepTimeMinutes)) {
              _tmpPrepTimeMinutes = null;
            } else {
              _tmpPrepTimeMinutes = _cursor.getInt(_cursorIndexOfPrepTimeMinutes);
            }
            final Integer _tmpCookTimeMinutes;
            if (_cursor.isNull(_cursorIndexOfCookTimeMinutes)) {
              _tmpCookTimeMinutes = null;
            } else {
              _tmpCookTimeMinutes = _cursor.getInt(_cursorIndexOfCookTimeMinutes);
            }
            final int _tmpServings;
            _tmpServings = _cursor.getInt(_cursorIndexOfServings);
            final int _tmpUpvotes;
            _tmpUpvotes = _cursor.getInt(_cursorIndexOfUpvotes);
            final int _tmpDownvotes;
            _tmpDownvotes = _cursor.getInt(_cursorIndexOfDownvotes);
            final int _tmpCommentCount;
            _tmpCommentCount = _cursor.getInt(_cursorIndexOfCommentCount);
            final Instant _tmpPublishedAt;
            final Long _tmp;
            if (_cursor.isNull(_cursorIndexOfPublishedAt)) {
              _tmp = null;
            } else {
              _tmp = _cursor.getLong(_cursorIndexOfPublishedAt);
            }
            _tmpPublishedAt = __converters.toInstant(_tmp);
            final Instant _tmpCachedAt;
            final Long _tmp_1;
            if (_cursor.isNull(_cursorIndexOfCachedAt)) {
              _tmp_1 = null;
            } else {
              _tmp_1 = _cursor.getLong(_cursorIndexOfCachedAt);
            }
            final Instant _tmp_2 = __converters.toInstant(_tmp_1);
            if (_tmp_2 == null) {
              throw new IllegalStateException("Expected NON-NULL 'java.time.Instant', but it was NULL.");
            } else {
              _tmpCachedAt = _tmp_2;
            }
            final boolean _tmpIsTrending;
            final int _tmp_3;
            _tmp_3 = _cursor.getInt(_cursorIndexOfIsTrending);
            _tmpIsTrending = _tmp_3 != 0;
            final boolean _tmpIsDiscover;
            final int _tmp_4;
            _tmp_4 = _cursor.getInt(_cursorIndexOfIsDiscover);
            _tmpIsDiscover = _tmp_4 != 0;
            _result = new RecipeEntity(_tmpId,_tmpTitle,_tmpSlug,_tmpDescription,_tmpPhotoUrl,_tmpAuthorId,_tmpAuthorName,_tmpAuthorUsername,_tmpAuthorProfileTier,_tmpCuisine,_tmpTag,_tmpDifficulty,_tmpPrepTimeMinutes,_tmpCookTimeMinutes,_tmpServings,_tmpUpvotes,_tmpDownvotes,_tmpCommentCount,_tmpPublishedAt,_tmpCachedAt,_tmpIsTrending,_tmpIsDiscover);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Object getTrendingRecipes(final int limit,
      final Continuation<? super List<RecipeEntity>> $completion) {
    final String _sql = "SELECT * FROM recipes WHERE isTrending = 1 ORDER BY upvotes DESC LIMIT ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, limit);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<RecipeEntity>>() {
      @Override
      @NonNull
      public List<RecipeEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "title");
          final int _cursorIndexOfSlug = CursorUtil.getColumnIndexOrThrow(_cursor, "slug");
          final int _cursorIndexOfDescription = CursorUtil.getColumnIndexOrThrow(_cursor, "description");
          final int _cursorIndexOfPhotoUrl = CursorUtil.getColumnIndexOrThrow(_cursor, "photoUrl");
          final int _cursorIndexOfAuthorId = CursorUtil.getColumnIndexOrThrow(_cursor, "authorId");
          final int _cursorIndexOfAuthorName = CursorUtil.getColumnIndexOrThrow(_cursor, "authorName");
          final int _cursorIndexOfAuthorUsername = CursorUtil.getColumnIndexOrThrow(_cursor, "authorUsername");
          final int _cursorIndexOfAuthorProfileTier = CursorUtil.getColumnIndexOrThrow(_cursor, "authorProfileTier");
          final int _cursorIndexOfCuisine = CursorUtil.getColumnIndexOrThrow(_cursor, "cuisine");
          final int _cursorIndexOfTag = CursorUtil.getColumnIndexOrThrow(_cursor, "tag");
          final int _cursorIndexOfDifficulty = CursorUtil.getColumnIndexOrThrow(_cursor, "difficulty");
          final int _cursorIndexOfPrepTimeMinutes = CursorUtil.getColumnIndexOrThrow(_cursor, "prepTimeMinutes");
          final int _cursorIndexOfCookTimeMinutes = CursorUtil.getColumnIndexOrThrow(_cursor, "cookTimeMinutes");
          final int _cursorIndexOfServings = CursorUtil.getColumnIndexOrThrow(_cursor, "servings");
          final int _cursorIndexOfUpvotes = CursorUtil.getColumnIndexOrThrow(_cursor, "upvotes");
          final int _cursorIndexOfDownvotes = CursorUtil.getColumnIndexOrThrow(_cursor, "downvotes");
          final int _cursorIndexOfCommentCount = CursorUtil.getColumnIndexOrThrow(_cursor, "commentCount");
          final int _cursorIndexOfPublishedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "publishedAt");
          final int _cursorIndexOfCachedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "cachedAt");
          final int _cursorIndexOfIsTrending = CursorUtil.getColumnIndexOrThrow(_cursor, "isTrending");
          final int _cursorIndexOfIsDiscover = CursorUtil.getColumnIndexOrThrow(_cursor, "isDiscover");
          final List<RecipeEntity> _result = new ArrayList<RecipeEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final RecipeEntity _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpTitle;
            _tmpTitle = _cursor.getString(_cursorIndexOfTitle);
            final String _tmpSlug;
            _tmpSlug = _cursor.getString(_cursorIndexOfSlug);
            final String _tmpDescription;
            if (_cursor.isNull(_cursorIndexOfDescription)) {
              _tmpDescription = null;
            } else {
              _tmpDescription = _cursor.getString(_cursorIndexOfDescription);
            }
            final String _tmpPhotoUrl;
            if (_cursor.isNull(_cursorIndexOfPhotoUrl)) {
              _tmpPhotoUrl = null;
            } else {
              _tmpPhotoUrl = _cursor.getString(_cursorIndexOfPhotoUrl);
            }
            final String _tmpAuthorId;
            _tmpAuthorId = _cursor.getString(_cursorIndexOfAuthorId);
            final String _tmpAuthorName;
            _tmpAuthorName = _cursor.getString(_cursorIndexOfAuthorName);
            final String _tmpAuthorUsername;
            _tmpAuthorUsername = _cursor.getString(_cursorIndexOfAuthorUsername);
            final String _tmpAuthorProfileTier;
            _tmpAuthorProfileTier = _cursor.getString(_cursorIndexOfAuthorProfileTier);
            final String _tmpCuisine;
            if (_cursor.isNull(_cursorIndexOfCuisine)) {
              _tmpCuisine = null;
            } else {
              _tmpCuisine = _cursor.getString(_cursorIndexOfCuisine);
            }
            final String _tmpTag;
            if (_cursor.isNull(_cursorIndexOfTag)) {
              _tmpTag = null;
            } else {
              _tmpTag = _cursor.getString(_cursorIndexOfTag);
            }
            final String _tmpDifficulty;
            if (_cursor.isNull(_cursorIndexOfDifficulty)) {
              _tmpDifficulty = null;
            } else {
              _tmpDifficulty = _cursor.getString(_cursorIndexOfDifficulty);
            }
            final Integer _tmpPrepTimeMinutes;
            if (_cursor.isNull(_cursorIndexOfPrepTimeMinutes)) {
              _tmpPrepTimeMinutes = null;
            } else {
              _tmpPrepTimeMinutes = _cursor.getInt(_cursorIndexOfPrepTimeMinutes);
            }
            final Integer _tmpCookTimeMinutes;
            if (_cursor.isNull(_cursorIndexOfCookTimeMinutes)) {
              _tmpCookTimeMinutes = null;
            } else {
              _tmpCookTimeMinutes = _cursor.getInt(_cursorIndexOfCookTimeMinutes);
            }
            final int _tmpServings;
            _tmpServings = _cursor.getInt(_cursorIndexOfServings);
            final int _tmpUpvotes;
            _tmpUpvotes = _cursor.getInt(_cursorIndexOfUpvotes);
            final int _tmpDownvotes;
            _tmpDownvotes = _cursor.getInt(_cursorIndexOfDownvotes);
            final int _tmpCommentCount;
            _tmpCommentCount = _cursor.getInt(_cursorIndexOfCommentCount);
            final Instant _tmpPublishedAt;
            final Long _tmp;
            if (_cursor.isNull(_cursorIndexOfPublishedAt)) {
              _tmp = null;
            } else {
              _tmp = _cursor.getLong(_cursorIndexOfPublishedAt);
            }
            _tmpPublishedAt = __converters.toInstant(_tmp);
            final Instant _tmpCachedAt;
            final Long _tmp_1;
            if (_cursor.isNull(_cursorIndexOfCachedAt)) {
              _tmp_1 = null;
            } else {
              _tmp_1 = _cursor.getLong(_cursorIndexOfCachedAt);
            }
            final Instant _tmp_2 = __converters.toInstant(_tmp_1);
            if (_tmp_2 == null) {
              throw new IllegalStateException("Expected NON-NULL 'java.time.Instant', but it was NULL.");
            } else {
              _tmpCachedAt = _tmp_2;
            }
            final boolean _tmpIsTrending;
            final int _tmp_3;
            _tmp_3 = _cursor.getInt(_cursorIndexOfIsTrending);
            _tmpIsTrending = _tmp_3 != 0;
            final boolean _tmpIsDiscover;
            final int _tmp_4;
            _tmp_4 = _cursor.getInt(_cursorIndexOfIsDiscover);
            _tmpIsDiscover = _tmp_4 != 0;
            _item = new RecipeEntity(_tmpId,_tmpTitle,_tmpSlug,_tmpDescription,_tmpPhotoUrl,_tmpAuthorId,_tmpAuthorName,_tmpAuthorUsername,_tmpAuthorProfileTier,_tmpCuisine,_tmpTag,_tmpDifficulty,_tmpPrepTimeMinutes,_tmpCookTimeMinutes,_tmpServings,_tmpUpvotes,_tmpDownvotes,_tmpCommentCount,_tmpPublishedAt,_tmpCachedAt,_tmpIsTrending,_tmpIsDiscover);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object getDiscoverRecipes(final int limit,
      final Continuation<? super List<RecipeEntity>> $completion) {
    final String _sql = "SELECT * FROM recipes WHERE isDiscover = 1 ORDER BY upvotes DESC LIMIT ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, limit);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<RecipeEntity>>() {
      @Override
      @NonNull
      public List<RecipeEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "title");
          final int _cursorIndexOfSlug = CursorUtil.getColumnIndexOrThrow(_cursor, "slug");
          final int _cursorIndexOfDescription = CursorUtil.getColumnIndexOrThrow(_cursor, "description");
          final int _cursorIndexOfPhotoUrl = CursorUtil.getColumnIndexOrThrow(_cursor, "photoUrl");
          final int _cursorIndexOfAuthorId = CursorUtil.getColumnIndexOrThrow(_cursor, "authorId");
          final int _cursorIndexOfAuthorName = CursorUtil.getColumnIndexOrThrow(_cursor, "authorName");
          final int _cursorIndexOfAuthorUsername = CursorUtil.getColumnIndexOrThrow(_cursor, "authorUsername");
          final int _cursorIndexOfAuthorProfileTier = CursorUtil.getColumnIndexOrThrow(_cursor, "authorProfileTier");
          final int _cursorIndexOfCuisine = CursorUtil.getColumnIndexOrThrow(_cursor, "cuisine");
          final int _cursorIndexOfTag = CursorUtil.getColumnIndexOrThrow(_cursor, "tag");
          final int _cursorIndexOfDifficulty = CursorUtil.getColumnIndexOrThrow(_cursor, "difficulty");
          final int _cursorIndexOfPrepTimeMinutes = CursorUtil.getColumnIndexOrThrow(_cursor, "prepTimeMinutes");
          final int _cursorIndexOfCookTimeMinutes = CursorUtil.getColumnIndexOrThrow(_cursor, "cookTimeMinutes");
          final int _cursorIndexOfServings = CursorUtil.getColumnIndexOrThrow(_cursor, "servings");
          final int _cursorIndexOfUpvotes = CursorUtil.getColumnIndexOrThrow(_cursor, "upvotes");
          final int _cursorIndexOfDownvotes = CursorUtil.getColumnIndexOrThrow(_cursor, "downvotes");
          final int _cursorIndexOfCommentCount = CursorUtil.getColumnIndexOrThrow(_cursor, "commentCount");
          final int _cursorIndexOfPublishedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "publishedAt");
          final int _cursorIndexOfCachedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "cachedAt");
          final int _cursorIndexOfIsTrending = CursorUtil.getColumnIndexOrThrow(_cursor, "isTrending");
          final int _cursorIndexOfIsDiscover = CursorUtil.getColumnIndexOrThrow(_cursor, "isDiscover");
          final List<RecipeEntity> _result = new ArrayList<RecipeEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final RecipeEntity _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpTitle;
            _tmpTitle = _cursor.getString(_cursorIndexOfTitle);
            final String _tmpSlug;
            _tmpSlug = _cursor.getString(_cursorIndexOfSlug);
            final String _tmpDescription;
            if (_cursor.isNull(_cursorIndexOfDescription)) {
              _tmpDescription = null;
            } else {
              _tmpDescription = _cursor.getString(_cursorIndexOfDescription);
            }
            final String _tmpPhotoUrl;
            if (_cursor.isNull(_cursorIndexOfPhotoUrl)) {
              _tmpPhotoUrl = null;
            } else {
              _tmpPhotoUrl = _cursor.getString(_cursorIndexOfPhotoUrl);
            }
            final String _tmpAuthorId;
            _tmpAuthorId = _cursor.getString(_cursorIndexOfAuthorId);
            final String _tmpAuthorName;
            _tmpAuthorName = _cursor.getString(_cursorIndexOfAuthorName);
            final String _tmpAuthorUsername;
            _tmpAuthorUsername = _cursor.getString(_cursorIndexOfAuthorUsername);
            final String _tmpAuthorProfileTier;
            _tmpAuthorProfileTier = _cursor.getString(_cursorIndexOfAuthorProfileTier);
            final String _tmpCuisine;
            if (_cursor.isNull(_cursorIndexOfCuisine)) {
              _tmpCuisine = null;
            } else {
              _tmpCuisine = _cursor.getString(_cursorIndexOfCuisine);
            }
            final String _tmpTag;
            if (_cursor.isNull(_cursorIndexOfTag)) {
              _tmpTag = null;
            } else {
              _tmpTag = _cursor.getString(_cursorIndexOfTag);
            }
            final String _tmpDifficulty;
            if (_cursor.isNull(_cursorIndexOfDifficulty)) {
              _tmpDifficulty = null;
            } else {
              _tmpDifficulty = _cursor.getString(_cursorIndexOfDifficulty);
            }
            final Integer _tmpPrepTimeMinutes;
            if (_cursor.isNull(_cursorIndexOfPrepTimeMinutes)) {
              _tmpPrepTimeMinutes = null;
            } else {
              _tmpPrepTimeMinutes = _cursor.getInt(_cursorIndexOfPrepTimeMinutes);
            }
            final Integer _tmpCookTimeMinutes;
            if (_cursor.isNull(_cursorIndexOfCookTimeMinutes)) {
              _tmpCookTimeMinutes = null;
            } else {
              _tmpCookTimeMinutes = _cursor.getInt(_cursorIndexOfCookTimeMinutes);
            }
            final int _tmpServings;
            _tmpServings = _cursor.getInt(_cursorIndexOfServings);
            final int _tmpUpvotes;
            _tmpUpvotes = _cursor.getInt(_cursorIndexOfUpvotes);
            final int _tmpDownvotes;
            _tmpDownvotes = _cursor.getInt(_cursorIndexOfDownvotes);
            final int _tmpCommentCount;
            _tmpCommentCount = _cursor.getInt(_cursorIndexOfCommentCount);
            final Instant _tmpPublishedAt;
            final Long _tmp;
            if (_cursor.isNull(_cursorIndexOfPublishedAt)) {
              _tmp = null;
            } else {
              _tmp = _cursor.getLong(_cursorIndexOfPublishedAt);
            }
            _tmpPublishedAt = __converters.toInstant(_tmp);
            final Instant _tmpCachedAt;
            final Long _tmp_1;
            if (_cursor.isNull(_cursorIndexOfCachedAt)) {
              _tmp_1 = null;
            } else {
              _tmp_1 = _cursor.getLong(_cursorIndexOfCachedAt);
            }
            final Instant _tmp_2 = __converters.toInstant(_tmp_1);
            if (_tmp_2 == null) {
              throw new IllegalStateException("Expected NON-NULL 'java.time.Instant', but it was NULL.");
            } else {
              _tmpCachedAt = _tmp_2;
            }
            final boolean _tmpIsTrending;
            final int _tmp_3;
            _tmp_3 = _cursor.getInt(_cursorIndexOfIsTrending);
            _tmpIsTrending = _tmp_3 != 0;
            final boolean _tmpIsDiscover;
            final int _tmp_4;
            _tmp_4 = _cursor.getInt(_cursorIndexOfIsDiscover);
            _tmpIsDiscover = _tmp_4 != 0;
            _item = new RecipeEntity(_tmpId,_tmpTitle,_tmpSlug,_tmpDescription,_tmpPhotoUrl,_tmpAuthorId,_tmpAuthorName,_tmpAuthorUsername,_tmpAuthorProfileTier,_tmpCuisine,_tmpTag,_tmpDifficulty,_tmpPrepTimeMinutes,_tmpCookTimeMinutes,_tmpServings,_tmpUpvotes,_tmpDownvotes,_tmpCommentCount,_tmpPublishedAt,_tmpCachedAt,_tmpIsTrending,_tmpIsDiscover);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object getRecipesByAuthor(final String userId,
      final Continuation<? super List<RecipeEntity>> $completion) {
    final String _sql = "SELECT * FROM recipes WHERE authorId = ? ORDER BY publishedAt DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, userId);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<RecipeEntity>>() {
      @Override
      @NonNull
      public List<RecipeEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "title");
          final int _cursorIndexOfSlug = CursorUtil.getColumnIndexOrThrow(_cursor, "slug");
          final int _cursorIndexOfDescription = CursorUtil.getColumnIndexOrThrow(_cursor, "description");
          final int _cursorIndexOfPhotoUrl = CursorUtil.getColumnIndexOrThrow(_cursor, "photoUrl");
          final int _cursorIndexOfAuthorId = CursorUtil.getColumnIndexOrThrow(_cursor, "authorId");
          final int _cursorIndexOfAuthorName = CursorUtil.getColumnIndexOrThrow(_cursor, "authorName");
          final int _cursorIndexOfAuthorUsername = CursorUtil.getColumnIndexOrThrow(_cursor, "authorUsername");
          final int _cursorIndexOfAuthorProfileTier = CursorUtil.getColumnIndexOrThrow(_cursor, "authorProfileTier");
          final int _cursorIndexOfCuisine = CursorUtil.getColumnIndexOrThrow(_cursor, "cuisine");
          final int _cursorIndexOfTag = CursorUtil.getColumnIndexOrThrow(_cursor, "tag");
          final int _cursorIndexOfDifficulty = CursorUtil.getColumnIndexOrThrow(_cursor, "difficulty");
          final int _cursorIndexOfPrepTimeMinutes = CursorUtil.getColumnIndexOrThrow(_cursor, "prepTimeMinutes");
          final int _cursorIndexOfCookTimeMinutes = CursorUtil.getColumnIndexOrThrow(_cursor, "cookTimeMinutes");
          final int _cursorIndexOfServings = CursorUtil.getColumnIndexOrThrow(_cursor, "servings");
          final int _cursorIndexOfUpvotes = CursorUtil.getColumnIndexOrThrow(_cursor, "upvotes");
          final int _cursorIndexOfDownvotes = CursorUtil.getColumnIndexOrThrow(_cursor, "downvotes");
          final int _cursorIndexOfCommentCount = CursorUtil.getColumnIndexOrThrow(_cursor, "commentCount");
          final int _cursorIndexOfPublishedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "publishedAt");
          final int _cursorIndexOfCachedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "cachedAt");
          final int _cursorIndexOfIsTrending = CursorUtil.getColumnIndexOrThrow(_cursor, "isTrending");
          final int _cursorIndexOfIsDiscover = CursorUtil.getColumnIndexOrThrow(_cursor, "isDiscover");
          final List<RecipeEntity> _result = new ArrayList<RecipeEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final RecipeEntity _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpTitle;
            _tmpTitle = _cursor.getString(_cursorIndexOfTitle);
            final String _tmpSlug;
            _tmpSlug = _cursor.getString(_cursorIndexOfSlug);
            final String _tmpDescription;
            if (_cursor.isNull(_cursorIndexOfDescription)) {
              _tmpDescription = null;
            } else {
              _tmpDescription = _cursor.getString(_cursorIndexOfDescription);
            }
            final String _tmpPhotoUrl;
            if (_cursor.isNull(_cursorIndexOfPhotoUrl)) {
              _tmpPhotoUrl = null;
            } else {
              _tmpPhotoUrl = _cursor.getString(_cursorIndexOfPhotoUrl);
            }
            final String _tmpAuthorId;
            _tmpAuthorId = _cursor.getString(_cursorIndexOfAuthorId);
            final String _tmpAuthorName;
            _tmpAuthorName = _cursor.getString(_cursorIndexOfAuthorName);
            final String _tmpAuthorUsername;
            _tmpAuthorUsername = _cursor.getString(_cursorIndexOfAuthorUsername);
            final String _tmpAuthorProfileTier;
            _tmpAuthorProfileTier = _cursor.getString(_cursorIndexOfAuthorProfileTier);
            final String _tmpCuisine;
            if (_cursor.isNull(_cursorIndexOfCuisine)) {
              _tmpCuisine = null;
            } else {
              _tmpCuisine = _cursor.getString(_cursorIndexOfCuisine);
            }
            final String _tmpTag;
            if (_cursor.isNull(_cursorIndexOfTag)) {
              _tmpTag = null;
            } else {
              _tmpTag = _cursor.getString(_cursorIndexOfTag);
            }
            final String _tmpDifficulty;
            if (_cursor.isNull(_cursorIndexOfDifficulty)) {
              _tmpDifficulty = null;
            } else {
              _tmpDifficulty = _cursor.getString(_cursorIndexOfDifficulty);
            }
            final Integer _tmpPrepTimeMinutes;
            if (_cursor.isNull(_cursorIndexOfPrepTimeMinutes)) {
              _tmpPrepTimeMinutes = null;
            } else {
              _tmpPrepTimeMinutes = _cursor.getInt(_cursorIndexOfPrepTimeMinutes);
            }
            final Integer _tmpCookTimeMinutes;
            if (_cursor.isNull(_cursorIndexOfCookTimeMinutes)) {
              _tmpCookTimeMinutes = null;
            } else {
              _tmpCookTimeMinutes = _cursor.getInt(_cursorIndexOfCookTimeMinutes);
            }
            final int _tmpServings;
            _tmpServings = _cursor.getInt(_cursorIndexOfServings);
            final int _tmpUpvotes;
            _tmpUpvotes = _cursor.getInt(_cursorIndexOfUpvotes);
            final int _tmpDownvotes;
            _tmpDownvotes = _cursor.getInt(_cursorIndexOfDownvotes);
            final int _tmpCommentCount;
            _tmpCommentCount = _cursor.getInt(_cursorIndexOfCommentCount);
            final Instant _tmpPublishedAt;
            final Long _tmp;
            if (_cursor.isNull(_cursorIndexOfPublishedAt)) {
              _tmp = null;
            } else {
              _tmp = _cursor.getLong(_cursorIndexOfPublishedAt);
            }
            _tmpPublishedAt = __converters.toInstant(_tmp);
            final Instant _tmpCachedAt;
            final Long _tmp_1;
            if (_cursor.isNull(_cursorIndexOfCachedAt)) {
              _tmp_1 = null;
            } else {
              _tmp_1 = _cursor.getLong(_cursorIndexOfCachedAt);
            }
            final Instant _tmp_2 = __converters.toInstant(_tmp_1);
            if (_tmp_2 == null) {
              throw new IllegalStateException("Expected NON-NULL 'java.time.Instant', but it was NULL.");
            } else {
              _tmpCachedAt = _tmp_2;
            }
            final boolean _tmpIsTrending;
            final int _tmp_3;
            _tmp_3 = _cursor.getInt(_cursorIndexOfIsTrending);
            _tmpIsTrending = _tmp_3 != 0;
            final boolean _tmpIsDiscover;
            final int _tmp_4;
            _tmp_4 = _cursor.getInt(_cursorIndexOfIsDiscover);
            _tmpIsDiscover = _tmp_4 != 0;
            _item = new RecipeEntity(_tmpId,_tmpTitle,_tmpSlug,_tmpDescription,_tmpPhotoUrl,_tmpAuthorId,_tmpAuthorName,_tmpAuthorUsername,_tmpAuthorProfileTier,_tmpCuisine,_tmpTag,_tmpDifficulty,_tmpPrepTimeMinutes,_tmpCookTimeMinutes,_tmpServings,_tmpUpvotes,_tmpDownvotes,_tmpCommentCount,_tmpPublishedAt,_tmpCachedAt,_tmpIsTrending,_tmpIsDiscover);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object getIngredientsByRecipeId(final String recipeId,
      final Continuation<? super List<IngredientEntity>> $completion) {
    final String _sql = "SELECT * FROM ingredients WHERE recipeId = ? ORDER BY sortOrder ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, recipeId);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<IngredientEntity>>() {
      @Override
      @NonNull
      public List<IngredientEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfRecipeId = CursorUtil.getColumnIndexOrThrow(_cursor, "recipeId");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfIngredientKey = CursorUtil.getColumnIndexOrThrow(_cursor, "ingredientKey");
          final int _cursorIndexOfAmount = CursorUtil.getColumnIndexOrThrow(_cursor, "amount");
          final int _cursorIndexOfUnit = CursorUtil.getColumnIndexOrThrow(_cursor, "unit");
          final int _cursorIndexOfNotes = CursorUtil.getColumnIndexOrThrow(_cursor, "notes");
          final int _cursorIndexOfSortOrder = CursorUtil.getColumnIndexOrThrow(_cursor, "sortOrder");
          final List<IngredientEntity> _result = new ArrayList<IngredientEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final IngredientEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpRecipeId;
            _tmpRecipeId = _cursor.getString(_cursorIndexOfRecipeId);
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final String _tmpIngredientKey;
            if (_cursor.isNull(_cursorIndexOfIngredientKey)) {
              _tmpIngredientKey = null;
            } else {
              _tmpIngredientKey = _cursor.getString(_cursorIndexOfIngredientKey);
            }
            final Double _tmpAmount;
            if (_cursor.isNull(_cursorIndexOfAmount)) {
              _tmpAmount = null;
            } else {
              _tmpAmount = _cursor.getDouble(_cursorIndexOfAmount);
            }
            final String _tmpUnit;
            if (_cursor.isNull(_cursorIndexOfUnit)) {
              _tmpUnit = null;
            } else {
              _tmpUnit = _cursor.getString(_cursorIndexOfUnit);
            }
            final String _tmpNotes;
            if (_cursor.isNull(_cursorIndexOfNotes)) {
              _tmpNotes = null;
            } else {
              _tmpNotes = _cursor.getString(_cursorIndexOfNotes);
            }
            final int _tmpSortOrder;
            _tmpSortOrder = _cursor.getInt(_cursorIndexOfSortOrder);
            _item = new IngredientEntity(_tmpId,_tmpRecipeId,_tmpName,_tmpIngredientKey,_tmpAmount,_tmpUnit,_tmpNotes,_tmpSortOrder);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object getStepsByRecipeId(final String recipeId,
      final Continuation<? super List<StepEntity>> $completion) {
    final String _sql = "SELECT * FROM steps WHERE recipeId = ? ORDER BY stepNumber ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, recipeId);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<StepEntity>>() {
      @Override
      @NonNull
      public List<StepEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfRecipeId = CursorUtil.getColumnIndexOrThrow(_cursor, "recipeId");
          final int _cursorIndexOfStepNumber = CursorUtil.getColumnIndexOrThrow(_cursor, "stepNumber");
          final int _cursorIndexOfInstruction = CursorUtil.getColumnIndexOrThrow(_cursor, "instruction");
          final List<StepEntity> _result = new ArrayList<StepEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final StepEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpRecipeId;
            _tmpRecipeId = _cursor.getString(_cursorIndexOfRecipeId);
            final int _tmpStepNumber;
            _tmpStepNumber = _cursor.getInt(_cursorIndexOfStepNumber);
            final String _tmpInstruction;
            _tmpInstruction = _cursor.getString(_cursorIndexOfInstruction);
            _item = new StepEntity(_tmpId,_tmpRecipeId,_tmpStepNumber,_tmpInstruction);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
