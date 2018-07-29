package software.uniqore.codesample

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.databinding.BaseObservable
import android.databinding.ObservableField
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.observers.DisposableObserver
import io.reactivex.schedulers.Schedulers
import org.threeten.bp.LocalDateTime
import javax.inject.Inject


data class Photo(val url: String, val author: String, val date: LocalDateTime) : BaseObservable() {
}


class PhotoViewModel @Inject constructor(private var model: PhotoProvider) : ViewModel() {

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