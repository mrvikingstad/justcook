package com.justcook.core.database.di;

import com.justcook.core.database.JustCookDatabase;
import com.justcook.core.database.dao.BookmarkDao;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata
@QualifierMetadata
@DaggerGenerated
@Generated(
    value = "dagger.internal.codegen.ComponentProcessor",
    comments = "https://dagger.dev"
)
@SuppressWarnings({
    "unchecked",
    "rawtypes",
    "KotlinInternal",
    "KotlinInternalInJava"
})
public final class DatabaseModule_ProvideBookmarkDaoFactory implements Factory<BookmarkDao> {
  private final Provider<JustCookDatabase> databaseProvider;

  public DatabaseModule_ProvideBookmarkDaoFactory(Provider<JustCookDatabase> databaseProvider) {
    this.databaseProvider = databaseProvider;
  }

  @Override
  public BookmarkDao get() {
    return provideBookmarkDao(databaseProvider.get());
  }

  public static DatabaseModule_ProvideBookmarkDaoFactory create(
      Provider<JustCookDatabase> databaseProvider) {
    return new DatabaseModule_ProvideBookmarkDaoFactory(databaseProvider);
  }

  public static BookmarkDao provideBookmarkDao(JustCookDatabase database) {
    return Preconditions.checkNotNullFromProvides(DatabaseModule.INSTANCE.provideBookmarkDao(database));
  }
}
