package com.justcook.android;

import android.app.Activity;
import android.app.Service;
import android.view.View;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModel;
import com.justcook.core.common.di.DispatchersModule_ProvideIoDispatcherFactory;
import com.justcook.core.database.JustCookDatabase;
import com.justcook.core.database.dao.BookmarkDao;
import com.justcook.core.database.dao.RecipeDao;
import com.justcook.core.database.di.DatabaseModule_ProvideBookmarkDaoFactory;
import com.justcook.core.database.di.DatabaseModule_ProvideDatabaseFactory;
import com.justcook.core.database.di.DatabaseModule_ProvideRecipeDaoFactory;
import com.justcook.core.datastore.UserPreferencesDataStore;
import com.justcook.core.datastore.di.DataStoreModule_ProvideUserPreferencesDataStoreFactory;
import com.justcook.core.network.di.NetworkModule_ProvideJsonFactory;
import com.justcook.core.network.di.NetworkModule_ProvideLoggingInterceptorFactory;
import com.justcook.core.network.di.NetworkModule_ProvideOkHttpClientFactory;
import com.justcook.core.network.di.NetworkModule_ProvideRetrofitFactory;
import com.justcook.core.network.interceptor.AuthInterceptor;
import com.justcook.data.di.DataModule_ProvideAuthApiServiceFactory;
import com.justcook.data.di.DataModule_ProvideBookmarkApiServiceFactory;
import com.justcook.data.di.DataModule_ProvideRecipeApiServiceFactory;
import com.justcook.data.remote.api.AuthApiService;
import com.justcook.data.remote.api.BookmarkApiService;
import com.justcook.data.remote.api.RecipeApiService;
import com.justcook.data.repository.AuthRepositoryImpl;
import com.justcook.data.repository.BookmarkRepositoryImpl;
import com.justcook.data.repository.CommentRepositoryImpl;
import com.justcook.data.repository.RecipeRepositoryImpl;
import com.justcook.data.repository.UserRepositoryImpl;
import com.justcook.feature.auth.AuthViewModel;
import com.justcook.feature.auth.AuthViewModel_HiltModules_KeyModule_ProvideFactory;
import com.justcook.feature.bookmarks.BookmarksViewModel;
import com.justcook.feature.bookmarks.BookmarksViewModel_HiltModules_KeyModule_ProvideFactory;
import com.justcook.feature.home.HomeViewModel;
import com.justcook.feature.home.HomeViewModel_HiltModules_KeyModule_ProvideFactory;
import com.justcook.feature.profile.ChefProfileViewModel;
import com.justcook.feature.profile.ChefProfileViewModel_HiltModules_KeyModule_ProvideFactory;
import com.justcook.feature.profile.FollowingViewModel;
import com.justcook.feature.profile.FollowingViewModel_HiltModules_KeyModule_ProvideFactory;
import com.justcook.feature.profile.ProfileViewModel;
import com.justcook.feature.profile.ProfileViewModel_HiltModules_KeyModule_ProvideFactory;
import com.justcook.feature.profile.SettingsViewModel;
import com.justcook.feature.profile.SettingsViewModel_HiltModules_KeyModule_ProvideFactory;
import com.justcook.feature.recipes.CommentsViewModel;
import com.justcook.feature.recipes.CommentsViewModel_HiltModules_KeyModule_ProvideFactory;
import com.justcook.feature.recipes.RecipeCreateViewModel;
import com.justcook.feature.recipes.RecipeCreateViewModel_HiltModules_KeyModule_ProvideFactory;
import com.justcook.feature.recipes.RecipeDetailViewModel;
import com.justcook.feature.recipes.RecipeDetailViewModel_HiltModules_KeyModule_ProvideFactory;
import com.justcook.feature.search.SearchViewModel;
import com.justcook.feature.search.SearchViewModel_HiltModules_KeyModule_ProvideFactory;
import dagger.hilt.android.ActivityRetainedLifecycle;
import dagger.hilt.android.ViewModelLifecycle;
import dagger.hilt.android.internal.builders.ActivityComponentBuilder;
import dagger.hilt.android.internal.builders.ActivityRetainedComponentBuilder;
import dagger.hilt.android.internal.builders.FragmentComponentBuilder;
import dagger.hilt.android.internal.builders.ServiceComponentBuilder;
import dagger.hilt.android.internal.builders.ViewComponentBuilder;
import dagger.hilt.android.internal.builders.ViewModelComponentBuilder;
import dagger.hilt.android.internal.builders.ViewWithFragmentComponentBuilder;
import dagger.hilt.android.internal.lifecycle.DefaultViewModelFactories;
import dagger.hilt.android.internal.lifecycle.DefaultViewModelFactories_InternalFactoryFactory_Factory;
import dagger.hilt.android.internal.managers.ActivityRetainedComponentManager_LifecycleModule_ProvideActivityRetainedLifecycleFactory;
import dagger.hilt.android.internal.managers.SavedStateHandleHolder;
import dagger.hilt.android.internal.modules.ApplicationContextModule;
import dagger.hilt.android.internal.modules.ApplicationContextModule_ProvideContextFactory;
import dagger.internal.DaggerGenerated;
import dagger.internal.DelegateFactory;
import dagger.internal.DoubleCheck;
import dagger.internal.MapBuilder;
import dagger.internal.Preconditions;
import dagger.internal.Provider;
import dagger.internal.SetBuilder;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.Generated;
import kotlinx.coroutines.CoroutineDispatcher;
import kotlinx.serialization.json.Json;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;

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
public final class DaggerJustCookApplication_HiltComponents_SingletonC {
  private DaggerJustCookApplication_HiltComponents_SingletonC() {
  }

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder {
    private ApplicationContextModule applicationContextModule;

    private Builder() {
    }

    public Builder applicationContextModule(ApplicationContextModule applicationContextModule) {
      this.applicationContextModule = Preconditions.checkNotNull(applicationContextModule);
      return this;
    }

    public JustCookApplication_HiltComponents.SingletonC build() {
      Preconditions.checkBuilderRequirement(applicationContextModule, ApplicationContextModule.class);
      return new SingletonCImpl(applicationContextModule);
    }
  }

