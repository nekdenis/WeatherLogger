package com.github.nekdenis.weatherlogger.messaging

import android.os.Handler
import android.os.Looper
import com.github.nekdenis.weatherlogger.MQTT_RECONNECT_TIMEOUT
import com.github.nekdenis.weatherlogger.messaging.client.MessageClient
import com.github.nekdenis.weatherlogger.messaging.client.MessageListener
import java.io.Serializable
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit


interface ClientRunner {
    fun connectClient(serverUrl: String, clientName: String, onConnected: () -> Unit, connectionDelay: Long = 0L)
    fun subscribeToTopic(subscriptionTopic: String, messageListener: MessageListener)
    fun publishMessage(publishMessage: String, publishTopic: String)
    fun stopClient()
}

private const val CONNECTED: Int = 10
private const val ERROR_CONNECTION: Int = CONNECTED + 1
private const val RECEIVED: Int = ERROR_CONNECTION + 1

class ClientRunnerAndroidImpl(
        val client: MessageClient
) : ClientRunner {
    val threadPool = Executors.newSingleThreadScheduledExecutor()

    val uiHandler = Handler(Looper.getMainLooper()) { msg ->
        when (msg.what) {
            CONNECTED        -> onConnected.invoke()
            ERROR_CONNECTION -> connectClient(serverUrl, clientName, onConnected, MQTT_RECONNECT_TIMEOUT)
            RECEIVED         -> (msg.obj as MsgPld).apply {
                subscriber.onReceived(topic = topic, message = message)
            }
        }
        true
    }

    lateinit var serverUrl: String
    lateinit var clientName: String
    lateinit var onConnected: () -> Unit
    lateinit var subscriber: MessageListener
    lateinit var handler: Handler

    override fun connectClient(serverUrl: String, clientName: String, onConnected: () -> Unit, connectionDelay: Long) {
        this.serverUrl = serverUrl
        this.clientName = clientName
        this.onConnected = onConnected
        threadPool.schedule({
            client.connect(serverUrl, clientName,
                    onConnected = {
                        uiHandler.obtainMessage(CONNECTED).sendToTarget()
                    },
                    onError = {
                        uiHandler.obtainMessage(ERROR_CONNECTION).sendToTarget()
                    }
            )
        }, connectionDelay, TimeUnit.MILLISECONDS)
    }

    override fun subscribeToTopic(subscriptionTopic: String, messageListener: MessageListener) {
        subscriber = messageListener
        threadPool.execute {
            client.subscribeToTopic(subscriptionTopic, object : MessageListener {
                override fun onReceived(topic: String, message: String) {
                    uiHandler.obtainMessage(RECEIVED, MsgPld(topic, message)).sendToTarget()
                }
            })
        }
    }

    override fun publishMessage(publishMessage: String, publishTopic: String) {
        threadPool.execute {
            client.publishMessage(publishMessage, publishTopic)
        }
    }

    override fun stopClient() {
        client.disconnect()
    }

    private class MsgPld(val topic: String, val message: String) : Serializable

}