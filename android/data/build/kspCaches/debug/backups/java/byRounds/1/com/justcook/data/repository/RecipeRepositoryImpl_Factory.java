package com.justcook.data.repository;

import com.justcook.core.database.dao.RecipeDao;
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
public final class RecipeRepositoryImpl_Factory implements Factory<RecipeRepositoryImpl> {
  private final Provider<RecipeApiService> recipeApiProvider;

  private final Provider<RecipeDao> recipeDaoProvider;

  private final Provider<CoroutineDispatcher> ioDispatcherProvider;

  public RecipeRepositoryImpl_Factory(Provider<RecipeApiService> recipeApiProvider,
      Provider<RecipeDao> recipeDaoProvider, Provider<CoroutineDispatcher> ioDispatcherProvider) {
    this.recipeApiProvider = recipeApiProvider;
    this.recipeDaoProvider = recipeDaoProvider;
    this.ioDispatcherProvider = ioDispatcherProvider;
  }

  @Override
  public RecipeRepositoryImpl get() {
    return newInstance(recipeApiProvider.get(), recipeDaoProvider.get(), ioDispatcherProvider.get());
  }

  public static RecipeRepositoryImpl_Factory create(Provider<RecipeApiService> recipeApiProvider,
      Provider<RecipeDao> recipeDaoProvider, Provider<CoroutineDispatcher> ioDispatcherProvider) {
    return new RecipeRepositoryImpl_Factory(recipeApiProvider, recipeDaoProvider, ioDispatcherProvider);
  }

  public static RecipeRepositoryImpl newInstance(RecipeApiService recipeApi, RecipeDao recipeDao,
      CoroutineDispatcher ioDispatcher) {
    return new RecipeRepositoryImpl(recipeApi, recipeDao, ioDispatcher);
  }
}
