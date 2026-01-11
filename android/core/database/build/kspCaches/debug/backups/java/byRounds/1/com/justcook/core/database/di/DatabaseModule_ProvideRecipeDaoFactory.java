package com.justcook.core.database.di;

import com.justcook.core.database.JustCookDatabase;
import com.justcook.core.database.dao.RecipeDao;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
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
public final class DatabaseModule_ProvideRecipeDaoFactory implements Factory<RecipeDao> {
  private final Provider<JustCookDatabase> databaseProvider;

  public DatabaseModule_ProvideRecipeDaoFactory(Provider<JustCookDatabase> databaseProvider) {
    this.databaseProvider = databaseProvider;
  }

  @Override
  public RecipeDao get() {
    return provideRecipeDao(databaseProvider.get());
  }

  public static DatabaseModule_ProvideRecipeDaoFactory create(
      Provider<JustCookDatabase> databaseProvider) {
    return new DatabaseModule_ProvideRecipeDaoFactory(databaseProvider);
  }

  public static RecipeDao provideRecipeDao(JustCookDatabase database) {
    return Preconditions.checkNotNullFromProvides(DatabaseModule.INSTANCE.provideRecipeDao(database));
  }
}
