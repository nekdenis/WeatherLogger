package com.github.nekdenis.weatherlogger.logic

import com.github.nekdenis.weatherlogger.REFRESH_SENSORS_INTERVAL
import com.github.nekdenis.weatherlogger.model.WeatherModel
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class ClimateControllerImplTest {

    var conditionerState = -1

    val controller = ClimateControllerImpl()
    val callback = object : ClimateControllerCallback {
        override fun turnOnConditioner() {
            conditionerState = 1
        }

        override fun turnOffConditioner() {
            conditionerState = 0
        }
    }

    @Before
    fun setUp() {
        controller.setCallback(callback)
    }

    @Test
    fun shouldNotReactOnFirstReading() {
        controller.onNewReading(WeatherModel(30.0, 30.0, 0))
        Assert.assertEquals(-1, conditionerState)
    }

    @Test
    fun shouldNotReactOnSecondCloseReading() {
        controller.onNewReading(WeatherModel(30.0, 30.0, 0))
        controller.onNewReading(WeatherModel(30.0, 30.0, 10))
        Assert.assertEquals(-1, conditionerState)
    }

    @Test
    fun shouldTurnOn() {
        controller.onNewReading(WeatherModel(30.0, 30.0, 0))
        controller.onNewReading(WeatherModel(30.0, 30.0, REFRESH_SENSORS_INTERVAL * 1))
        controller.onNewReading(WeatherModel(30.0, 30.0, REFRESH_SENSORS_INTERVAL * 2))
        controller.onNewReading(WeatherModel(30.0, 30.0, REFRESH_SENSORS_INTERVAL * 3))
        controller.onNewReading(WeatherModel(30.0, 30.0, REFRESH_SENSORS_INTERVAL * 4))
        controller.onNewReading(WeatherModel(30.0, 30.0, REFRESH_SENSORS_INTERVAL * 5))
        Assert.assertEquals(1, conditionerState)
    }

    @Test
    fun shouldTurnOff() {
        controller.onNewReading(WeatherModel(25.0, 30.0, 0))
        controller.onNewReading(WeatherModel(25.0, 30.0, REFRESH_SENSORS_INTERVAL * 1))
        controller.onNewReading(WeatherModel(25.0, 30.0, REFRESH_SENSORS_INTERVAL * 2))
        controller.onNewReading(WeatherModel(25.0, 30.0, REFRESH_SENSORS_INTERVAL * 3))
        controller.onNewReading(WeatherModel(25.0, 30.0, REFRESH_SENSORS_INTERVAL * 4))
        controller.onNewReading(WeatherModel(25.0, 30.0, REFRESH_SENSORS_INTERVAL * 5))
        controller.onNewReading(WeatherModel(25.0, 30.0, REFRESH_SENSORS_INTERVAL * 5))
        controller.onNewReading(WeatherModel(25.0, 30.0, REFRESH_SENSORS_INTERVAL * 5))
        controller.onNewReading(WeatherModel(25.0, 30.0, REFRESH_SENSORS_INTERVAL * 5))
        controller.onNewReading(WeatherModel(25.0, 30.0, REFRESH_SENSORS_INTERVAL * 5))
        Assert.assertEquals(0, conditionerState)
    }

    @Test
    fun shouldTurnOffDespiteWarmReadings() {
        controller.onNewReading(WeatherModel(24.0, 30.0, 0))
        controller.onNewReading(WeatherModel(24.0, 30.0, REFRESH_SENSORS_INTERVAL * 1))
        controller.onNewReading(WeatherModel(24.0, 30.0, REFRESH_SENSORS_INTERVAL * 2))
        controller.onNewReading(WeatherModel(25.0, 30.0, REFRESH_SENSORS_INTERVAL * 3))
        controller.onNewReading(WeatherModel(28.0, 30.0, REFRESH_SENSORS_INTERVAL * 4))
        controller.onNewReading(WeatherModel(28.0, 30.0, REFRESH_SENSORS_INTERVAL * 5))
        Assert.assertEquals(0, conditionerState)
    }
}

