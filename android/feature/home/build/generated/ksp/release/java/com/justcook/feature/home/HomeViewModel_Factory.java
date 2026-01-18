package com.justcook.feature.home;

import com.justcook.domain.repository.RecipeRepository;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
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
public final class HomeViewModel_Factory implements Factory<HomeViewModel> {
  private final Provider<RecipeRepository> recipeRepositoryProvider;

  public HomeViewModel_Factory(Provider<RecipeRepository> recipeRepositoryProvider) {
    this.recipeRepositoryProvider = recipeRepositoryProvider;
  }

  @Override
  public HomeViewModel get() {
    return newInstance(recipeRepositoryProvider.get());
  }

  public static HomeViewModel_Factory create(Provider<RecipeRepository> recipeRepositoryProvider) {
    return new HomeViewModel_Factory(recipeRepositoryProvider);
  }

  public static HomeViewModel newInstance(RecipeRepository recipeRepository) {
    return new HomeViewModel(recipeRepository);
  }
}
