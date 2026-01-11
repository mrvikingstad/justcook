package com.justcook.data.repository;

import com.justcook.data.remote.api.RecipeApiService;
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
public final class CommentRepositoryImpl_Factory implements Factory<CommentRepositoryImpl> {
  private final Provider<RecipeApiService> recipeApiServiceProvider;

  private final Provider<CoroutineDispatcher> ioDispatcherProvider;

  public CommentRepositoryImpl_Factory(Provider<RecipeApiService> recipeApiServiceProvider,
      Provider<CoroutineDispatcher> ioDispatcherProvider) {
    this.recipeApiServiceProvider = recipeApiServiceProvider;
    this.ioDispatcherProvider = ioDispatcherProvider;
  }

  @Override
  public CommentRepositoryImpl get() {
    return newInstance(recipeApiServiceProvider.get(), ioDispatcherProvider.get());
  }

  public static CommentRepositoryImpl_Factory create(
      Provider<RecipeApiService> recipeApiServiceProvider,
      Provider<CoroutineDispatcher> ioDispatcherProvider) {
    return new CommentRepositoryImpl_Factory(recipeApiServiceProvider, ioDispatcherProvider);
  }

  public static CommentRepositoryImpl newInstance(RecipeApiService recipeApiService,
      CoroutineDispatcher ioDispatcher) {
    return new CommentRepositoryImpl(recipeApiService, ioDispatcher);
  }
}
