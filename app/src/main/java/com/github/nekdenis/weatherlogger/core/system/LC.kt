package com.github.nekdenis.weatherlogger.core.system

import androidx.annotation.CallSuper
import com.github.nekdenis.weatherlogger.core.rx.CompositeDisposableHolder


/**
 * Lifecycle of most of components
 */
interface LC {

    fun onStart()
    fun onStop()
}

/**
 * Lifecycle of most of components which have rx components
 */
interface LCRX : LC,CompositeDisposableHolder {

    @CallSuper
    override fun onStop() = resetCompositeDisposable()
}