package com.github.nekdenis.weatherlogger.devices

import android.os.Handler
import android.os.Looper
import com.github.nekdenis.weatherlogger.MQTT_AIR_CONDITIONER_CONTROL_TOPIC
import com.github.nekdenis.weatherlogger.MQTT_AIR_CONDITIONER_RESPONSE_TOPIC
import com.github.nekdenis.weatherlogger.MQTT_CLIENT_NAME
import com.github.nekdenis.weatherlogger.MQTT_COMAND_TURN_OFF
import com.github.nekdenis.weatherlogger.MQTT_COMAND_TURN_ON
import com.github.nekdenis.weatherlogger.MQTT_SERVER_URL
import com.github.nekdenis.weatherlogger.MQTT_WATCHDOG_TIMEOUT
import com.github.nekdenis.weatherlogger.logic.ClimateControllerCallback
import com.github.nekdenis.weatherlogger.messaging.ClientRunner
import com.github.nekdenis.weatherlogger.messaging.client.MessageListener
import com.github.nekdenis.weatherlogger.utils.TimeProvider

interface AirConditioner : ClimateControllerCallback {
    fun init(onInitialized: () -> Unit, conditionerWatchdogListener: ((alive: Boolean) -> Unit))
    fun stop()
}

class AirConditionerMqtt(
        val clientRunner: ClientRunner,
        val timeProvider: TimeProvider
) : AirConditioner {
    var watchdogHandlerRunnable: Runnable? = null
    val handler: Handler = Handler(Looper.getMainLooper())
    var conditionerControllerLastResponse = 0L
    lateinit var conditionerControllerWatchdogListener: ((alive: Boolean) -> Unit)

    override fun init(onInitialized: () -> Unit, conditionerWatchdogListener: ((alive: Boolean) -> Unit)) {
        conditionerControllerWatchdogListener = conditionerWatchdogListener
        clientRunner.connectClient(MQTT_SERVER_URL, MQTT_CLIENT_NAME, {
            onClientConnected()
            onInitialized()
        })
    }

    private fun onClientConnected() {
        watchConditionerController()
    }

    override fun turnOnConditioner() {
        clientRunner.publishMessage(MQTT_COMAND_TURN_ON, MQTT_AIR_CONDITIONER_CONTROL_TOPIC)
    }

    override fun turnOffConditioner() {
        clientRunner.publishMessage(MQTT_COMAND_TURN_OFF, MQTT_AIR_CONDITIONER_CONTROL_TOPIC)
    }

    private fun watchConditionerController() {
        conditionerControllerLastResponse = timeProvider.nowMillis()
        clientRunner.subscribeToTopic(MQTT_AIR_CONDITIONER_RESPONSE_TOPIC, object : MessageListener {
            override fun onReceived(topic: String, message: String) {
                conditionerControllerLastResponse = timeProvider.nowMillis()
                conditionerControllerWatchdogListener(true)
                startWatchdog()
            }
        })
        startWatchdog()
    }

    private fun startWatchdog() {
        watchdogHandlerRunnable?.let { handler.removeCallbacks(it) }
        watchdogHandlerRunnable = Runnable {
            conditionerControllerWatchdogListener(timeProvider.nowMillis() - conditionerControllerLastResponse <= MQTT_WATCHDOG_TIMEOUT)
            startWatchdog()
        }
        handler.postDelayed(watchdogHandlerRunnable, MQTT_WATCHDOG_TIMEOUT / 2)
    }

    override fun stop() {
        handler.removeCallbacks(watchdogHandlerRunnable)
        clientRunner.stopClient()
    }
}