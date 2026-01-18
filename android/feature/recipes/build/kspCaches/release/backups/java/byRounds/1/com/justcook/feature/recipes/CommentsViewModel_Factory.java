package com.justcook.feature.recipes;

import androidx.lifecycle.SavedStateHandle;
import com.justcook.domain.repository.AuthRepository;
import com.justcook.domain.repository.CommentRepository;
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
public final class CommentsViewModel_Factory implements Factory<CommentsViewModel> {
  private final Provider<SavedStateHandle> savedStateHandleProvider;

  private final Provider<CommentRepository> commentRepositoryProvider;

  private final Provider<RecipeRepository> recipeRepositoryProvider;

  private final Provider<AuthRepository> authRepositoryProvider;

  public CommentsViewModel_Factory(Provider<SavedStateHandle> savedStateHandleProvider,
      Provider<CommentRepository> commentRepositoryProvider,
      Provider<RecipeRepository> recipeRepositoryProvider,
      Provider<AuthRepository> authRepositoryProvider) {
    this.savedStateHandleProvider = savedStateHandleProvider;
    this.commentRepositoryProvider = commentRepositoryProvider;
    this.recipeRepositoryProvider = recipeRepositoryProvider;
    this.authRepositoryProvider = authRepositoryProvider;
  }

  @Override
  public CommentsViewModel get() {
    return newInstance(savedStateHandleProvider.get(), commentRepositoryProvider.get(), recipeRepositoryProvider.get(), authRepositoryProvider.get());
  }

  public static CommentsViewModel_Factory create(
      Provider<SavedStateHandle> savedStateHandleProvider,
      Provider<CommentRepository> commentRepositoryProvider,
      Provider<RecipeRepository> recipeRepositoryProvider,
      Provider<AuthRepository> authRepositoryProvider) {
    return new CommentsViewModel_Factory(savedStateHandleProvider, commentRepositoryProvider, recipeRepositoryProvider, authRepositoryProvider);
  }

  public static CommentsViewModel newInstance(SavedStateHandle savedStateHandle,
      CommentRepository commentRepository, RecipeRepository recipeRepository,
      AuthRepository authRepository) {
    return new CommentsViewModel(savedStateHandle, commentRepository, recipeRepository, authRepository);
  }
}
