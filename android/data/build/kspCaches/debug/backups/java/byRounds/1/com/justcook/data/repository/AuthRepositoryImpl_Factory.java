package com.justcook.data.repository;

import com.justcook.core.datastore.UserPreferencesDataStore;
import com.justcook.data.remote.api.AuthApiService;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;
import kotlinx.coroutines.CoroutineDispatcher;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata("com.justcook.core.common.di.IoDispatcher")
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
public final class AuthRepositoryImpl_Factory implements Factory<AuthRepositoryImpl> {
  private final Provider<AuthApiService> authApiProvider;

  private final Provider<UserPreferencesDataStore> userPreferencesDataStoreProvider;

  private final Provider<CoroutineDispatcher> ioDispatcherProvider;

  public AuthRepositoryImpl_Factory(Provider<AuthApiService> authApiProvider,
      Provider<UserPreferencesDataStore> userPreferencesDataStoreProvider,
      Provider<CoroutineDispatcher> ioDispatcherProvider) {
    this.authApiProvider = authApiProvider;
    this.userPreferencesDataStoreProvider = userPreferencesDataStoreProvider;
    this.ioDispatcherProvider = ioDispatcherProvider;
  }

  @Override
  public AuthRepositoryImpl get() {
    return newInstance(authApiProvider.get(), userPreferencesDataStoreProvider.get(), ioDispatcherProvider.get());
  }

  public static AuthRepositoryImpl_Factory create(Provider<AuthApiService> authApiProvider,
      Provider<UserPreferencesDataStore> userPreferencesDataStoreProvider,
      Provider<CoroutineDispatcher> ioDispatcherProvider) {
    return new AuthRepositoryImpl_Factory(authApiProvider, userPreferencesDataStoreProvider, ioDispatcherProvider);
  }

  public static AuthRepositoryImpl newInstance(AuthApiService authApi,
      UserPreferencesDataStore userPreferencesDataStore, CoroutineDispatcher ioDispatcher) {
    return new AuthRepositoryImpl(authApi, userPreferencesDataStore, ioDispatcher);
  }
}