  private static final class ActivityRetainedCBuilder implements JustCookApplication_HiltComponents.ActivityRetainedC.Builder {
    private final SingletonCImpl singletonCImpl;

    private SavedStateHandleHolder savedStateHandleHolder;

    private ActivityRetainedCBuilder(SingletonCImpl singletonCImpl) {
      this.singletonCImpl = singletonCImpl;
    }

    @Override
    public ActivityRetainedCBuilder savedStateHandleHolder(
        SavedStateHandleHolder savedStateHandleHolder) {
      this.savedStateHandleHolder = Preconditions.checkNotNull(savedStateHandleHolder);
      return this;
    }

    @Override
    public JustCookApplication_HiltComponents.ActivityRetainedC build() {
      Preconditions.checkBuilderRequirement(savedStateHandleHolder, SavedStateHandleHolder.class);
      return new ActivityRetainedCImpl(singletonCImpl, savedStateHandleHolder);
    }
  }

  private static final class ActivityCBuilder implements JustCookApplication_HiltComponents.ActivityC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private Activity activity;

    private ActivityCBuilder(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
    }

    @Override
    public ActivityCBuilder activity(Activity activity) {
      this.activity = Preconditions.checkNotNull(activity);
      return this;
    }

    @Override
    public JustCookApplication_HiltComponents.ActivityC build() {
      Preconditions.checkBuilderRequirement(activity, Activity.class);
      return new ActivityCImpl(singletonCImpl, activityRetainedCImpl, activity);
    }
  }

  private static final class FragmentCBuilder implements JustCookApplication_HiltComponents.FragmentC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private Fragment fragment;

    private FragmentCBuilder(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, ActivityCImpl activityCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;
    }

    @Override
    public FragmentCBuilder fragment(Fragment fragment) {
      this.fragment = Preconditions.checkNotNull(fragment);
      return this;
    }

    @Override
    public JustCookApplication_HiltComponents.FragmentC build() {
      Preconditions.checkBuilderRequirement(fragment, Fragment.class);
      return new FragmentCImpl(singletonCImpl, activityRetainedCImpl, activityCImpl, fragment);
    }
  }

  private static final class ViewWithFragmentCBuilder implements JustCookApplication_HiltComponents.ViewWithFragmentC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private final FragmentCImpl fragmentCImpl;

    private View view;

    private ViewWithFragmentCBuilder(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, ActivityCImpl activityCImpl,
        FragmentCImpl fragmentCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;
      this.fragmentCImpl = fragmentCImpl;
    }

    @Override
    public ViewWithFragmentCBuilder view(View view) {
      this.view = Preconditions.checkNotNull(view);
      return this;
    }

    @Override
    public JustCookApplication_HiltComponents.ViewWithFragmentC build() {
      Preconditions.checkBuilderRequirement(view, View.class);
      return new ViewWithFragmentCImpl(singletonCImpl, activityRetainedCImpl, activityCImpl, fragmentCImpl, view);
    }
  }

  private static final class ViewCBuilder implements JustCookApplication_HiltComponents.ViewC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private View view;

    private ViewCBuilder(SingletonCImpl singletonCImpl, ActivityRetainedCImpl activityRetainedCImpl,
        ActivityCImpl activityCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;
    }

    @Override
    public ViewCBuilder view(View view) {
      this.view = Preconditions.checkNotNull(view);
      return this;
    }

    @Override
    public JustCookApplication_HiltComponents.ViewC build() {
      Preconditions.checkBuilderRequirement(view, View.class);
      return new ViewCImpl(singletonCImpl, activityRetainedCImpl, activityCImpl, view);
    }
  }

  private static final class ViewModelCBuilder implements JustCookApplication_HiltComponents.ViewModelC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private SavedStateHandle savedStateHandle;

    private ViewModelLifecycle viewModelLifecycle;

    private ViewModelCBuilder(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
    }

    @Override
    public ViewModelCBuilder savedStateHandle(SavedStateHandle handle) {
      this.savedStateHandle = Preconditions.checkNotNull(handle);
      return this;
    }

    @Override
    public ViewModelCBuilder viewModelLifecycle(ViewModelLifecycle viewModelLifecycle) {
      this.viewModelLifecycle = Preconditions.checkNotNull(viewModelLifecycle);
      return this;
    }

    @Override
    public JustCookApplication_HiltComponents.ViewModelC build() {
      Preconditions.checkBuilderRequirement(savedStateHandle, SavedStateHandle.class);
      Preconditions.checkBuilderRequirement(viewModelLifecycle, ViewModelLifecycle.class);
      return new ViewModelCImpl(singletonCImpl, activityRetainedCImpl, savedStateHandle, viewModelLifecycle);
    }
  }

  private static final class ServiceCBuilder implements JustCookApplication_HiltComponents.ServiceC.Builder {
    private final SingletonCImpl singletonCImpl;

    private Service service;

    private ServiceCBuilder(SingletonCImpl singletonCImpl) {
      this.singletonCImpl = singletonCImpl;
    }

    @Override
    public ServiceCBuilder service(Service service) {
      this.service = Preconditions.checkNotNull(service);
      return this;
    }

    @Override
    public JustCookApplication_HiltComponents.ServiceC build() {
      Preconditions.checkBuilderRequirement(service, Service.class);
      return new ServiceCImpl(singletonCImpl, service);
    }
  }

  private static final class ViewWithFragmentCImpl extends JustCookApplication_HiltComponents.ViewWithFragmentC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private final FragmentCImpl fragmentCImpl;

    private final ViewWithFragmentCImpl viewWithFragmentCImpl = this;

    private ViewWithFragmentCImpl(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, ActivityCImpl activityCImpl,
        FragmentCImpl fragmentCImpl, View viewParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;
      this.fragmentCImpl = fragmentCImpl;


    }
  }

  private static final class FragmentCImpl extends JustCookApplication_HiltComponents.FragmentC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private final FragmentCImpl fragmentCImpl = this;

    private FragmentCImpl(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, ActivityCImpl activityCImpl,
        Fragment fragmentParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;


    }

    @Override
    public DefaultViewModelFactories.InternalFactoryFactory getHiltInternalFactoryFactory() {
      return activityCImpl.getHiltInternalFactoryFactory();
    }

    @Override
    public ViewWithFragmentComponentBuilder viewWithFragmentComponentBuilder() {
      return new ViewWithFragmentCBuilder(singletonCImpl, activityRetainedCImpl, activityCImpl, fragmentCImpl);
    }
  }

  private static final class ViewCImpl extends JustCookApplication_HiltComponents.ViewC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private final ViewCImpl viewCImpl = this;

    private ViewCImpl(SingletonCImpl singletonCImpl, ActivityRetainedCImpl activityRetainedCImpl,
        ActivityCImpl activityCImpl, View viewParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;


    }
  }

  private static final class ActivityCImpl extends JustCookApplication_HiltComponents.ActivityC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl = this;

    private ActivityCImpl(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, Activity activityParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;


    }

    @Override
    public void injectMainActivity(MainActivity arg0) {
      injectMainActivity2(arg0);
    }

    @Override
    public DefaultViewModelFactories.InternalFactoryFactory getHiltInternalFactoryFactory() {
      return DefaultViewModelFactories_InternalFactoryFactory_Factory.newInstance(getViewModelKeys(), new ViewModelCBuilder(singletonCImpl, activityRetainedCImpl));
    }

    @Override
    public Set<String> getViewModelKeys() {
      return SetBuilder.<String>newSetBuilder(11).add(AuthViewModel_HiltModules_KeyModule_ProvideFactory.provide()).add(BookmarksViewModel_HiltModules_KeyModule_ProvideFactory.provide()).add(ChefProfileViewModel_HiltModules_KeyModule_ProvideFactory.provide()).add(CommentsViewModel_HiltModules_KeyModule_ProvideFactory.provide()).add(FollowingViewModel_HiltModules_KeyModule_ProvideFactory.provide()).add(HomeViewModel_HiltModules_KeyModule_ProvideFactory.provide()).add(ProfileViewModel_HiltModules_KeyModule_ProvideFactory.provide()).add(RecipeCreateViewModel_HiltModules_KeyModule_ProvideFactory.provide()).add(RecipeDetailViewModel_HiltModules_KeyModule_ProvideFactory.provide()).add(SearchViewModel_HiltModules_KeyModule_ProvideFactory.provide()).add(SettingsViewModel_HiltModules_KeyModule_ProvideFactory.provide()).build();
    }

    @Override
    public ViewModelComponentBuilder getViewModelComponentBuilder() {
      return new ViewModelCBuilder(singletonCImpl, activityRetainedCImpl);
    }

    @Override
    public FragmentComponentBuilder fragmentComponentBuilder() {
      return new FragmentCBuilder(singletonCImpl, activityRetainedCImpl, activityCImpl);
    }

    @Override
    public ViewComponentBuilder viewComponentBuilder() {
      return new ViewCBuilder(singletonCImpl, activityRetainedCImpl, activityCImpl);
    }

    private MainActivity injectMainActivity2(MainActivity instance) {
      MainActivity_MembersInjector.injectUserPreferencesDataStore(instance, singletonCImpl.provideUserPreferencesDataStoreProvider.get());
      return instance;
    }
  }

  private static final class ViewModelCImpl extends JustCookApplication_HiltComponents.ViewModelC {
    private final SavedStateHandle savedStateHandle;

    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ViewModelCImpl viewModelCImpl = this;

    private Provider<AuthViewModel> authViewModelProvider;

    private Provider<BookmarksViewModel> bookmarksViewModelProvider;

    private Provider<ChefProfileViewModel> chefProfileViewModelProvider;

    private Provider<CommentsViewModel> commentsViewModelProvider;

    private Provider<FollowingViewModel> followingViewModelProvider;

    private Provider<HomeViewModel> homeViewModelProvider;

    private Provider<ProfileViewModel> profileViewModelProvider;

    private Provider<RecipeCreateViewModel> recipeCreateViewModelProvider;

    private Provider<RecipeDetailViewModel> recipeDetailViewModelProvider;

    private Provider<SearchViewModel> searchViewModelProvider;

    private Provider<SettingsViewModel> settingsViewModelProvider;

    private ViewModelCImpl(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, SavedStateHandle savedStateHandleParam,
        ViewModelLifecycle viewModelLifecycleParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.savedStateHandle = savedStateHandleParam;
      initialize(savedStateHandleParam, viewModelLifecycleParam);

    }

    @SuppressWarnings("unchecked")
    private void initialize(final SavedStateHandle savedStateHandleParam,
        final ViewModelLifecycle viewModelLifecycleParam) {
      this.authViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 0);
      this.bookmarksViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 1);
      this.chefProfileViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 2);
      this.commentsViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 3);
      this.followingViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 4);
      this.homeViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 5);
      this.profileViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 6);
      this.recipeCreateViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 7);
      this.recipeDetailViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 8);
      this.searchViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 9);
      this.settingsViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 10);
    }

    @Override
    public Map<String, javax.inject.Provider<ViewModel>> getHiltViewModelMap() {
      return MapBuilder.<String, javax.inject.Provider<ViewModel>>newMapBuilder(11).put("com.justcook.feature.auth.AuthViewModel", ((Provider) authViewModelProvider)).put("com.justcook.feature.bookmarks.BookmarksViewModel", ((Provider) bookmarksViewModelProvider)).put("com.justcook.feature.profile.ChefProfileViewModel", ((Provider) chefProfileViewModelProvider)).put("com.justcook.feature.recipes.CommentsViewModel", ((Provider) commentsViewModelProvider)).put("com.justcook.feature.profile.FollowingViewModel", ((Provider) followingViewModelProvider)).put("com.justcook.feature.home.HomeViewModel", ((Provider) homeViewModelProvider)).put("com.justcook.feature.profile.ProfileViewModel", ((Provider) profileViewModelProvider)).put("com.justcook.feature.recipes.RecipeCreateViewModel", ((Provider) recipeCreateViewModelProvider)).put("com.justcook.feature.recipes.RecipeDetailViewModel", ((Provider) recipeDetailViewModelProvider)).put("com.justcook.feature.search.SearchViewModel", ((Provider) searchViewModelProvider)).put("com.justcook.feature.profile.SettingsViewModel", ((Provider) settingsViewModelProvider)).build();
    }

    @Override
    public Map<String, Object> getHiltViewModelAssistedMap() {
      return Collections.<String, Object>emptyMap();
    }

    private static final class SwitchingProvider<T> implements Provider<T> {
      private final SingletonCImpl singletonCImpl;

      private final ActivityRetainedCImpl activityRetainedCImpl;

      private final ViewModelCImpl viewModelCImpl;

      private final int id;

      SwitchingProvider(SingletonCImpl singletonCImpl, ActivityRetainedCImpl activityRetainedCImpl,
          ViewModelCImpl viewModelCImpl, int id) {
        this.singletonCImpl = singletonCImpl;
        this.activityRetainedCImpl = activityRetainedCImpl;
        this.viewModelCImpl = viewModelCImpl;
        this.id = id;
      }

      @SuppressWarnings("unchecked")
      @Override
      public T get() {
        switch (id) {
          case 0: // com.justcook.feature.auth.AuthViewModel 
          return (T) new AuthViewModel(singletonCImpl.authRepositoryImplProvider.get());

          case 1: // com.justcook.feature.bookmarks.BookmarksViewModel 
          return (T) new BookmarksViewModel(singletonCImpl.bookmarkRepositoryImplProvider.get(), singletonCImpl.authRepositoryImplProvider.get());

          case 2: // com.justcook.feature.profile.ChefProfileViewModel 
          return (T) new ChefProfileViewModel(viewModelCImpl.savedStateHandle, singletonCImpl.userRepositoryImplProvider.get(), singletonCImpl.recipeRepositoryImplProvider.get());

          case 3: // com.justcook.feature.recipes.CommentsViewModel 
          return (T) new CommentsViewModel(viewModelCImpl.savedStateHandle, singletonCImpl.commentRepositoryImplProvider.get(), singletonCImpl.recipeRepositoryImplProvider.get(), singletonCImpl.authRepositoryImplProvider.get());

          case 4: // com.justcook.feature.profile.FollowingViewModel 
          return (T) new FollowingViewModel(singletonCImpl.userRepositoryImplProvider.get());

          case 5: // com.justcook.feature.home.HomeViewModel 
          return (T) new HomeViewModel(singletonCImpl.recipeRepositoryImplProvider.get());

          case 6: // com.justcook.feature.profile.ProfileViewModel 
          return (T) new ProfileViewModel(singletonCImpl.authRepositoryImplProvider.get(), singletonCImpl.recipeRepositoryImplProvider.get(), singletonCImpl.userRepositoryImplProvider.get());

          case 7: // com.justcook.feature.recipes.RecipeCreateViewModel 
          return (T) new RecipeCreateViewModel(singletonCImpl.recipeRepositoryImplProvider.get());

          case 8: // com.justcook.feature.recipes.RecipeDetailViewModel 
          return (T) new RecipeDetailViewModel(viewModelCImpl.savedStateHandle, singletonCImpl.recipeRepositoryImplProvider.get(), singletonCImpl.bookmarkRepositoryImplProvider.get(), singletonCImpl.authRepositoryImplProvider.get());

          case 9: // com.justcook.feature.search.SearchViewModel 
          return (T) new SearchViewModel(singletonCImpl.recipeRepositoryImplProvider.get());

          case 10: // com.justcook.feature.profile.SettingsViewModel 
          return (T) new SettingsViewModel(singletonCImpl.provideUserPreferencesDataStoreProvider.get(), singletonCImpl.authRepositoryImplProvider.get());

          default: throw new AssertionError(id);
        }
      }
    }
  }

  private static final class ActivityRetainedCImpl extends JustCookApplication_HiltComponents.ActivityRetainedC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl = this;

    private Provider<ActivityRetainedLifecycle> provideActivityRetainedLifecycleProvider;

    private ActivityRetainedCImpl(SingletonCImpl singletonCImpl,
        SavedStateHandleHolder savedStateHandleHolderParam) {
      this.singletonCImpl = singletonCImpl;

      initialize(savedStateHandleHolderParam);

    }

    @SuppressWarnings("unchecked")
    private void initialize(final SavedStateHandleHolder savedStateHandleHolderParam) {
      this.provideActivityRetainedLifecycleProvider = DoubleCheck.provider(new SwitchingProvider<ActivityRetainedLifecycle>(singletonCImpl, activityRetainedCImpl, 0));
    }

    @Override
    public ActivityComponentBuilder activityComponentBuilder() {
      return new ActivityCBuilder(singletonCImpl, activityRetainedCImpl);
    }

    @Override
    public ActivityRetainedLifecycle getActivityRetainedLifecycle() {
      return provideActivityRetainedLifecycleProvider.get();
    }

    private static final class SwitchingProvider<T> implements Provider<T> {
      private final SingletonCImpl singletonCImpl;

      private final ActivityRetainedCImpl activityRetainedCImpl;

      private final int id;

      SwitchingProvider(SingletonCImpl singletonCImpl, ActivityRetainedCImpl activityRetainedCImpl,
          int id) {
        this.singletonCImpl = singletonCImpl;
        this.activityRetainedCImpl = activityRetainedCImpl;
        this.id = id;
      }

      @SuppressWarnings("unchecked")
      @Override
      public T get() {
        switch (id) {
          case 0: // dagger.hilt.android.ActivityRetainedLifecycle 
          return (T) ActivityRetainedComponentManager_LifecycleModule_ProvideActivityRetainedLifecycleFactory.provideActivityRetainedLifecycle();

          default: throw new AssertionError(id);
        }
      }
    }
  }

  private static final class ServiceCImpl extends JustCookApplication_HiltComponents.ServiceC {
    private final SingletonCImpl singletonCImpl;

    private final ServiceCImpl serviceCImpl = this;

    private ServiceCImpl(SingletonCImpl singletonCImpl, Service serviceParam) {
      this.singletonCImpl = singletonCImpl;


    }
  }

  private static final class SingletonCImpl extends JustCookApplication_HiltComponents.SingletonC {
    private final ApplicationContextModule applicationContextModule;

    private final SingletonCImpl singletonCImpl = this;

    private Provider<UserPreferencesDataStore> provideUserPreferencesDataStoreProvider;

    private Provider<AuthRepositoryImpl> authRepositoryImplProvider;

    private Provider<AuthInterceptor> authInterceptorProvider;

    private Provider<HttpLoggingInterceptor> provideLoggingInterceptorProvider;

    private Provider<OkHttpClient> provideOkHttpClientProvider;

    private Provider<Json> provideJsonProvider;

    private Provider<Retrofit> provideRetrofitProvider;

    private Provider<AuthApiService> provideAuthApiServiceProvider;

    private Provider<CoroutineDispatcher> provideIoDispatcherProvider;

    private Provider<BookmarkApiService> provideBookmarkApiServiceProvider;

    private Provider<JustCookDatabase> provideDatabaseProvider;

    private Provider<BookmarkRepositoryImpl> bookmarkRepositoryImplProvider;

    private Provider<RecipeApiService> provideRecipeApiServiceProvider;

    private Provider<UserRepositoryImpl> userRepositoryImplProvider;

    private Provider<RecipeRepositoryImpl> recipeRepositoryImplProvider;

    private Provider<CommentRepositoryImpl> commentRepositoryImplProvider;

    private SingletonCImpl(ApplicationContextModule applicationContextModuleParam) {
      this.applicationContextModule = applicationContextModuleParam;
      initialize(applicationContextModuleParam);

    }

    private BookmarkDao bookmarkDao() {
      return DatabaseModule_ProvideBookmarkDaoFactory.provideBookmarkDao(provideDatabaseProvider.get());
    }

    private RecipeDao recipeDao() {
      return DatabaseModule_ProvideRecipeDaoFactory.provideRecipeDao(provideDatabaseProvider.get());
    }

    @SuppressWarnings("unchecked")
    private void initialize(final ApplicationContextModule applicationContextModuleParam) {
      this.provideUserPreferencesDataStoreProvider = DoubleCheck.provider(new SwitchingProvider<UserPreferencesDataStore>(singletonCImpl, 0));
      this.authRepositoryImplProvider = new DelegateFactory<>();
      this.authInterceptorProvider = DoubleCheck.provider(new SwitchingProvider<AuthInterceptor>(singletonCImpl, 5));
      this.provideLoggingInterceptorProvider = DoubleCheck.provider(new SwitchingProvider<HttpLoggingInterceptor>(singletonCImpl, 6));
      this.provideOkHttpClientProvider = DoubleCheck.provider(new SwitchingProvider<OkHttpClient>(singletonCImpl, 4));
      this.provideJsonProvider = DoubleCheck.provider(new SwitchingProvider<Json>(singletonCImpl, 7));
      this.provideRetrofitProvider = DoubleCheck.provider(new SwitchingProvider<Retrofit>(singletonCImpl, 3));
      this.provideAuthApiServiceProvider = DoubleCheck.provider(new SwitchingProvider<AuthApiService>(singletonCImpl, 2));
      this.provideIoDispatcherProvider = DoubleCheck.provider(new SwitchingProvider<CoroutineDispatcher>(singletonCImpl, 8));
      DelegateFactory.setDelegate(authRepositoryImplProvider, DoubleCheck.provider(new SwitchingProvider<AuthRepositoryImpl>(singletonCImpl, 1)));
      this.provideBookmarkApiServiceProvider = DoubleCheck.provider(new SwitchingProvider<BookmarkApiService>(singletonCImpl, 10));
      this.provideDatabaseProvider = DoubleCheck.provider(new SwitchingProvider<JustCookDatabase>(singletonCImpl, 11));
      this.bookmarkRepositoryImplProvider = DoubleCheck.provider(new SwitchingProvider<BookmarkRepositoryImpl>(singletonCImpl, 9));
      this.provideRecipeApiServiceProvider = DoubleCheck.provider(new SwitchingProvider<RecipeApiService>(singletonCImpl, 13));
      this.userRepositoryImplProvider = DoubleCheck.provider(new SwitchingProvider<UserRepositoryImpl>(singletonCImpl, 12));
      this.recipeRepositoryImplProvider = DoubleCheck.provider(new SwitchingProvider<RecipeRepositoryImpl>(singletonCImpl, 14));
      this.commentRepositoryImplProvider = DoubleCheck.provider(new SwitchingProvider<CommentRepositoryImpl>(singletonCImpl, 15));
    }

    @Override
    public void injectJustCookApplication(JustCookApplication justCookApplication) {
    }

    @Override
    public Set<Boolean> getDisableFragmentGetContextFix() {
      return Collections.<Boolean>emptySet();
    }

    @Override
    public ActivityRetainedComponentBuilder retainedComponentBuilder() {
      return new ActivityRetainedCBuilder(singletonCImpl);
    }

    @Override
    public ServiceComponentBuilder serviceComponentBuilder() {
      return new ServiceCBuilder(singletonCImpl);
    }

    private static final class SwitchingProvider<T> implements Provider<T> {
      private final SingletonCImpl singletonCImpl;

      private final int id;

      SwitchingProvider(SingletonCImpl singletonCImpl, int id) {
        this.singletonCImpl = singletonCImpl;
        this.id = id;
      }

      @SuppressWarnings("unchecked")
      @Override
      public T get() {
        switch (id) {
          case 0: // com.justcook.core.datastore.UserPreferencesDataStore 
          return (T) DataStoreModule_ProvideUserPreferencesDataStoreFactory.provideUserPreferencesDataStore(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule));

          case 1: // com.justcook.data.repository.AuthRepositoryImpl 
          return (T) new AuthRepositoryImpl(singletonCImpl.provideAuthApiServiceProvider.get(), singletonCImpl.provideUserPreferencesDataStoreProvider.get(), singletonCImpl.provideIoDispatcherProvider.get());

          case 2: // com.justcook.data.remote.api.AuthApiService 
          return (T) DataModule_ProvideAuthApiServiceFactory.provideAuthApiService(singletonCImpl.provideRetrofitProvider.get());

          case 3: // retrofit2.Retrofit 
          return (T) NetworkModule_ProvideRetrofitFactory.provideRetrofit(singletonCImpl.provideOkHttpClientProvider.get(), singletonCImpl.provideJsonProvider.get());

          case 4: // okhttp3.OkHttpClient 
          return (T) NetworkModule_ProvideOkHttpClientFactory.provideOkHttpClient(singletonCImpl.authInterceptorProvider.get(), singletonCImpl.provideLoggingInterceptorProvider.get());

          case 5: // com.justcook.core.network.interceptor.AuthInterceptor 
          return (T) new AuthInterceptor(DoubleCheck.lazy(((Provider) singletonCImpl.authRepositoryImplProvider)));

          case 6: // okhttp3.logging.HttpLoggingInterceptor 
          return (T) NetworkModule_ProvideLoggingInterceptorFactory.provideLoggingInterceptor();

          case 7: // kotlinx.serialization.json.Json 
          return (T) NetworkModule_ProvideJsonFactory.provideJson();

          case 8: // @com.justcook.core.common.di.IoDispatcher kotlinx.coroutines.CoroutineDispatcher 
          return (T) DispatchersModule_ProvideIoDispatcherFactory.provideIoDispatcher();

          case 9: // com.justcook.data.repository.BookmarkRepositoryImpl 
          return (T) new BookmarkRepositoryImpl(singletonCImpl.provideBookmarkApiServiceProvider.get(), singletonCImpl.bookmarkDao(), singletonCImpl.recipeDao(), singletonCImpl.provideIoDispatcherProvider.get());

          case 10: // com.justcook.data.remote.api.BookmarkApiService 
          return (T) DataModule_ProvideBookmarkApiServiceFactory.provideBookmarkApiService(singletonCImpl.provideRetrofitProvider.get());

          case 11: // com.justcook.core.database.JustCookDatabase 
          return (T) DatabaseModule_ProvideDatabaseFactory.provideDatabase(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule));

          case 12: // com.justcook.data.repository.UserRepositoryImpl 
          return (T) new UserRepositoryImpl(singletonCImpl.provideRecipeApiServiceProvider.get(), singletonCImpl.provideIoDispatcherProvider.get());

          case 13: // com.justcook.data.remote.api.RecipeApiService 
          return (T) DataModule_ProvideRecipeApiServiceFactory.provideRecipeApiService(singletonCImpl.provideRetrofitProvider.get());

          case 14: // com.justcook.data.repository.RecipeRepositoryImpl 
          return (T) new RecipeRepositoryImpl(singletonCImpl.provideRecipeApiServiceProvider.get(), singletonCImpl.recipeDao(), singletonCImpl.provideIoDispatcherProvider.get());

          case 15: // com.justcook.data.repository.CommentRepositoryImpl 
          return (T) new CommentRepositoryImpl(singletonCImpl.provideRecipeApiServiceProvider.get(), singletonCImpl.provideIoDispatcherProvider.get());

          default: throw new AssertionError(id);
        }
      }
    }
  }
}
