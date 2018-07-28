package software.uniqore.codesample

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.databinding.BaseObservable
import android.databinding.ObservableField
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.observers.DisposableObserver
import io.reactivex.schedulers.Schedulers
import org.threeten.bp.LocalDateTime
import java.util.*
import java.util.concurrent.TimeUnit

data class Photo(val url: String, val author: String, val date: LocalDateTime) : BaseObservable() {
}

class PhotoModel {

    fun getPhotos(): Observable<ArrayList<Photo>> {
        val photos = ArrayList<Photo>()
        photos.add(Photo("https://farm1.staticflickr.com/850/28804302867_f59cb8a86d_b.jpg", "steve_clayworth", LocalDateTime.parse("2018-07-28T16:23:35")))
        photos.add(Photo("https://farm1.staticflickr.com/860/28804304497_3461b9de06_b.jpg", "@sebastian1906", LocalDateTime.parse("2018-07-28T16:23:39")))
        return Observable.just(photos).delay(2, TimeUnit.SECONDS)
    }
}

class PhotoViewModel : ViewModel() {

    var model: PhotoModel = PhotoModel()

    val loading = ObservableField(true)
    var photos = MutableLiveData<List<Photo>>()
    private val disposables = CompositeDisposable()

    fun loadPhotos() {
        loading.set(true)
        disposables.add(model.getPhotos()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object : DisposableObserver<List<Photo>>() {
                    override fun onComplete() {
                        loading.set(false)
                    }

                    override fun onNext(data: List<Photo>) {
                        photos.value = data
                    }

                    override fun onError(e: Throwable) {
                        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                    }

                }))
    }

    override fun onCleared() {
        super.onCleared()
        if (!disposables.isDisposed)
            disposables.dispose()
    }

}