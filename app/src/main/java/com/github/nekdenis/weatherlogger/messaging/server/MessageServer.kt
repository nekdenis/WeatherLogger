package com.github.nekdenis.weatherlogger.messaging.server

import com.github.nekdenis.weatherlogger.messaging.ServerRunner


interface MessageServer {
    fun start()
    fun stop()
}

class MessageServerImpl(
        val mqqtBroker: MqqtBroker,
        val serverRunner: ServerRunner,
        val messageHandler: MessageHandler
) : MessageServer {

    override fun start() {
        serverRunner.runServer(serverRunner())
    }

    override fun stop() {
    }

    private fun serverRunner(): Runnable = Runnable {
        mqqtBroker.start(messageHandler)
    }

}