package software.uniqore.codesample

import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentActivity
import android.support.v4.app.FragmentManager
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides
import dagger.android.*
import dagger.android.support.AndroidSupportInjection
import dagger.android.support.AndroidSupportInjectionModule
import software.uniqore.codesample.cache.DefaultCache
import software.uniqore.codesample.cache.FileCacheStorage
import software.uniqore.codesample.remote.FlickrPhotoProvider
import software.uniqore.codesample.repository.DefaultPhotoRepository
import software.uniqore.codesample.support.ViewModelModule
import javax.inject.Singleton


class CodeSampleApplication : DaggerApplication() {

    override fun applicationInjector(): AndroidInjector<out DaggerApplication> {
        return DaggerApplicationComponent.builder().application(this).build()
    }

    override fun onCreate() {
        super.onCreate()
        Injector.init(this)
    }
}

@Singleton
@Component(modules = [AndroidInjectionModule::class, ApplicationModule::class, ActivityBuilder::class])
interface ApplicationComponent : AndroidInjector<CodeSampleApplication> {

    @Component.Builder
    interface Builder {
        @BindsInstance
        fun application(application: Application): Builder

        fun build(): ApplicationComponent
    }
}

@Module(includes = [AndroidSupportInjectionModule::class, FlickrPhotoProvider.DaggerModule::class, ViewModelModule::class, DefaultPhotoRepository.DaggerModule::class, FileCacheStorage.DaggerModule::class, DefaultCache.DaggerModule::class])
class ApplicationModule {

    @Provides
    @Singleton
    fun provideContext(application: Application): Context = application
}


@Module
abstract class ActivityBuilder {

    @ContributesAndroidInjector
    abstract fun bindMainActivity(): PhotosActivity

}

interface Injectable

object Injector {

    fun init(application: Application) {
        application.registerActivityLifecycleCallbacks(object : Application.ActivityLifecycleCallbacks {
            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
                if (activity is Injectable) {
                    AndroidInjection.inject(activity)
                }
                if (activity is FragmentActivity) {
                    activity.supportFragmentManager.registerFragmentLifecycleCallbacks(
                            object : FragmentManager.FragmentLifecycleCallbacks() {
                                override fun onFragmentPreAttached(fm: FragmentManager, f: Fragment, context: Context) {
                                    if (f is Injectable) {
                                        AndroidSupportInjection.inject(f)
                                    }
                                }
                            }, true)
                }
            }

            override fun onActivityStarted(activity: Activity) {
            }

            override fun onActivityResumed(activity: Activity) {
            }

            override fun onActivityPaused(activity: Activity) {
            }

            override fun onActivityStopped(activity: Activity) {
            }

            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
            }

            override fun onActivityDestroyed(activity: Activity) {
            }
        })
    }
}