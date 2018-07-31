package software.uniqore.codesample

import android.arch.lifecycle.ViewModelProvider
import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.action.ViewActions.swipeDown
import android.support.test.espresso.assertion.ViewAssertions.matches
import android.support.test.espresso.matcher.ViewMatchers.*
import android.support.test.filters.LargeTest
import android.support.test.rule.ActivityTestRule
import android.support.test.runner.AndroidJUnit4
import com.nhaarman.mockitokotlin2.*
import io.reactivex.Observable
import io.reactivex.Single
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.threeten.bp.LocalDateTime
import software.uniqore.codesample.model.Photo
import software.uniqore.codesample.repository.PhotoRepository
import software.uniqore.codesample.support.ActivityInjectionRule


@RunWith(AndroidJUnit4::class)
@LargeTest
class PhotosActivityTest {


    @Rule
    @JvmField
    val activityRule = ActivityTestRule(PhotosActivity::class.java, false, false)


    @Rule
    @JvmField
    val injectionRule = ActivityInjectionRule(PhotosActivity::class.java) { it.viewModelFactory = factory }

    lateinit var factory: ViewModelProvider.Factory
    lateinit var viewModel: PhotoViewModel
    lateinit var repository: PhotoRepository


    private fun cachedPhotoList() = listOf(Photo("cached", "authorCached", LocalDateTime.now()))
    private fun remotePhotoList() = listOf(Photo("remote", "author", LocalDateTime.now()))


    @Before
    fun setup() {

        factory = mock<ViewModelProvider.Factory> {
            on { create(PhotoViewModel::class.java) } doAnswer { viewModel }
        }

        repository = mock {
            on {
                getPhotos()
            } doReturn Observable.just(cachedPhotoList())
            on {
                update()
            } doReturn Single.just(remotePhotoList())
        }

        viewModel = spy(PhotoViewModel(repository))

        activityRule.launchActivity(null)

    }

    @Test
    fun testText() {
        onView(withId(R.id.my_recycler_view)).check(matches(isDisplayed()))

        onView(withText(cachedPhotoList()[0].author)).check(matches(isDisplayed()))

        verify(viewModel).loadPhotos()

        onView(withId(R.id.swiperefresh)).perform(swipeDown())

        verify(viewModel).refreshPhotos()

        onView(withText(remotePhotoList()[0].author)).check(matches(isDisplayed()))

    }


}