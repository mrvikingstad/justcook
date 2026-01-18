package com.justcook.feature.profile;

import com.justcook.domain.repository.AuthRepository;
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
public final class ProfileViewModel_Factory implements Factory<ProfileViewModel> {
  private final Provider<AuthRepository> authRepositoryProvider;

  private final Provider<RecipeRepository> recipeRepositoryProvider;

  private final Provider<UserRepository> userRepositoryProvider;

  public ProfileViewModel_Factory(Provider<AuthRepository> authRepositoryProvider,
      Provider<RecipeRepository> recipeRepositoryProvider,
      Provider<UserRepository> userRepositoryProvider) {
    this.authRepositoryProvider = authRepositoryProvider;
    this.recipeRepositoryProvider = recipeRepositoryProvider;
    this.userRepositoryProvider = userRepositoryProvider;
  }

  @Override
  public ProfileViewModel get() {
    return newInstance(authRepositoryProvider.get(), recipeRepositoryProvider.get(), userRepositoryProvider.get());
  }

  public static ProfileViewModel_Factory create(Provider<AuthRepository> authRepositoryProvider,
      Provider<RecipeRepository> recipeRepositoryProvider,
      Provider<UserRepository> userRepositoryProvider) {
    return new ProfileViewModel_Factory(authRepositoryProvider, recipeRepositoryProvider, userRepositoryProvider);
  }

  public static ProfileViewModel newInstance(AuthRepository authRepository,
      RecipeRepository recipeRepository, UserRepository userRepository) {
    return new ProfileViewModel(authRepository, recipeRepository, userRepository);
  }
}
