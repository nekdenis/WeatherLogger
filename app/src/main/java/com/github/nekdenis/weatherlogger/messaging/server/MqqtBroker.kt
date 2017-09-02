package com.github.nekdenis.weatherlogger.messaging.server

import com.github.nekdenis.weatherlogger.core.system.LC
import com.github.nekdenis.weatherlogger.core.system.Logger
import org.eclipse.moquette.server.Server

interface MqqtBroker : LC

private const val TAG = "MqttBroker::"

class MqqtBrokerImpl(
        val log: Logger
) : MqqtBroker {
    private lateinit var mqttBroker: Server

    override fun onStart() {
        log.d("$TAG starting")
        mqttBroker = Server()
        mqttBroker.startServer()
        log.d("$TAG started")
    }

    override fun onStop() {
        log.d("$TAG stopping")
        if (mqttBroker != null) mqttBroker.stopServer()
        else throw IllegalStateException("MQTT server was not started before calling stop()")
        log.d("$TAG stopped")
    }

}
