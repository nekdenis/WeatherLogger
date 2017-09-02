package com.github.nekdenis.weatherlogger

import android.app.Application
import com.github.nekdenis.weatherlogger.utils.Injector


class App : Application() {

    init {
        Injector.appRef = this
    }

    override fun onCreate() {
        super.onCreate()
    }
}