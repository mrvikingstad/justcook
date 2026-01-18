package com.justcook.feature.recipes;

import androidx.lifecycle.SavedStateHandle;
import com.justcook.domain.repository.AuthRepository;
import com.justcook.domain.repository.BookmarkRepository;
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
public final class RecipeDetailViewModel_Factory implements Factory<RecipeDetailViewModel> {
  private final Provider<SavedStateHandle> savedStateHandleProvider;

  private final Provider<RecipeRepository> recipeRepositoryProvider;

  private final Provider<BookmarkRepository> bookmarkRepositoryProvider;

  private final Provider<AuthRepository> authRepositoryProvider;

  public RecipeDetailViewModel_Factory(Provider<SavedStateHandle> savedStateHandleProvider,
      Provider<RecipeRepository> recipeRepositoryProvider,
      Provider<BookmarkRepository> bookmarkRepositoryProvider,
      Provider<AuthRepository> authRepositoryProvider) {
    this.savedStateHandleProvider = savedStateHandleProvider;
    this.recipeRepositoryProvider = recipeRepositoryProvider;
    this.bookmarkRepositoryProvider = bookmarkRepositoryProvider;
    this.authRepositoryProvider = authRepositoryProvider;
  }

  @Override
  public RecipeDetailViewModel get() {
    return newInstance(savedStateHandleProvider.get(), recipeRepositoryProvider.get(), bookmarkRepositoryProvider.get(), authRepositoryProvider.get());
  }

  public static RecipeDetailViewModel_Factory create(
      Provider<SavedStateHandle> savedStateHandleProvider,
      Provider<RecipeRepository> recipeRepositoryProvider,
      Provider<BookmarkRepository> bookmarkRepositoryProvider,
      Provider<AuthRepository> authRepositoryProvider) {
    return new RecipeDetailViewModel_Factory(savedStateHandleProvider, recipeRepositoryProvider, bookmarkRepositoryProvider, authRepositoryProvider);
  }

  public static RecipeDetailViewModel newInstance(SavedStateHandle savedStateHandle,
      RecipeRepository recipeRepository, BookmarkRepository bookmarkRepository,
      AuthRepository authRepository) {
    return new RecipeDetailViewModel(savedStateHandle, recipeRepository, bookmarkRepository, authRepository);
  }
}
