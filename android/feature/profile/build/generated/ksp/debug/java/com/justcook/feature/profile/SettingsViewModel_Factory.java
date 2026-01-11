package com.justcook.feature.profile;

import com.justcook.core.datastore.UserPreferencesDataStore;
import com.justcook.domain.repository.AuthRepository;
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
public final class SettingsViewModel_Factory implements Factory<SettingsViewModel> {
  private final Provider<UserPreferencesDataStore> userPreferencesDataStoreProvider;

  private final Provider<AuthRepository> authRepositoryProvider;

  public SettingsViewModel_Factory(
      Provider<UserPreferencesDataStore> userPreferencesDataStoreProvider,
      Provider<AuthRepository> authRepositoryProvider) {
    this.userPreferencesDataStoreProvider = userPreferencesDataStoreProvider;
    this.authRepositoryProvider = authRepositoryProvider;
  }

  @Override
  public SettingsViewModel get() {
    return newInstance(userPreferencesDataStoreProvider.get(), authRepositoryProvider.get());
  }

  public static SettingsViewModel_Factory create(
      Provider<UserPreferencesDataStore> userPreferencesDataStoreProvider,
      Provider<AuthRepository> authRepositoryProvider) {
    return new SettingsViewModel_Factory(userPreferencesDataStoreProvider, authRepositoryProvider);
  }

  public static SettingsViewModel newInstance(UserPreferencesDataStore userPreferencesDataStore,
      AuthRepository authRepository) {
    return new SettingsViewModel(userPreferencesDataStore, authRepository);
  }
}
