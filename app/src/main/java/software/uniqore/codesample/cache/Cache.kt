package software.uniqore.codesample.cache

import android.content.Context
import com.google.gson.Gson
import dagger.Module
import dagger.Provides
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import org.threeten.bp.LocalDateTime
import software.uniqore.codesample.model.Photo
import java.io.File
import javax.inject.Inject


interface Cache {
    data class CachedPhotos(val validity: Validity, val photos: List<Photo> = listOf()) {
        enum class Validity {
            INVALID,
            OUTDATED,
            VALID
        }
    }

    fun storeInCache(photos: List<Photo>)
    fun retrieveCache(): Single<CachedPhotos>
}


interface CacheStorage {

    data class CachedData(val retrieved: LocalDateTime, val photos: List<Photo>)

    fun store(data: CachedData)
    fun load(): CachedData?
}

class FileCacheStorage @Inject constructor(private val context: Context) : CacheStorage {

    @Module
    class DaggerModule {
        @Provides
        fun provideFileCacheStorage(context: Context): CacheStorage = FileCacheStorage(context)
    }


    var gson = Gson()

    // internal due to testing
    internal fun getCacheFile(): File = File(context.getCacheDir(), "photo-list-cache")

    override fun store(data: CacheStorage.CachedData) {

        getCacheFile().bufferedWriter().use {
            gson.toJson(data, it)
        }
    }

    override fun load(): CacheStorage.CachedData? {
        val file = getCacheFile()
        if (!file.exists()) return null;

        getCacheFile().bufferedReader().use {
            return gson.fromJson(it, CacheStorage.CachedData::class.java)
        }
    }

}

class DefaultCache @Inject constructor(private val dataStorage: CacheStorage) : Cache {

    @Module
    class DaggerModule {
        @Provides
        fun provideDefaultCache(storage: CacheStorage): Cache = DefaultCache(storage)
    }


    override fun storeInCache(photos: List<Photo>) {
        dataStorage.store(CacheStorage.CachedData(LocalDateTime.now(), photos))
    }

    override fun retrieveCache(): Single<Cache.CachedPhotos> {

        return Single.fromCallable {
            val cacheData = dataStorage.load();
            if (cacheData != null)
                Cache.CachedPhotos(if (isOutdated(cacheData.retrieved)) Cache.CachedPhotos.Validity.OUTDATED else Cache.CachedPhotos.Validity.VALID, cacheData.photos)
            else
                Cache.CachedPhotos(Cache.CachedPhotos.Validity.INVALID)
        }.subscribeOn(Schedulers.io())
    }

    fun isOutdated(storedTime: LocalDateTime) = storedTime.plusMinutes(15).isBefore(LocalDateTime.now())
}