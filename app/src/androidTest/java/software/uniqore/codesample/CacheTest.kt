package software.uniqore.codesample


import android.support.test.InstrumentationRegistry
import android.support.test.runner.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.threeten.bp.LocalDateTime
import software.uniqore.codesample.cache.CacheStorage
import software.uniqore.codesample.cache.FileCacheStorage
import software.uniqore.codesample.model.Photo

@RunWith(AndroidJUnit4::class)
class FileCacheStorageTest {

    @Test
    fun readWriteFileCacheStorageTest() {
        val appContext = InstrumentationRegistry.getTargetContext()
        val storage = FileCacheStorage(appContext)

        val writeData = CacheStorage.CachedData(LocalDateTime.now(), listOf(Photo("url", "author", LocalDateTime.now())))

        storage.store(writeData)

        val readData = storage.load()
        assertEquals(readData, writeData)
    }


    @Test
    fun emptyFileCacheStorageTest() {
        val appContext = InstrumentationRegistry.getTargetContext()
        val storage = FileCacheStorage(appContext)
        storage.getCacheFile().delete()
        val readData = storage.load()
        assertNull(readData)
    }

}
