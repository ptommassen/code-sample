package software.uniqore.codesample.remote

import dagger.Module
import dagger.Provides
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import org.threeten.bp.ZonedDateTime
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import software.uniqore.codesample.PhotoProvider
import software.uniqore.codesample.model.Photo


data class FlickrItem(val title: String, val media: Map<String, String>, val date_taken: String, val author: String)

data class FlickrFeed(val items: Array<FlickrItem>)

interface FlickrService {
    @GET("/services/feeds/photos_public.gne?format=json&nojsoncallback=1")
    fun getFeed(): Single<FlickrFeed>
}

class FlickrPhotoProvider : PhotoProvider {


    @Module
    class DaggerModule {
        @Provides
        open fun providePhotoProvider(): PhotoProvider = FlickrPhotoProvider()
    }


    val retrofit = Retrofit.Builder()
            .baseUrl("https://api.flickr.com/")
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .addConverterFactory(
                    GsonConverterFactory.create())
            .build();

    val flickrService = retrofit.create(FlickrService::class.java)

    override fun getPhotos(): Observable<List<Photo>> {

        return flickrService.getFeed()
                .subscribeOn(Schedulers.io())
                .map { feed -> feed.items.map { item -> Photo(item.media["m"]!!, item.author, ZonedDateTime.parse(item.date_taken).toLocalDateTime()) } }
                .toObservable()

    }

}