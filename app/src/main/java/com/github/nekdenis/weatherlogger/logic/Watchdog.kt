package com.github.nekdenis.weatherlogger.logic

import com.github.nekdenis.weatherlogger.core.rx.CompositeDisposableHolder
import com.github.nekdenis.weatherlogger.core.system.LCRX
import com.github.nekdenis.weatherlogger.core.system.TimeProvider
import io.reactivex.Observable
import java.util.concurrent.TimeUnit


interface Watchdog : LCRX {

    fun configure(checkInterval: Long, errorInterval: Long, onWorking: () -> Unit, onStopped: () -> Unit)

    fun onEvent()
}

class WatchdogImpl(
        val timeProvider: TimeProvider,
        val compositeDisposableHolder: CompositeDisposableHolder
) : Watchdog, CompositeDisposableHolder by compositeDisposableHolder {

    private var checkInterval: Long = 1
    private var errorInterval: Long = 1
    private lateinit var onWorking: () -> Unit
    private lateinit var onStopped: () -> Unit

    private var lastEventTime = timeProvider.nowMillis()

    override fun configure(checkInterval: Long, errorInterval: Long, onWorking: () -> Unit, onStopped: () -> Unit) {
        this.checkInterval = checkInterval
        this.errorInterval = errorInterval
        this.onWorking = onWorking
        this.onStopped = onStopped
    }

    override fun onStart() {
        Observable.interval(0, checkInterval, TimeUnit.MILLISECONDS)
                .doOnNext { watch() }
    }

    private fun watch() {
        if ((timeProvider.nowMillis() - lastEventTime) > errorInterval) onStopped()
        else onWorking()
    }

    override fun onEvent() {
        lastEventTime = timeProvider.nowMillis()
    }

}