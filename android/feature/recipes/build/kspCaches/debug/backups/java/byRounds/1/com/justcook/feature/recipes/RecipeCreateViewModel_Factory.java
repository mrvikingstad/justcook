package com.justcook.feature.recipes;

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
public final class RecipeCreateViewModel_Factory implements Factory<RecipeCreateViewModel> {
  private final Provider<RecipeRepository> recipeRepositoryProvider;

  public RecipeCreateViewModel_Factory(Provider<RecipeRepository> recipeRepositoryProvider) {
    this.recipeRepositoryProvider = recipeRepositoryProvider;
  }

  @Override
  public RecipeCreateViewModel get() {
    return newInstance(recipeRepositoryProvider.get());
  }

  public static RecipeCreateViewModel_Factory create(
      Provider<RecipeRepository> recipeRepositoryProvider) {
    return new RecipeCreateViewModel_Factory(recipeRepositoryProvider);
  }

  public static RecipeCreateViewModel newInstance(RecipeRepository recipeRepository) {
    return new RecipeCreateViewModel(recipeRepository);
  }
}
