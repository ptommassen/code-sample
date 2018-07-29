package software.uniqore.codesample

import dagger.Module
import dagger.Provides
import io.reactivex.Observable
import org.threeten.bp.LocalDateTime
import software.uniqore.codesample.model.Photo
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

interface PhotoProvider {
    fun getPhotos(): Observable<List<Photo>>
}

@Singleton
class DummyPhotoProvider : PhotoProvider {

    override fun getPhotos(): Observable<List<Photo>> {
        val photos = ArrayList<Photo>()
        photos.add(Photo("https://farm1.staticflickr.com/850/28804302867_f59cb8a86d_b.jpg", "steve_clayworth", LocalDateTime.parse("2018-07-28T16:23:35")))
        photos.add(Photo("https://farm1.staticflickr.com/860/28804304497_3461b9de06_b.jpg", "@sebastian1906", LocalDateTime.parse("2018-07-28T16:23:39")))
        return Observable.just(photos as List<Photo>).delay(2, TimeUnit.SECONDS)
    }


    @Module
    class DaggerModule {
        @Provides
        open fun providePhotoProvider(): PhotoProvider = DummyPhotoProvider()
    }
}
