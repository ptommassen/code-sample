package software.uniqore.codesample

import android.arch.core.executor.ArchTaskExecutor
import android.arch.core.executor.TaskExecutor
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.android.plugins.RxAndroidPlugins
import io.reactivex.plugins.RxJavaPlugins
import io.reactivex.schedulers.Schedulers
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement
import org.threeten.bp.LocalDateTime
import software.uniqore.codesample.model.Photo
import software.uniqore.codesample.repository.PhotoRepository


class TrampolineSchedulerRule : TestRule {

    override fun apply(base: Statement, d: Description): Statement {
        return object : Statement() {
            @Throws(Throwable::class)
            override fun evaluate() {
                RxJavaPlugins.setIoSchedulerHandler { scheduler -> Schedulers.trampoline() }
                RxJavaPlugins.setComputationSchedulerHandler { scheduler -> Schedulers.trampoline() }
                RxJavaPlugins.setNewThreadSchedulerHandler { scheduler -> Schedulers.trampoline() }
                RxAndroidPlugins.setInitMainThreadSchedulerHandler { scheduler -> Schedulers.trampoline() }
                ArchTaskExecutor.getInstance().setDelegate(object : TaskExecutor() {
                    override fun executeOnDiskIO(runnable: Runnable) {
                        runnable.run()
                    }

                    override fun isMainThread(): Boolean = true

                    override fun postToMainThread(runnable: Runnable) {
                        runnable.run()
                    }

                })

                try {
                    base.evaluate()
                } finally {
                    RxJavaPlugins.reset()
                    RxAndroidPlugins.reset()
                }
            }
        }
    }
}

class PhotoViewModelTest {
    private fun cachedPhotoList() = listOf(Photo("cached", "author", LocalDateTime.now()))
    private fun remotePhotoList() = listOf(Photo("remote", "author", LocalDateTime.now()))

    @Rule
    @JvmField
    val rule = TrampolineSchedulerRule()

    @Test
    fun doTest() {

        val cachedPhotoList = cachedPhotoList()
        val remotePhotoList = remotePhotoList()

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