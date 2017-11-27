package com.github.nekdenis.weatherlogger.core.rx;

import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable

interface CompositeDisposableHolder {
    var composite: CompositeDisposable

    fun Disposable.bind()

    fun resetCompositeDisposable() {
        synchronized(this) {
            composite.clear()
            composite = CompositeDisposable()
        }
    }

}

class CompositeDisposableHolderImpl : CompositeDisposableHolder {

    override var composite = CompositeDisposable()

    override fun Disposable.bind() {
        composite.add(this)
    }
}