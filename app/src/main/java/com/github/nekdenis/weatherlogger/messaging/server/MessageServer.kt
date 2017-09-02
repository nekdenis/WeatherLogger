package com.github.nekdenis.weatherlogger.messaging.server

import com.github.nekdenis.weatherlogger.core.rx.CompositeDisposableHolder
import com.github.nekdenis.weatherlogger.core.system.LCRX
import io.reactivex.Completable
import io.reactivex.schedulers.Schedulers


interface MessageServer : LCRX

class MessageServerImpl(
        val mqqtBroker: MqqtBroker,
        val disposableHolder: CompositeDisposableHolder
) : MessageServer, CompositeDisposableHolder by disposableHolder {

    override fun onStart() {
        serverCompletable().observeOn(Schedulers.io())
                .doOnDispose { mqqtBroker.onStop() }
                .subscribe()
                .bind()
    }

    private fun serverCompletable() = Completable.fromCallable {
        mqqtBroker.onStart()
    }

    override fun onStop() {
        super.onStop()
    }

}