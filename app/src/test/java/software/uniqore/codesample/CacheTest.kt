package software.uniqore.codesample

import com.nhaarman.mockitokotlin2.argWhere
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import io.reactivex.observers.TestObserver
import org.junit.Test
import org.threeten.bp.LocalDateTime
import software.uniqore.codesample.cache.Cache
import software.uniqore.codesample.cache.CacheStorage
import software.uniqore.codesample.cache.DefaultCache
import software.uniqore.codesample.model.Photo

class CacheUnitTest {

    fun loadTest(testData: CacheStorage.CachedData?, asserts: (TestObserver<Cache.CachedPhotos>) -> Unit) {
        val mockCacheStorage = mock<CacheStorage> {
            on { load() } doReturn testData
        }

        val defaultCache = DefaultCache(mockCacheStorage)

        val cached = defaultCache.retrieveCache().toObservable()
        val subscriber = TestObserver<Cache.CachedPhotos>()
        cached.subscribe(subscriber)
        subscriber.await()

        asserts(subscriber)
    }

    @Test
    fun emptyCacheTest() {
        loadTest(null) {
            it.assertValue { it.validity == Cache.CachedPhotos.Validity.INVALID }
        }
    }

    @Test
    fun validCacheTest() {
        loadTest(CacheStorage.CachedData(LocalDateTime.now(), photoList()))
        {
            it.assertValue { it.validity == Cache.CachedPhotos.Validity.VALID && it.photos.size == 1 }
        }
    }


    @Test
    fun outdatedCacheTest() {
        loadTest(CacheStorage.CachedData(LocalDateTime.now().minusHours(1), photoList()))
        {
            it.assertValue { it.validity == Cache.CachedPhotos.Validity.OUTDATED && it.photos.size == 1 }
        }
    }

    private fun photoList() = listOf(Photo("url", "author", LocalDateTime.now()))


    @Test
    fun writeToCache() {
        val mockCacheStorage = mock<CacheStorage>()

        val defaultCache = DefaultCache(mockCacheStorage)
        val list = photoList()

        defaultCache.storeInCache(list)

        verify(mockCacheStorage).store(argWhere {
            it.retrieved.isBefore(LocalDateTime.now().plusMinutes(1)) &&
                    it.retrieved.isAfter(LocalDateTime.now().minusMinutes(1)) &&
                    it.photos == list
        })

    }
}