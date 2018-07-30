package software.uniqore.codesample

import io.reactivex.Observable
import software.uniqore.codesample.model.Photo

interface PhotoProvider {
    fun getPhotos(): Observable<List<Photo>>
}
