package software.uniqore.codesample

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.databinding.ObservableBoolean
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.observers.DisposableObserver
import io.reactivex.schedulers.Schedulers
import software.uniqore.codesample.model.Photo
import software.uniqore.codesample.repository.PhotoRepository
import javax.inject.Inject


class PhotoViewModel @Inject constructor(private val repository: PhotoRepository) : ViewModel() {

    val loading = ObservableBoolean(true)
    var photos = MutableLiveData<List<Photo>>()
    private val disposables = CompositeDisposable()


    fun loadPhotos() {
        loading.set(true)
        disposables.add(repository.getPhotos()
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
                        throw e
                    }

                }))
    }

    fun refreshPhotos() {
        loading.set(true)
        disposables.add(repository.update().observeOn(AndroidSchedulers.mainThread()).subscribe { list ->
            loading.set(false)
            photos.value = list
        })
    }

    override fun onCleared() {
        super.onCleared()
        if (!disposables.isDisposed)
            disposables.dispose()
    }

}