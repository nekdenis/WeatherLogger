package com.github.nekdenis.weatherlogger.logic

import com.github.nekdenis.weatherlogger.core.system.TimeProvider
import com.github.nekdenis.weatherlogger.devices.Display
import com.github.nekdenis.weatherlogger.devices.Leds
import com.github.nekdenis.weatherlogger.model.WeatherModel


interface IndicatorController {
    fun onNewReading(weather: WeatherModel, boundaryTemperature: Double)
    fun onSystemError()
    fun onSystemErrorSolved()
    fun onTemperatureError()
    fun onTemperatureErrorSolved()
}

class IndicatorControllerImpl(
        private val display: Display,
        private val leds: Leds,
        private val timeProvider: TimeProvider
) : IndicatorController {

    init {
        leds.apply {
            turnOnBlue(false)
            turnOnGreen(false)
            turnOnBlue(false)
        }
    }

    override fun onNewReading(weather: WeatherModel, boundaryTemperature: Double) {
        display.setBrightness(timeProvider.isNight())
        display.updateDisplay(formatDisplayValue(weather.temperature, boundaryTemperature))
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