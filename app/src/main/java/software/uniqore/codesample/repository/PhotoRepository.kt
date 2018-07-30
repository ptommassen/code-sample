package software.uniqore.codesample.repository

import dagger.Module
import dagger.Provides
import io.reactivex.Observable
import software.uniqore.codesample.cache.Cache
import software.uniqore.codesample.model.Photo
import software.uniqore.codesample.remote.RemotePhotoRetriever
import javax.inject.Inject


interface PhotoRepository {
    fun getPhotos(): Observable<List<Photo>>

    fun update(): Unit
}


class DefaultPhotoRepository @Inject constructor(private val remotePhotoRetriever: RemotePhotoRetriever, private val cache: Cache) : PhotoRepository {

    @Module
    class DaggerModule {
        @Provides
        fun providePhotoRepository(remotePhotoRetriever: RemotePhotoRetriever, cache: Cache): PhotoRepository = DefaultPhotoRepository(remotePhotoRetriever, cache)
    }


    override fun getPhotos(): Observable<List<Photo>> {
        return remotePhotoRetriever.retrievePhotos().toObservable()
    }

    override fun update() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}