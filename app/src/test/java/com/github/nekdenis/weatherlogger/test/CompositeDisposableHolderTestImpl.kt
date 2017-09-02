package com.github.nekdenis.weatherlogger.test

import com.github.nekdenis.weatherlogger.core.rx.CompositeDisposableHolder
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable


class CompositeDisposableHolderTestImpl : CompositeDisposableHolder {
    override var composite: CompositeDisposable = CompositeDisposable()

    override fun Disposable.bind() {
    }

}