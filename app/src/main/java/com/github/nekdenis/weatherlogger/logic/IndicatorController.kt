package com.github.nekdenis.weatherlogger.logic

import com.github.nekdenis.weatherlogger.db.DBProvider
import com.github.nekdenis.weatherlogger.devices.Display
import com.github.nekdenis.weatherlogger.devices.Leds
import com.github.nekdenis.weatherlogger.model.WeatherModel
import com.github.nekdenis.weatherlogger.utils.TimeProvider


interface IndicatorController {
    fun onNewReading(weather: WeatherModel)
}

class IndicatorControllerImpl(
        private val display: Display,
        private val leds: Leds,
        private val dbProvider: DBProvider,
        private val timeProvider: TimeProvider
) : IndicatorController {

    override fun onNewReading(weather: WeatherModel) {
        display.setBrightness(timeProvider.isNight())
        display.updateDisplay(formatDisplayValue(weather.temperature))
        leds.apply {
            turnOnBlue(false)
            turnOnGreen(false)
            turnOnBlue(false)
        }
    }

    private fun formatDisplayValue(temperature: Double): Double =
            (dbProvider.pullBoundaryTemperature().toInt() + temperature.toInt() * 100).toDouble()

}