package com.justcook.core.network.interceptor;

import com.justcook.core.network.session.SessionProvider;
import dagger.Lazy;
import dagger.internal.DaggerGenerated;
import dagger.internal.DoubleCheck;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

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
public final class AuthInterceptor_Factory implements Factory<AuthInterceptor> {
  private final Provider<SessionProvider> sessionProvider;

  public AuthInterceptor_Factory(Provider<SessionProvider> sessionProvider) {
    this.sessionProvider = sessionProvider;
  }

  @Override
  public AuthInterceptor get() {
    return newInstance(DoubleCheck.lazy(sessionProvider));
  }

  public static AuthInterceptor_Factory create(Provider<SessionProvider> sessionProvider) {
    return new AuthInterceptor_Factory(sessionProvider);
  }

  public static AuthInterceptor newInstance(Lazy<SessionProvider> sessionProvider) {
    return new AuthInterceptor(sessionProvider);
  }
}
