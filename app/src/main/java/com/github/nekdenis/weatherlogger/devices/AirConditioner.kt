package com.github.nekdenis.weatherlogger.devices

import com.github.nekdenis.weatherlogger.MQTT_AIR_CONDITIONER_CONTROL_TOPIC
import com.github.nekdenis.weatherlogger.MQTT_CLIENT_NAME
import com.github.nekdenis.weatherlogger.MQTT_COMAND_TURN_OFF
import com.github.nekdenis.weatherlogger.MQTT_COMAND_TURN_ON
import com.github.nekdenis.weatherlogger.MQTT_SERVER_URL
import com.github.nekdenis.weatherlogger.logic.ClimateControllerCallback
import com.github.nekdenis.weatherlogger.messaging.ClientRunner

interface AirConditioner : ClimateControllerCallback {
    fun init(onInitialized: () -> Unit)
}

class AirConditionerMqtt(
        val clientRunner: ClientRunner
) : AirConditioner {

    override fun init( onInitialized: () -> Unit) {
        clientRunner.connectClient(MQTT_SERVER_URL, MQTT_CLIENT_NAME, onInitialized)
    }

    override fun turnOnConditioner() {
        clientRunner.publishMessage(MQTT_COMAND_TURN_ON, MQTT_AIR_CONDITIONER_CONTROL_TOPIC)
    }

    override fun turnOffConditioner() {
        clientRunner.publishMessage(MQTT_COMAND_TURN_OFF, MQTT_AIR_CONDITIONER_CONTROL_TOPIC)
    }
}