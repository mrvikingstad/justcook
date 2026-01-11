package com.justcook.core.database.dao;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.justcook.core.database.converter.Converters;
import com.justcook.core.database.entity.UserEntity;
import java.lang.Class;
import java.lang.Exception;
import java.lang.IllegalStateException;
import java.lang.Long;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlinx.coroutines.flow.Flow;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class UserDao_Impl implements UserDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<UserEntity> __insertionAdapterOfUserEntity;

  private final Converters __converters = new Converters();

  private final SharedSQLiteStatement __preparedStmtOfDeleteUser;

  private final SharedSQLiteStatement __preparedStmtOfDeleteAllUsers;

  public UserDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfUserEntity = new EntityInsertionAdapter<UserEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `users` (`id`,`email`,`name`,`username`,`displayUsername`,`fullName`,`country`,`bio`,`photoUrl`,`profileTier`,`emailVerified`,`createdAt`,`cachedAt`) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final UserEntity entity) {
        statement.bindString(1, entity.getId());
        statement.bindString(2, entity.getEmail());
        if (entity.getName() == null) {
          statement.bindNull(3);
        } else {
          statement.bindString(3, entity.getName());
        }
        if (entity.getUsername() == null) {
          statement.bindNull(4);
        } else {
          statement.bindString(4, entity.getUsername());
        }
        if (entity.getDisplayUsername() == null) {
          statement.bindNull(5);
        } else {
          statement.bindString(5, entity.getDisplayUsername());
        }
        if (entity.getFullName() == null) {
          statement.bindNull(6);
        } else {
          statement.bindString(6, entity.getFullName());
        }
        if (entity.getCountry() == null) {
          statement.bindNull(7);
        } else {
          statement.bindString(7, entity.getCountry());
        }
        if (entity.getBio() == null) {
          statement.bindNull(8);
        } else {
          statement.bindString(8, entity.getBio());
        }
        if (entity.getPhotoUrl() == null) {
          statement.bindNull(9);
        } else {
          statement.bindString(9, entity.getPhotoUrl());
        }
        statement.bindString(10, entity.getProfileTier());
        final int _tmp = entity.getEmailVerified() ? 1 : 0;
        statement.bindLong(11, _tmp);
        final Long _tmp_1 = __converters.fromInstant(entity.getCreatedAt());
        if (_tmp_1 == null) {
          statement.bindNull(12);
        } else {
          statement.bindLong(12, _tmp_1);
        }
        final Long _tmp_2 = __converters.fromInstant(entity.getCachedAt());
        if (_tmp_2 == null) {
          statement.bindNull(13);
        } else {
          statement.bindLong(13, _tmp_2);
        }
      }
    };
    this.__preparedStmtOfDeleteUser = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM users WHERE id = ?";
        return _query;
      }
    };
    this.__preparedStmtOfDeleteAllUsers = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM users";
        return _query;
      }
    };
  }

  @Override
  public Object insertUser(final UserEntity user, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfUserEntity.insert(user);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteUser(final String userId, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteUser.acquire();
        int _argIndex = 1;
        _stmt.bindString(_argIndex, userId);
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
          __preparedStmtOfDeleteUser.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteAllUsers(final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteAllUsers.acquire();
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
          __preparedStmtOfDeleteAllUsers.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object getUserById(final String userId,
      final Continuation<? super UserEntity> $completion) {
    final String _sql = "SELECT * FROM users WHERE id = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, userId);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<UserEntity>() {
      @Override
      @Nullable
      public UserEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfEmail = CursorUtil.getColumnIndexOrThrow(_cursor, "email");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfUsername = CursorUtil.getColumnIndexOrThrow(_cursor, "username");
          final int _cursorIndexOfDisplayUsername = CursorUtil.getColumnIndexOrThrow(_cursor, "displayUsername");
          final int _cursorIndexOfFullName = CursorUtil.getColumnIndexOrThrow(_cursor, "fullName");
          final int _cursorIndexOfCountry = CursorUtil.getColumnIndexOrThrow(_cursor, "country");
          final int _cursorIndexOfBio = CursorUtil.getColumnIndexOrThrow(_cursor, "bio");
          final int _cursorIndexOfPhotoUrl = CursorUtil.getColumnIndexOrThrow(_cursor, "photoUrl");
          final int _cursorIndexOfProfileTier = CursorUtil.getColumnIndexOrThrow(_cursor, "profileTier");
          final int _cursorIndexOfEmailVerified = CursorUtil.getColumnIndexOrThrow(_cursor, "emailVerified");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfCachedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "cachedAt");
          final UserEntity _result;
          if (_cursor.moveToFirst()) {
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpEmail;
            _tmpEmail = _cursor.getString(_cursorIndexOfEmail);
            final String _tmpName;
            if (_cursor.isNull(_cursorIndexOfName)) {
              _tmpName = null;
            } else {
              _tmpName = _cursor.getString(_cursorIndexOfName);
            }
            final String _tmpUsername;
            if (_cursor.isNull(_cursorIndexOfUsername)) {
              _tmpUsername = null;
            } else {
              _tmpUsername = _cursor.getString(_cursorIndexOfUsername);
            }
            final String _tmpDisplayUsername;
            if (_cursor.isNull(_cursorIndexOfDisplayUsername)) {
              _tmpDisplayUsername = null;
            } else {
              _tmpDisplayUsername = _cursor.getString(_cursorIndexOfDisplayUsername);
            }
            final String _tmpFullName;
            if (_cursor.isNull(_cursorIndexOfFullName)) {
              _tmpFullName = null;
            } else {
              _tmpFullName = _cursor.getString(_cursorIndexOfFullName);
            }
            final String _tmpCountry;
            if (_cursor.isNull(_cursorIndexOfCountry)) {
              _tmpCountry = null;
            } else {
              _tmpCountry = _cursor.getString(_cursorIndexOfCountry);
            }
            final String _tmpBio;
            if (_cursor.isNull(_cursorIndexOfBio)) {
              _tmpBio = null;
            } else {
              _tmpBio = _cursor.getString(_cursorIndexOfBio);
            }
            final String _tmpPhotoUrl;
            if (_cursor.isNull(_cursorIndexOfPhotoUrl)) {
              _tmpPhotoUrl = null;
            } else {
              _tmpPhotoUrl = _cursor.getString(_cursorIndexOfPhotoUrl);
            }
            final String _tmpProfileTier;
            _tmpProfileTier = _cursor.getString(_cursorIndexOfProfileTier);
            final boolean _tmpEmailVerified;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfEmailVerified);
            _tmpEmailVerified = _tmp != 0;
            final Instant _tmpCreatedAt;
            final Long _tmp_1;
            if (_cursor.isNull(_cursorIndexOfCreatedAt)) {
              _tmp_1 = null;
            } else {
              _tmp_1 = _cursor.getLong(_cursorIndexOfCreatedAt);
            }
            final Instant _tmp_2 = __converters.toInstant(_tmp_1);
            if (_tmp_2 == null) {
              throw new IllegalStateException("Expected NON-NULL 'java.time.Instant', but it was NULL.");
            } else {
              _tmpCreatedAt = _tmp_2;
            }
            final Instant _tmpCachedAt;
            final Long _tmp_3;
            if (_cursor.isNull(_cursorIndexOfCachedAt)) {
              _tmp_3 = null;
            } else {
              _tmp_3 = _cursor.getLong(_cursorIndexOfCachedAt);
            }
            final Instant _tmp_4 = __converters.toInstant(_tmp_3);
            if (_tmp_4 == null) {
              throw new IllegalStateException("Expected NON-NULL 'java.time.Instant', but it was NULL.");
            } else {
              _tmpCachedAt = _tmp_4;
            }
            _result = new UserEntity(_tmpId,_tmpEmail,_tmpName,_tmpUsername,_tmpDisplayUsername,_tmpFullName,_tmpCountry,_tmpBio,_tmpPhotoUrl,_tmpProfileTier,_tmpEmailVerified,_tmpCreatedAt,_tmpCachedAt);
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
  public Object getUserByUsername(final String username,
      final Continuation<? super UserEntity> $completion) {
    final String _sql = "SELECT * FROM users WHERE username = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, username);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<UserEntity>() {
      @Override
      @Nullable
      public UserEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfEmail = CursorUtil.getColumnIndexOrThrow(_cursor, "email");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfUsername = CursorUtil.getColumnIndexOrThrow(_cursor, "username");
          final int _cursorIndexOfDisplayUsername = CursorUtil.getColumnIndexOrThrow(_cursor, "displayUsername");
          final int _cursorIndexOfFullName = CursorUtil.getColumnIndexOrThrow(_cursor, "fullName");
          final int _cursorIndexOfCountry = CursorUtil.getColumnIndexOrThrow(_cursor, "country");
          final int _cursorIndexOfBio = CursorUtil.getColumnIndexOrThrow(_cursor, "bio");
          final int _cursorIndexOfPhotoUrl = CursorUtil.getColumnIndexOrThrow(_cursor, "photoUrl");
          final int _cursorIndexOfProfileTier = CursorUtil.getColumnIndexOrThrow(_cursor, "profileTier");
          final int _cursorIndexOfEmailVerified = CursorUtil.getColumnIndexOrThrow(_cursor, "emailVerified");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfCachedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "cachedAt");
          final UserEntity _result;
          if (_cursor.moveToFirst()) {
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpEmail;
            _tmpEmail = _cursor.getString(_cursorIndexOfEmail);
            final String _tmpName;
            if (_cursor.isNull(_cursorIndexOfName)) {
              _tmpName = null;
            } else {
              _tmpName = _cursor.getString(_cursorIndexOfName);
            }
            final String _tmpUsername;
            if (_cursor.isNull(_cursorIndexOfUsername)) {
              _tmpUsername = null;
            } else {
              _tmpUsername = _cursor.getString(_cursorIndexOfUsername);
            }
            final String _tmpDisplayUsername;
            if (_cursor.isNull(_cursorIndexOfDisplayUsername)) {
              _tmpDisplayUsername = null;
            } else {
              _tmpDisplayUsername = _cursor.getString(_cursorIndexOfDisplayUsername);
            }
            final String _tmpFullName;
            if (_cursor.isNull(_cursorIndexOfFullName)) {
              _tmpFullName = null;
            } else {
              _tmpFullName = _cursor.getString(_cursorIndexOfFullName);
            }
            final String _tmpCountry;
            if (_cursor.isNull(_cursorIndexOfCountry)) {
              _tmpCountry = null;
            } else {
              _tmpCountry = _cursor.getString(_cursorIndexOfCountry);
            }
            final String _tmpBio;
            if (_cursor.isNull(_cursorIndexOfBio)) {
              _tmpBio = null;
            } else {
              _tmpBio = _cursor.getString(_cursorIndexOfBio);
            }
            final String _tmpPhotoUrl;
            if (_cursor.isNull(_cursorIndexOfPhotoUrl)) {
              _tmpPhotoUrl = null;
            } else {
              _tmpPhotoUrl = _cursor.getString(_cursorIndexOfPhotoUrl);
            }
            final String _tmpProfileTier;
            _tmpProfileTier = _cursor.getString(_cursorIndexOfProfileTier);
            final boolean _tmpEmailVerified;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfEmailVerified);
            _tmpEmailVerified = _tmp != 0;
            final Instant _tmpCreatedAt;
            final Long _tmp_1;
            if (_cursor.isNull(_cursorIndexOfCreatedAt)) {
              _tmp_1 = null;
            } else {
              _tmp_1 = _cursor.getLong(_cursorIndexOfCreatedAt);
            }
            final Instant _tmp_2 = __converters.toInstant(_tmp_1);
            if (_tmp_2 == null) {
              throw new IllegalStateException("Expected NON-NULL 'java.time.Instant', but it was NULL.");
            } else {
              _tmpCreatedAt = _tmp_2;
            }
            final Instant _tmpCachedAt;
            final Long _tmp_3;
            if (_cursor.isNull(_cursorIndexOfCachedAt)) {
              _tmp_3 = null;
            } else {
              _tmp_3 = _cursor.getLong(_cursorIndexOfCachedAt);
            }
            final Instant _tmp_4 = __converters.toInstant(_tmp_3);
            if (_tmp_4 == null) {
              throw new IllegalStateException("Expected NON-NULL 'java.time.Instant', but it was NULL.");
            } else {
              _tmpCachedAt = _tmp_4;
            }
            _result = new UserEntity(_tmpId,_tmpEmail,_tmpName,_tmpUsername,_tmpDisplayUsername,_tmpFullName,_tmpCountry,_tmpBio,_tmpPhotoUrl,_tmpProfileTier,_tmpEmailVerified,_tmpCreatedAt,_tmpCachedAt);
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
  public Flow<UserEntity> observeUserById(final String userId) {
    final String _sql = "SELECT * FROM users WHERE id = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, userId);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"users"}, new Callable<UserEntity>() {
      @Override
      @Nullable
      public UserEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfEmail = CursorUtil.getColumnIndexOrThrow(_cursor, "email");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfUsername = CursorUtil.getColumnIndexOrThrow(_cursor, "username");
          final int _cursorIndexOfDisplayUsername = CursorUtil.getColumnIndexOrThrow(_cursor, "displayUsername");
          final int _cursorIndexOfFullName = CursorUtil.getColumnIndexOrThrow(_cursor, "fullName");
          final int _cursorIndexOfCountry = CursorUtil.getColumnIndexOrThrow(_cursor, "country");
          final int _cursorIndexOfBio = CursorUtil.getColumnIndexOrThrow(_cursor, "bio");
          final int _cursorIndexOfPhotoUrl = CursorUtil.getColumnIndexOrThrow(_cursor, "photoUrl");
          final int _cursorIndexOfProfileTier = CursorUtil.getColumnIndexOrThrow(_cursor, "profileTier");
          final int _cursorIndexOfEmailVerified = CursorUtil.getColumnIndexOrThrow(_cursor, "emailVerified");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfCachedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "cachedAt");
          final UserEntity _result;
          if (_cursor.moveToFirst()) {
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpEmail;
            _tmpEmail = _cursor.getString(_cursorIndexOfEmail);
            final String _tmpName;
            if (_cursor.isNull(_cursorIndexOfName)) {
              _tmpName = null;
            } else {
              _tmpName = _cursor.getString(_cursorIndexOfName);
            }
            final String _tmpUsername;
            if (_cursor.isNull(_cursorIndexOfUsername)) {
              _tmpUsername = null;
            } else {
              _tmpUsername = _cursor.getString(_cursorIndexOfUsername);
            }
            final String _tmpDisplayUsername;
            if (_cursor.isNull(_cursorIndexOfDisplayUsername)) {
              _tmpDisplayUsername = null;
            } else {
              _tmpDisplayUsername = _cursor.getString(_cursorIndexOfDisplayUsername);
            }
            final String _tmpFullName;
            if (_cursor.isNull(_cursorIndexOfFullName)) {
              _tmpFullName = null;
            } else {
              _tmpFullName = _cursor.getString(_cursorIndexOfFullName);
            }
            final String _tmpCountry;
            if (_cursor.isNull(_cursorIndexOfCountry)) {
              _tmpCountry = null;
            } else {
              _tmpCountry = _cursor.getString(_cursorIndexOfCountry);
            }
            final String _tmpBio;
            if (_cursor.isNull(_cursorIndexOfBio)) {
              _tmpBio = null;
            } else {
              _tmpBio = _cursor.getString(_cursorIndexOfBio);
            }
            final String _tmpPhotoUrl;
            if (_cursor.isNull(_cursorIndexOfPhotoUrl)) {
              _tmpPhotoUrl = null;
            } else {
              _tmpPhotoUrl = _cursor.getString(_cursorIndexOfPhotoUrl);
            }
            final String _tmpProfileTier;
            _tmpProfileTier = _cursor.getString(_cursorIndexOfProfileTier);
            final boolean _tmpEmailVerified;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfEmailVerified);
            _tmpEmailVerified = _tmp != 0;
            final Instant _tmpCreatedAt;
            final Long _tmp_1;
            if (_cursor.isNull(_cursorIndexOfCreatedAt)) {
              _tmp_1 = null;
            } else {
              _tmp_1 = _cursor.getLong(_cursorIndexOfCreatedAt);
            }
            final Instant _tmp_2 = __converters.toInstant(_tmp_1);
            if (_tmp_2 == null) {
              throw new IllegalStateException("Expected NON-NULL 'java.time.Instant', but it was NULL.");
            } else {
              _tmpCreatedAt = _tmp_2;
            }
            final Instant _tmpCachedAt;
            final Long _tmp_3;
            if (_cursor.isNull(_cursorIndexOfCachedAt)) {
              _tmp_3 = null;
            } else {
              _tmp_3 = _cursor.getLong(_cursorIndexOfCachedAt);
            }
            final Instant _tmp_4 = __converters.toInstant(_tmp_3);
            if (_tmp_4 == null) {
              throw new IllegalStateException("Expected NON-NULL 'java.time.Instant', but it was NULL.");
            } else {
              _tmpCachedAt = _tmp_4;
            }
            _result = new UserEntity(_tmpId,_tmpEmail,_tmpName,_tmpUsername,_tmpDisplayUsername,_tmpFullName,_tmpCountry,_tmpBio,_tmpPhotoUrl,_tmpProfileTier,_tmpEmailVerified,_tmpCreatedAt,_tmpCachedAt);
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

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
