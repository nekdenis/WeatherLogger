package com.github.nekdenis.weatherlogger.core.system

import android.util.Log

interface Logger {
    fun e(e: Throwable? = null, message: String)
    fun d(message: String)
}

class LoggerImpl : Logger {
    override fun e(e: Throwable?, message: String) {
        if (e != null) Log.e("APP", message, e)
        else Log.e("APP", message)
    }

    override fun d(message: String) {
        Log.d("APP", message)
    }

}