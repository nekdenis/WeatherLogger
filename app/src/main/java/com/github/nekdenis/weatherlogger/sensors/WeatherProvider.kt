package com.github.nekdenis.weatherlogger.sensors

import com.github.nekdenis.weatherlogger.MQTT_TEMPERATURE_ROOM_TOPIC
import com.github.nekdenis.weatherlogger.core.system.Logger
import com.github.nekdenis.weatherlogger.core.system.TimeProvider
import com.github.nekdenis.weatherlogger.db.WeatherModelRepo
import com.github.nekdenis.weatherlogger.logic.Watchdog
import com.github.nekdenis.weatherlogger.messaging.client.MessageClient
import com.github.nekdenis.weatherlogger.model.WeatherModel
import io.reactivex.Observable


interface WeatherProvider {
    fun observeWeather(): Observable<WeatherModel>
}

const val UNKNOWN_TEMPERATURE = -1000.0
const val UNKNOWN_HUMIDITY = -1000.0

private const val TAG = "WEATHER_REPO::"

class WeatherProviderImpl(
        val messageClient: MessageClient,
        val weatherModelRepo: WeatherModelRepo,
        val timeProvider: TimeProvider,
        val log: Logger,
        val watchdog: Watchdog
) : WeatherProvider {

    private val weatherObservable = retrieveData()
            .flatMapSingle { weatherModelRepo.saveTemperatureValue(it, timeProvider) }

    override fun observeWeather(): Observable<WeatherModel> = weatherObservable

    private fun retrieveData(): Observable<WeatherModel> =
            messageClient.subscribeToTopic(MQTT_TEMPERATURE_ROOM_TOPIC)
                    .map(this::parseResponse)
                    .doOnNext { pingReceived() }
                    .doOnNext { log.d("$TAG t=${it.temperature}, h=${it.humidity}") }

    private fun pingReceived() {
        watchdog.onEvent()
    }

    private fun parseResponse(message: String) = message.split('|')
            .let {
                WeatherModel(
                        temperature = it[0].toDoubleOrNull() ?: UNKNOWN_TEMPERATURE,
                        humidity = it[1].toDoubleOrNull() ?: UNKNOWN_HUMIDITY,
                        timeStamp = timeProvider.nowMillis())
            }

}