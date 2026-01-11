package com.justcook.core.common.di;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import kotlinx.coroutines.CoroutineDispatcher;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata("com.justcook.core.common.di.MainDispatcher")
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
public final class DispatchersModule_ProvideMainDispatcherFactory implements Factory<CoroutineDispatcher> {
  @Override
  public CoroutineDispatcher get() {
    return provideMainDispatcher();
  }

  public static DispatchersModule_ProvideMainDispatcherFactory create() {
    return InstanceHolder.INSTANCE;
  }

  public static CoroutineDispatcher provideMainDispatcher() {
    return Preconditions.checkNotNullFromProvides(DispatchersModule.INSTANCE.provideMainDispatcher());
  }

  private static final class InstanceHolder {
    private static final DispatchersModule_ProvideMainDispatcherFactory INSTANCE = new DispatchersModule_ProvideMainDispatcherFactory();
  }
}
