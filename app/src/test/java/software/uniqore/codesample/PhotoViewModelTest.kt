package software.uniqore.codesample

import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import io.reactivex.Observable
import io.reactivex.Single
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import software.uniqore.codesample.repository.PhotoRepository
import software.uniqore.codesample.support.PhotoLists
import software.uniqore.codesample.support.TrampolineSchedulerRule


class PhotoViewModelTest {

    @Rule
    @JvmField
    val rule = TrampolineSchedulerRule()

    @Test
    fun doTest() {

        val cachedPhotoList = PhotoLists.cached()
        val remotePhotoList = PhotoLists.remote()

        val repository = mock<PhotoRepository> {
            on {
                getPhotos()
            } doReturn Observable.just(cachedPhotoList)
            on {
                update()
            } doReturn Single.just(remotePhotoList)
        }

        val viewModel = PhotoViewModel(repository)

        viewModel.loadPhotos()

        verify(repository).getPhotos()
        assertEquals(cachedPhotoList, viewModel.photos.value)

        viewModel.refreshPhotos()
        verify(repository).update()
        assertEquals(remotePhotoList, viewModel.photos.value)

    }
}