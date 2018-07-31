package software.uniqore.codesample

import android.app.Application
import android.content.Context
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides
import dagger.android.AndroidInjectionModule
import dagger.android.AndroidInjector
import dagger.android.ContributesAndroidInjector
import dagger.android.DaggerApplication
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
    abstract fun bindPhotosActivity(): PhotosActivity


}