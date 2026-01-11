package com.justcook.feature.search;

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
public final class SearchViewModel_Factory implements Factory<SearchViewModel> {
  private final Provider<RecipeRepository> recipeRepositoryProvider;

  public SearchViewModel_Factory(Provider<RecipeRepository> recipeRepositoryProvider) {
    this.recipeRepositoryProvider = recipeRepositoryProvider;
  }

  @Override
  public SearchViewModel get() {
    return newInstance(recipeRepositoryProvider.get());
  }

  public static SearchViewModel_Factory create(
      Provider<RecipeRepository> recipeRepositoryProvider) {
    return new SearchViewModel_Factory(recipeRepositoryProvider);
  }

  public static SearchViewModel newInstance(RecipeRepository recipeRepository) {
    return new SearchViewModel(recipeRepository);
  }
}
