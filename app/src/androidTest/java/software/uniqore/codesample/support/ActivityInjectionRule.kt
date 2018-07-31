package software.uniqore.codesample.support

import android.app.Activity
import android.app.Application
import android.os.Bundle
import android.support.test.InstrumentationRegistry
import org.junit.rules.ExternalResource


/**
 * Rule to work around me not getting DaggerMock to work nicely with the Android lifecycle; when
 * an Activity of activityClass gets created, injectionFunction will be run instead of Dagger-injection,
 * allowing a test to inject their own mocks in the injectable fields.
 */
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