package com.github.nekdenis.weatherlogger.messaging.server

import org.eclipse.moquette.server.Server

interface MqqtBroker {
    fun start(messageHandler: MessageHandler)
    fun stop()
}

class MqqtBrokerImpl : MqqtBroker {
    private lateinit var mqttBroker: Server

    override fun start(messageHandler: MessageHandler) {
        mqttBroker = Server()
        mqttBroker.startServer()
    }

    override fun stop() {
        if (mqttBroker != null) mqttBroker.stopServer()
        else throw IllegalStateException("MQTT server was not started before calling stop()")
    }

}
