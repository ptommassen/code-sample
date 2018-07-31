package software.uniqore.codesample.remote

import dagger.Module
import dagger.Provides
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import org.threeten.bp.ZonedDateTime
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import software.uniqore.codesample.model.Photo
import java.util.concurrent.TimeUnit

interface RemotePhotoRetriever {
    fun retrievePhotos(): Single<List<Photo>>
}

data class FlickrItem(val title: String, val media: Map<String, String>, val date_taken: String, val author: String)

data class FlickrFeed(val items: Array<FlickrItem>)

interface FlickrService {
    @GET("/services/feeds/photos_public.gne?format=json&nojsoncallback=1&tags=landscape")
    fun getFeed(): Single<FlickrFeed>
}

class FlickrPhotoProvider : RemotePhotoRetriever {

    @Module
    class DaggerModule {
        @Provides
        fun providePhotoProvider(): RemotePhotoRetriever = FlickrPhotoProvider()
    }


    val retrofit = Retrofit.Builder()
            .baseUrl("https://api.flickr.com/")
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .addConverterFactory(
                    GsonConverterFactory.create())
            .build();

    private val flickrService = retrofit.create(FlickrService::class.java)!!


    override fun retrievePhotos(): Single<List<Photo>> {

        return flickrService.getFeed()
                .subscribeOn(Schedulers.io())
                .map { feed -> feed.items.map { item -> Photo(item.media["m"]!!, item.author, ZonedDateTime.parse(item.date_taken).toLocalDateTime()) } }

    }

}