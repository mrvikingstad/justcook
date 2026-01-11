package com.justcook.data.di;

import com.justcook.data.remote.api.BookmarkApiService;
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
public final class DataModule_ProvideBookmarkApiServiceFactory implements Factory<BookmarkApiService> {
  private final Provider<Retrofit> retrofitProvider;

  public DataModule_ProvideBookmarkApiServiceFactory(Provider<Retrofit> retrofitProvider) {
    this.retrofitProvider = retrofitProvider;
  }

  @Override
  public BookmarkApiService get() {
    return provideBookmarkApiService(retrofitProvider.get());
  }

  public static DataModule_ProvideBookmarkApiServiceFactory create(
      Provider<Retrofit> retrofitProvider) {
    return new DataModule_ProvideBookmarkApiServiceFactory(retrofitProvider);
  }

  public static BookmarkApiService provideBookmarkApiService(Retrofit retrofit) {
    return Preconditions.checkNotNullFromProvides(DataModule.INSTANCE.provideBookmarkApiService(retrofit));
  }
}
