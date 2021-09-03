package com.github.nekdenis.weatherlogger.devices

import androidx.annotation.CheckResult
import com.github.nekdenis.weatherlogger.MQTT_AIR_CONDITIONER_CONTROL_TOPIC
import com.github.nekdenis.weatherlogger.MQTT_AIR_CONDITIONER_RESPONSE_TOPIC
import com.github.nekdenis.weatherlogger.MQTT_COMMAND_TURN_OFF
import com.github.nekdenis.weatherlogger.MQTT_COMMAND_TURN_ON
import com.github.nekdenis.weatherlogger.core.rx.CompositeDisposableHolder
import com.github.nekdenis.weatherlogger.core.system.LCRX
import com.github.nekdenis.weatherlogger.logic.Watchdog
import com.github.nekdenis.weatherlogger.messaging.client.MessageClient
import io.reactivex.Completable

interface AirConditioner : LCRX {
    var watchdogListener: (alive: Boolean) -> Unit

    @CheckResult
    fun turnOnConditioner(): Completable

    @CheckResult
    fun turnOffConditioner(): Completable
}

class AirConditionerMqtt(
        val messageClient: MessageClient,
        val compositeDisposableHolder: CompositeDisposableHolder,
        val watchdog: Watchdog
) : AirConditioner, CompositeDisposableHolder by compositeDisposableHolder {

    override var watchdogListener: (alive: Boolean) -> Unit = {}

    override fun onStart() {
        messageClient.subscribeToTopic(MQTT_AIR_CONDITIONER_RESPONSE_TOPIC)
                .subscribe(this::pingReceived)
                .bind()
    }

    private fun pingReceived(message: String) {
        watchdog.onEvent()
    }

    override fun turnOnConditioner()
            = messageClient.publishMessage(MQTT_COMMAND_TURN_ON, MQTT_AIR_CONDITIONER_CONTROL_TOPIC)


    override fun turnOffConditioner()
            = messageClient.publishMessage(MQTT_COMMAND_TURN_OFF, MQTT_AIR_CONDITIONER_CONTROL_TOPIC)
}