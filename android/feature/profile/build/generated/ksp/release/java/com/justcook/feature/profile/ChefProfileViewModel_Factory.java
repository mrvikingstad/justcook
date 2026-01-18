package com.justcook.feature.profile;

import androidx.lifecycle.SavedStateHandle;
import com.justcook.domain.repository.RecipeRepository;
import com.justcook.domain.repository.UserRepository;
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
public final class ChefProfileViewModel_Factory implements Factory<ChefProfileViewModel> {
  private final Provider<SavedStateHandle> savedStateHandleProvider;

  private final Provider<UserRepository> userRepositoryProvider;

  private final Provider<RecipeRepository> recipeRepositoryProvider;

  public ChefProfileViewModel_Factory(Provider<SavedStateHandle> savedStateHandleProvider,
      Provider<UserRepository> userRepositoryProvider,
      Provider<RecipeRepository> recipeRepositoryProvider) {
    this.savedStateHandleProvider = savedStateHandleProvider;
    this.userRepositoryProvider = userRepositoryProvider;
    this.recipeRepositoryProvider = recipeRepositoryProvider;
  }

  @Override
  public ChefProfileViewModel get() {
    return newInstance(savedStateHandleProvider.get(), userRepositoryProvider.get(), recipeRepositoryProvider.get());
  }

  public static ChefProfileViewModel_Factory create(
      Provider<SavedStateHandle> savedStateHandleProvider,
      Provider<UserRepository> userRepositoryProvider,
      Provider<RecipeRepository> recipeRepositoryProvider) {
    return new ChefProfileViewModel_Factory(savedStateHandleProvider, userRepositoryProvider, recipeRepositoryProvider);
  }

  public static ChefProfileViewModel newInstance(SavedStateHandle savedStateHandle,
      UserRepository userRepository, RecipeRepository recipeRepository) {
    return new ChefProfileViewModel(savedStateHandle, userRepository, recipeRepository);
  }
}
