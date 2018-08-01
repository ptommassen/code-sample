package software.uniqore.codesample.support

import android.arch.core.executor.ArchTaskExecutor
import android.arch.core.executor.TaskExecutor
import io.reactivex.android.plugins.RxAndroidPlugins
import io.reactivex.plugins.RxJavaPlugins
import io.reactivex.schedulers.Schedulers
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement


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
