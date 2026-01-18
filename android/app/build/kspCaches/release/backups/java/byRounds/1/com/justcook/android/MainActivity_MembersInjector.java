package com.justcook.android;

import com.justcook.core.datastore.UserPreferencesDataStore;
import dagger.MembersInjector;
import dagger.internal.DaggerGenerated;
import dagger.internal.InjectedFieldSignature;
import dagger.internal.QualifierMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

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
public final class MainActivity_MembersInjector implements MembersInjector<MainActivity> {
  private final Provider<UserPreferencesDataStore> userPreferencesDataStoreProvider;

  public MainActivity_MembersInjector(
      Provider<UserPreferencesDataStore> userPreferencesDataStoreProvider) {
    this.userPreferencesDataStoreProvider = userPreferencesDataStoreProvider;
  }

  public static MembersInjector<MainActivity> create(
      Provider<UserPreferencesDataStore> userPreferencesDataStoreProvider) {
    return new MainActivity_MembersInjector(userPreferencesDataStoreProvider);
  }

  @Override
  public void injectMembers(MainActivity instance) {
    injectUserPreferencesDataStore(instance, userPreferencesDataStoreProvider.get());
  }

  @InjectedFieldSignature("com.justcook.android.MainActivity.userPreferencesDataStore")
  public static void injectUserPreferencesDataStore(MainActivity instance,
      UserPreferencesDataStore userPreferencesDataStore) {
    instance.userPreferencesDataStore = userPreferencesDataStore;
  }
}
