package com.github.nekdenis.weatherlogger.logic

import com.github.nekdenis.weatherlogger.db.DBProvider
import com.github.nekdenis.weatherlogger.devices.Display
import com.github.nekdenis.weatherlogger.model.WeatherModel


interface IndicatorController {
    fun onNewReading(weather: WeatherModel)
}

class IndicatorControllerImpl(
        private val display: Display,
        private val dbProvider: DBProvider
) : IndicatorController {

    override fun onNewReading(weather: WeatherModel) {
        display.updateDisplay(formatDisplayValue(weather.temperature))
    }

    private fun formatDisplayValue(temperature: Double): Double =
            (dbProvider.pullBoundaryTemperature().toInt() + temperature.toInt() * 100).toDouble()

}