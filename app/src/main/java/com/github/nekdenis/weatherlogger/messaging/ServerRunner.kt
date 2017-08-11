package com.github.nekdenis.weatherlogger.messaging

import android.os.Handler
import android.os.HandlerThread


interface ServerRunner {
    fun runServer(runnable: Runnable)
    fun stopServer()
}

class ServerRunnerAndroidImpl : ServerRunner {
    val thread = HandlerThread("MqttBrokerThread")

    var lastRunnable: Runnable? = null

    lateinit var handler: Handler
    override fun runServer(runnable: Runnable) {
        stopServer()
        lastRunnable = runnable
        thread.run {
            start()
            handler = Handler(looper)
            handler.post(runnable)
        }
    }

    override fun stopServer() {
        lastRunnable?.let(handler::removeCallbacks)
    }

}