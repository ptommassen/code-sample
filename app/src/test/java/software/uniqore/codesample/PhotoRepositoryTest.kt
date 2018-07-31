package software.uniqore.codesample

import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyZeroInteractions
import io.reactivex.Single
import io.reactivex.observers.TestObserver
import org.junit.Test
import org.threeten.bp.LocalDateTime
import software.uniqore.codesample.cache.Cache
import software.uniqore.codesample.model.Photo
import software.uniqore.codesample.remote.RemotePhotoRetriever
import software.uniqore.codesample.repository.DefaultPhotoRepository

class PhotoRepositoryUnitTest {
    private fun cachedPhotoList() = listOf(Photo("cached", "author", LocalDateTime.now()))
    private fun remotePhotoList() = listOf(Photo("remote", "author", LocalDateTime.now()))


    @Test
    fun testWithValidCache() {
        val photoList = cachedPhotoList()

        val mockCache = mock<Cache> {
            on { retrieveCache() } doReturn Single.just(Cache.CachedPhotos(Cache.CachedPhotos.Validity.VALID, photoList))
        }
        val mockRemote = mock<RemotePhotoRetriever>()

        val photoRepository = DefaultPhotoRepository(mockRemote, mockCache)

        val testObserver = TestObserver<List<Photo>>()
        photoRepository.getPhotos().subscribe(testObserver)
        testObserver.awaitCount(1)
        testObserver.assertValueAt(0, photoList)
        verifyZeroInteractions(mockRemote)
    }

    @Test
    fun testWithInvalidCache() {
        val remotePhotoList = remotePhotoList()

        val mockCache = mock<Cache> {
            on { retrieveCache() } doReturn Single.just(Cache.CachedPhotos(Cache.CachedPhotos.Validity.INVALID, listOf()))
        }
        val mockRemote = mock<RemotePhotoRetriever> {
            on { retrievePhotos() } doReturn Single.just(remotePhotoList)
        }

        val photoRepository = DefaultPhotoRepository(mockRemote, mockCache)

        val testObserver = TestObserver<List<Photo>>()
        photoRepository.getPhotos().subscribe(testObserver)
        testObserver.awaitCount(1)
        testObserver.assertValueAt(0, remotePhotoList)
        verify(mockCache).storeInCache(remotePhotoList)
    }


    @Test
    fun testWithOutdatedCache() {
        val remotePhotoList = remotePhotoList()
        val cachedPhotoList = cachedPhotoList()

        val mockCache = mock<Cache> {
            on { retrieveCache() } doReturn Single.just(Cache.CachedPhotos(Cache.CachedPhotos.Validity.OUTDATED, cachedPhotoList))
        }
        val mockRemote = mock<RemotePhotoRetriever> {
            on { retrievePhotos() } doReturn Single.just(remotePhotoList)
        }

        val photoRepository = DefaultPhotoRepository(mockRemote, mockCache)

        val testObserver = TestObserver<List<Photo>>()
        photoRepository.getPhotos().subscribe(testObserver)


        testObserver.awaitCount(2)
        testObserver.assertValueAt(0, cachedPhotoList)
        testObserver.assertValueAt(1, remotePhotoList)
        verify(mockCache).storeInCache(remotePhotoList)
    }


    @Test
    fun testUpdate() {
        val remotePhotoList = remotePhotoList()
        val cachedPhotoList = cachedPhotoList()

        val mockCache = mock<Cache> {
            on { retrieveCache() } doReturn Single.just(Cache.CachedPhotos(Cache.CachedPhotos.Validity.VALID, cachedPhotoList))
        }
        val mockRemote = mock<RemotePhotoRetriever> {
            on { retrievePhotos() } doReturn Single.just(remotePhotoList)
        }

        val photoRepository = DefaultPhotoRepository(mockRemote, mockCache)

        val testObserver = TestObserver<List<Photo>>()
        photoRepository.getPhotos().subscribe(testObserver)

        testObserver.await()
        testObserver.assertValue(cachedPhotoList)
        verifyZeroInteractions(mockRemote)

        photoRepository.update().subscribe(testObserver)

        testObserver.awaitCount(2)
        testObserver.assertValueAt(0, cachedPhotoList)
        testObserver.assertValueAt(1, remotePhotoList)

        verify(mockRemote).retrievePhotos()
        verify(mockCache).storeInCache(remotePhotoList)

    }
}