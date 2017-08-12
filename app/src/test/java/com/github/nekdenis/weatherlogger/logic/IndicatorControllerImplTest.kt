package com.github.nekdenis.weatherlogger.logic

import com.github.nekdenis.weatherlogger.db.DBProvider
import com.github.nekdenis.weatherlogger.devices.Display
import com.github.nekdenis.weatherlogger.model.WeatherModel
import org.junit.Assert
import org.junit.Test

class IndicatorControllerImplTest {

    var dispVal = -1.0
    var currentTemp = 45.0

    val indicator = IndicatorControllerImpl(object : Display {
        override fun updateDisplay(value: Double) {
            dispVal = value
        }

        override fun stop() {
        }

    }, object : DBProvider {
        override fun saveTemperatureValue(data: WeatherModel) {
        }

        override fun saveBoundaryTemperature(temp: Double) {
        }

        override fun pullBoundaryTemperature(): Double = currentTemp
    })

    @Test
    fun onNewReading() {
        indicator.onNewReading(WeatherModel(35.0, 0.0, 0L))
        Assert.assertEquals(3545.0, dispVal, 0.1)
    }
}