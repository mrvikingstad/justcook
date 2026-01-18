package com.justcook.data.repository;

import com.justcook.core.database.dao.BookmarkDao;
import com.justcook.core.database.dao.RecipeDao;
import com.justcook.data.remote.api.BookmarkApiService;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;
import kotlinx.coroutines.CoroutineDispatcher;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata("com.justcook.core.common.di.IoDispatcher")
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
public final class BookmarkRepositoryImpl_Factory implements Factory<BookmarkRepositoryImpl> {
  private final Provider<BookmarkApiService> bookmarkApiProvider;

  private final Provider<BookmarkDao> bookmarkDaoProvider;

  private final Provider<RecipeDao> recipeDaoProvider;

  private final Provider<CoroutineDispatcher> ioDispatcherProvider;

  public BookmarkRepositoryImpl_Factory(Provider<BookmarkApiService> bookmarkApiProvider,
      Provider<BookmarkDao> bookmarkDaoProvider, Provider<RecipeDao> recipeDaoProvider,
      Provider<CoroutineDispatcher> ioDispatcherProvider) {
    this.bookmarkApiProvider = bookmarkApiProvider;
    this.bookmarkDaoProvider = bookmarkDaoProvider;
    this.recipeDaoProvider = recipeDaoProvider;
    this.ioDispatcherProvider = ioDispatcherProvider;
  }

  @Override
  public BookmarkRepositoryImpl get() {
    return newInstance(bookmarkApiProvider.get(), bookmarkDaoProvider.get(), recipeDaoProvider.get(), ioDispatcherProvider.get());
  }

  public static BookmarkRepositoryImpl_Factory create(
      Provider<BookmarkApiService> bookmarkApiProvider, Provider<BookmarkDao> bookmarkDaoProvider,
      Provider<RecipeDao> recipeDaoProvider, Provider<CoroutineDispatcher> ioDispatcherProvider) {
    return new BookmarkRepositoryImpl_Factory(bookmarkApiProvider, bookmarkDaoProvider, recipeDaoProvider, ioDispatcherProvider);
  }

  public static BookmarkRepositoryImpl newInstance(BookmarkApiService bookmarkApi,
      BookmarkDao bookmarkDao, RecipeDao recipeDao, CoroutineDispatcher ioDispatcher) {
    return new BookmarkRepositoryImpl(bookmarkApi, bookmarkDao, recipeDao, ioDispatcher);
  }
}
