package software.uniqore.codesample

import android.app.Activity
import android.app.Application
import android.arch.lifecycle.ViewModelProvider
import android.os.Bundle
import android.support.test.InstrumentationRegistry
import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.assertion.ViewAssertions.matches
import android.support.test.espresso.matcher.ViewMatchers.*
import android.support.test.filters.LargeTest
import android.support.test.rule.ActivityTestRule
import android.support.test.runner.AndroidJUnit4
import com.nhaarman.mockitokotlin2.doAnswer
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import io.reactivex.Observable
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExternalResource
import org.junit.runner.RunWith
import org.threeten.bp.LocalDateTime
import software.uniqore.codesample.model.Photo
import software.uniqore.codesample.repository.PhotoRepository


class ActivityInjectionRule<Act : Activity>(val activityClass: Class<Act>, val injectionFunction: (Act) -> Unit) : ExternalResource() {


    val application = InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as Application
    val listener = object : Application.ActivityLifecycleCallbacks {
        override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
            if (activityClass.isInstance(activity)) {
                injectionFunction(activity as Act)
            }

        }

        override fun onActivityStarted(activity: Activity) {
        }

        override fun onActivityResumed(activity: Activity) {
        }

        override fun onActivityPaused(activity: Activity) {
        }

        override fun onActivityStopped(activity: Activity) {
        }

        override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
        }

        override fun onActivityDestroyed(activity: Activity) {
        }
    }


    override fun before() {
        application.registerActivityLifecycleCallbacks(listener)
    }

    override fun after() {
        application.unregisterActivityLifecycleCallbacks(listener)
    }

}

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
        }

        viewModel = PhotoViewModel(repository)

        activityRule.launchActivity(null)

    }

    @Test
    fun testText() {
        onView(withId(R.id.my_recycler_view)).check(matches(isDisplayed()))

        onView(withText(cachedPhotoList()[0].author)).check(matches(isDisplayed()))
    }


}