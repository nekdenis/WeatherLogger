package com.github.nekdenis.weatherlogger.messaging.client

import com.github.nekdenis.weatherlogger.MQTT_AIR_CONDITIONER_RESPONSE_TOPIC
import com.github.nekdenis.weatherlogger.utils.Logger
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken
import org.eclipse.paho.client.mqttv3.MqttCallback
import org.eclipse.paho.client.mqttv3.MqttClient
import org.eclipse.paho.client.mqttv3.MqttConnectOptions
import org.eclipse.paho.client.mqttv3.MqttException
import org.eclipse.paho.client.mqttv3.MqttMessage
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence

private const val TAG = "MQTTCLIENT:: "

interface MessageClient {
    fun connect(serverUrl: String, clientName: String, onConnected: () -> Unit, onError: () -> Unit)
    fun subscribeToTopic(subscriptionTopic:String, messageListener: MessageListener)
    fun publishMessage(publishMessage: String, publishTopic: String)
    fun disconnect()
}

interface MessageListener {
    fun onReceived(topic: String, message: String)
}

class MessageClientImpl(val logger: Logger) : MessageClient {

    lateinit var mqttAndroidClient: MqttClient

    var qos = 2
    var persistence = MemoryPersistence()

    override fun connect(
            serverUrl: String,
            clientName: String,
            onConnected: () -> Unit,
            onError: () -> Unit
    ) {
        try {
            mqttAndroidClient = MqttClient(serverUrl, clientName, persistence)
            val connOpts = MqttConnectOptions()
            connOpts.isCleanSession = true
            logger.d("$TAG Connecting to broker: " + serverUrl)
            mqttAndroidClient.connect(connOpts)
            mqttAndroidClient.setCallback(object : MqttCallback{
                override fun messageArrived(topic: String?, message: MqttMessage?) {
                    logger.e(message = "$TAG messageArrived but not processed by special callback! Received message: $topic : $message")
                }

                override fun connectionLost(cause: Throwable?) {
                    logger.e(message = "$TAG connectionLost()")
                }

                override fun deliveryComplete(token: IMqttDeliveryToken?) {
                    logger.d(message = "$TAG deliveryComplete()")
                }

            })
            mqttAndroidClient.subscribe(MQTT_AIR_CONDITIONER_RESPONSE_TOPIC)
            logger.d("$TAG Connected")
            onConnected()
        } catch (e: MqttException) {
            onError()
            logger.e(e, "$TAG can't connect:  ${e.message}")
        }
    }

    override fun subscribeToTopic(subscriptionTopic:String, messageListener: MessageListener) {
        try {
            mqttAndroidClient.subscribe(subscriptionTopic, qos) { topic, message ->
                val messageString = String(message.payload)
                logger.d(message = "$TAG Received message: $topic : $messageString")
                messageListener.onReceived(topic = topic, message = messageString)
            }
        } catch (ex: MqttException) {
            logger.e(ex, "$TAG Exception whilst subscribing")
        }

    }

    override fun publishMessage(publishMessage: String, publishTopic: String) {
        try {
            logger.d("$TAG Publishing message: " + publishMessage)
            val message = MqttMessage(publishMessage.toByteArray())
            message.qos = qos
            mqttAndroidClient.publish(publishTopic, message)
            logger.d("$TAG Message Published")
            if (!mqttAndroidClient.isConnected) {
                logger.e(message = "$TAG not connected while publishing")
            }
        } catch (e: MqttException) {
            logger.e(e, "$TAG Error Publishing:  ${e.message}")
        }
    }

    override fun disconnect() {
        try {
            mqttAndroidClient.disconnect()
            logger.d("$TAG Disconnected")
        } catch (e: MqttException) {
            logger.e(e, "$TAG can't disconnect connect:  ${e.message}")
        }
    }
}