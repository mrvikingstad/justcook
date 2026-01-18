package com.justcook.data.di;

import com.justcook.data.remote.api.RecipeApiService;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;
import retrofit2.Retrofit;

@ScopeMetadata("javax.inject.Singleton")
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
public final class DataModule_ProvideRecipeApiServiceFactory implements Factory<RecipeApiService> {
  private final Provider<Retrofit> retrofitProvider;

  public DataModule_ProvideRecipeApiServiceFactory(Provider<Retrofit> retrofitProvider) {
    this.retrofitProvider = retrofitProvider;
  }

  @Override
  public RecipeApiService get() {
    return provideRecipeApiService(retrofitProvider.get());
  }

  public static DataModule_ProvideRecipeApiServiceFactory create(
      Provider<Retrofit> retrofitProvider) {
    return new DataModule_ProvideRecipeApiServiceFactory(retrofitProvider);
  }

  public static RecipeApiService provideRecipeApiService(Retrofit retrofit) {
    return Preconditions.checkNotNullFromProvides(DataModule.INSTANCE.provideRecipeApiService(retrofit));
  }
}
