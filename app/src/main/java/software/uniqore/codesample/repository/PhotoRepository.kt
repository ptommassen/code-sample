package software.uniqore.codesample.repository

import dagger.Module
import dagger.Provides
import io.reactivex.Observable
import io.reactivex.Single
import software.uniqore.codesample.cache.Cache
import software.uniqore.codesample.model.Photo
import software.uniqore.codesample.remote.RemotePhotoRetriever
import javax.inject.Inject


interface PhotoRepository {
    fun getPhotos(): Observable<List<Photo>>

    fun update(): Single<List<Photo>>
}


class DefaultPhotoRepository @Inject constructor(private val remotePhotoRetriever: RemotePhotoRetriever, private val cache: Cache) : PhotoRepository {

    @Module
    class DaggerModule {
        @Provides
        fun providePhotoRepository(remotePhotoRetriever: RemotePhotoRetriever, cache: Cache): PhotoRepository = DefaultPhotoRepository(remotePhotoRetriever, cache)
    }


    override fun getPhotos(): Observable<List<Photo>> {
        val single = cache.retrieveCache().flatMapObservable {
            when (it.validity) {
                Cache.CachedPhotos.Validity.INVALID -> retrieveRemotePhotos().toObservable()
                Cache.CachedPhotos.Validity.VALID -> Observable.just(it.photos)
                Cache.CachedPhotos.Validity.OUTDATED -> Single.just(it.photos).mergeWith(retrieveRemotePhotos()).toObservable()
            }
        }
        return single
    }

    private fun retrieveRemotePhotos(): Single<List<Photo>> {
        val result = remotePhotoRetriever.retrievePhotos();
        result.subscribe { list ->
            cache.storeInCache(list)
        }
        return result;
    }

    override fun update(): Single<List<Photo>> = retrieveRemotePhotos()

}