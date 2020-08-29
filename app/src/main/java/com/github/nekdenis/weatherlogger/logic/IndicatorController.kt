package com.github.nekdenis.weatherlogger.logic

import com.github.nekdenis.weatherlogger.core.system.TimeProvider
import com.github.nekdenis.weatherlogger.devices.Display
import com.github.nekdenis.weatherlogger.devices.Leds
import com.github.nekdenis.weatherlogger.model.*


interface IndicatorController {
    fun onNewReading(weather: WeatherModel, config: ConditionerConfig)
    fun onSystemError()
    fun onSystemErrorSolved()
    fun onTemperatureError()
    fun onTemperatureErrorSolved()
    fun onNewReadingWithAqi(weather: WeatherModel, weatherDataType: WEATHER_DATA)
}

class IndicatorControllerImpl(
        private val display: Display,
        private val leds: Leds,
        private val timeProvider: TimeProvider
) : IndicatorController {

    init {
        leds.apply {
            turnOnRed(false)
            turnOnGreen(false)
            turnOnBlue(false)
        }
    }

    override fun onNewReading(weather: WeatherModel, config: ConditionerConfig) {
        display.setBrightness(timeProvider.isNight())
        when (config.mode) {
            CONFIG_MODE_AUTO -> updateDisplay(weather.temperature, config.boundaryTemp)
            CONFIG_MODE_ON -> updateDisplay(weather.temperature, 01.0)
            CONFIG_MODE_OFF -> updateDisplay(weather.temperature, 00.0)
        }
    }

    override fun onNewReadingWithAqi(weather: WeatherModel, weatherDataType: WEATHER_DATA) {
        display.setBrightness(timeProvider.isNight())
        when (weatherDataType) {
            WEATHER_DATA.TEMPERATURE -> {
                display.updateDisplay(weather.temperature)
                display.setRating(0)
            }
            WEATHER_DATA.HUMIDITY -> {
                display.updateDisplay(weather.humidity)
                display.setRating(0)
            }
            WEATHER_DATA.AQI -> {
                display.updateDisplay(weather.airQualityIndex.value)
                display.setRating(weather.airQualityIndex.rating)
            }
        }
    }

    private fun updateDisplay(first: Double, second: Double) {
        display.updateDisplay(formatDisplayValue(first, second))
    }

    private fun formatDisplayValue(temperature: Double, boundaryTemperature: Double): Double =
            (boundaryTemperature.toInt() + temperature.toInt() * 100).toDouble()

    override fun onSystemError() {
        leds.turnOnRed(true)
    }

    override fun onSystemErrorSolved() {
        leds.turnOnRed(false)
    }

    override fun onTemperatureError() {
        leds.turnOnGreen(true)
    }

    override fun onTemperatureErrorSolved() {
        leds.turnOnGreen(false)
    }
}