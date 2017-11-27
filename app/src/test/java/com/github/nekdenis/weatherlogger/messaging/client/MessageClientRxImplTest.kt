package com.github.nekdenis.weatherlogger.messaging.client

import com.github.nekdenis.weatherlogger.test.CompositeDisposableHolderTestImpl
import com.github.nekdenis.weatherlogger.test.RxSchedulersOverrideRule
import org.junit.Assert
import org.junit.Rule
import org.junit.Test


class MessageClientRxImplTest {

    @Rule @JvmField val rxRule = RxSchedulersOverrideRule()

    var connected: Boolean? = null
    var serverURL: String? = null
    var clientName: String? = null
    var subscribed: String? = null
    var lastMessage: String? = null
    var lastTopic: String? = null
    var connectionCount = 0
    var disconnected: Boolean? = null

    var messenger: ((topic: String, message: String) -> Unit)? = null
    var errorProcessor: ((e: Throwable) -> Unit)? = null

    val client = MessageClientRxImpl(
            object : MqttClient {
                override fun connect(serverUrl: String, clientName: String, onConnected: () -> Unit, onMessage: (topic: String, message: String) -> Unit, onError: (e: Throwable) -> Unit) {
                    this@MessageClientRxImplTest.clientName = clientName
                    this@MessageClientRxImplTest.serverURL = serverUrl
                    this@MessageClientRxImplTest.connected = true
                    this@MessageClientRxImplTest.messenger = onMessage
                    this@MessageClientRxImplTest.errorProcessor = onError
                    connectionCount++
                    onConnected()
                }

                override fun subscribeToTopic(subscriptionTopic: String, messageListener: MessageListener) {
                    subscribed = subscriptionTopic
                }

                override fun subscribeToTopic(subscriptionTopic: String) {
                    subscribed = subscriptionTopic
                }

                override fun publishMessage(publishMessage: String, publishTopic: String) {
                    lastMessage = publishMessage
                    lastTopic = publishTopic
                }

                override fun disconnect() {
                    disconnected = true
                }

            },
            CompositeDisposableHolderTestImpl()
    )

    private fun initClient() {
        client.configClient("SERVER", "CLIENT", retryOnError = false)
        client.onStart()
    }

    @Test
    fun shouldConnect() {
        initClient()
        Assert.assertEquals(true, connected)
        Assert.assertEquals("SERVER", serverURL)
        Assert.assertEquals("CLIENT", clientName)
    }

    @Test
    fun shouldSubscribeOnce() {
        initClient()
        client.publishMessage("", "").test()
        client.publishMessage("", "").test()
        client.subscribeToTopic("").test()
        client.subscribeToTopic("").test()
        Assert.assertEquals(1, connectionCount)
    }

    @Test
    fun shouldSubscribe() {
        initClient()
        client.subscribeToTopic("aaa").test()
        Assert.assertEquals("aaa", subscribed)
    }


    @Test
    fun shouldReceiveMessage() {
        initClient()
        val testObserver = client.subscribeToTopic("aaa").test()
        messenger!!("any", "mess")
        testObserver.assertValue { it == "mess" }
    }

    @Test
    fun shouldPublishMessage() {
        initClient()
        val testObserver = client.publishMessage("hey!", "bb").test()
        Assert.assertEquals("hey!", lastMessage)
        Assert.assertEquals("bb", lastTopic)
    }

    @Test
    fun shouldObserveErrorConnection() {
        initClient()
        val testObserver = client.observeConnection().test()
        errorProcessor!!(Exception("aa"))
        testObserver.assertError { it.message == "aa" }
        Assert.assertTrue(disconnected!!)
    }

    @Test
    fun shouldObserveSuccessfulConnection() {
        initClient()
        val testObserver = client.observeConnection().test()
        testObserver.assertValue { it == true }
    }
}