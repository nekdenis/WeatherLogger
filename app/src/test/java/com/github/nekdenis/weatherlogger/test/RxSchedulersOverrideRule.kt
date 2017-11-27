package com.github.nekdenis.weatherlogger.test

import io.reactivex.android.plugins.RxAndroidPlugins
import io.reactivex.internal.schedulers.ImmediateThinScheduler
import io.reactivex.plugins.RxJavaPlugins
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement

/**
 * This rule registers SchedulerHooks for RxJava and RxAndroid to ensure that subscriptions
 * always subscribeOn and observeOn Schedulers.immediate().
 * Warning, this rule will reset RxAndroidPlugins and RxJavaPlugins before and after each test so
 * if the application code uses RxJava plugins this may affect the behaviour of the testing method.
 */
class RxSchedulersOverrideRule : TestRule {

    override fun apply(base: Statement, description: Description): Statement {
        return object : Statement() {
            @Throws(Throwable::class)
            override fun evaluate() {
                val testScheduler = ImmediateThinScheduler.INSTANCE

                RxAndroidPlugins.reset()
                RxJavaPlugins.reset()

                RxAndroidPlugins.setInitMainThreadSchedulerHandler { testScheduler }
                RxAndroidPlugins.setMainThreadSchedulerHandler { testScheduler }

                RxJavaPlugins.setComputationSchedulerHandler { testScheduler }
                RxJavaPlugins.setIoSchedulerHandler { testScheduler }
                RxJavaPlugins.setNewThreadSchedulerHandler { testScheduler }
                RxJavaPlugins.setSingleSchedulerHandler { testScheduler }

                base.evaluate()

                RxAndroidPlugins.reset()
                RxJavaPlugins.reset()
            }
        }
    }
}