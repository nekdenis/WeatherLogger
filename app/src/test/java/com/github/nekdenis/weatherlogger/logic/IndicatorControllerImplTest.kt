package com.github.nekdenis.weatherlogger.logic

import com.github.nekdenis.weatherlogger.devices.Display
import com.github.nekdenis.weatherlogger.devices.Leds
import com.github.nekdenis.weatherlogger.model.CONFIG_MODE_AUTO
import com.github.nekdenis.weatherlogger.model.CONFIG_MODE_OFF
import com.github.nekdenis.weatherlogger.model.CONFIG_MODE_ON
import com.github.nekdenis.weatherlogger.model.ConditionerConfig
import com.github.nekdenis.weatherlogger.model.WeatherModel
import com.github.nekdenis.weatherlogger.test.TimeProviderTestImpl
import org.junit.Assert
import org.junit.Test

class IndicatorControllerImplTest {

    var dispVal = -1.0
    var currentTemp = 45.0
    var night: Boolean? = null
    var redLight: Boolean? = null
    var greenLight: Boolean? = null
    var blueLight: Boolean? = null

    val indicator = createTextIndicator()

    private fun createTextIndicator(nightMode: Boolean = false): IndicatorControllerImpl {
        return IndicatorControllerImpl(object : Display {
            override fun setBrightness(isNight: Boolean) {
                night = isNight
            }

            override fun updateDisplay(value: Double) {
                dispVal = value
            }

            override fun stop() {
            }

        }, object : Leds {
            override fun turnOnRed(enable: Boolean) {
                redLight = enable
            }

            override fun turnOnBlue(enable: Boolean) {
                blueLight = enable
            }

            override fun turnOnGreen(enable: Boolean) {
                greenLight = enable
            }

        }, TimeProviderTestImpl(nightMode))
    }

    @Test
    fun testInit() {
        Assert.assertEquals(false, blueLight)
        Assert.assertEquals(false, redLight)
        Assert.assertEquals(false, greenLight)
    }

    @Test
    fun onNewReadingAUTO() {
        indicator.onNewReading(WeatherModel(35.0, 0.0, 0L), ConditionerConfig(45.0, CONFIG_MODE_AUTO))
        Assert.assertEquals(3545.0, dispVal, 0.1)
    }

    @Test
    fun onNewReadingON() {
        indicator.onNewReading(WeatherModel(32.0, 0.0, 0L), ConditionerConfig(45.0, CONFIG_MODE_ON))
        Assert.assertEquals(3201.0, dispVal, 0.1)
    }

    @Test
    fun onNewReadingOFF() {
        indicator.onNewReading(WeatherModel(33.0, 0.0, 0L), ConditionerConfig(45.0, CONFIG_MODE_OFF))
        Assert.assertEquals(3300.0, dispVal, 0.1)
    }

    @Test
    fun onNewReadingNight() {
        val indicator1 = createTextIndicator(true)
        indicator1.onNewReading(WeatherModel(35.0, 0.0, 0L), ConditionerConfig(45.0, CONFIG_MODE_AUTO))
        Assert.assertEquals(true, night)
    }

    @Test
    fun onSystemError() {
        indicator.onSystemError()
        Assert.assertEquals(true, redLight)
        indicator.onSystemErrorSolved()
        Assert.assertEquals(false, redLight)
    }

    @Test
    fun onTemperatureError() {
        indicator.onTemperatureError()
        Assert.assertEquals(true, greenLight)
        indicator.onTemperatureErrorSolved()
        Assert.assertEquals(false, greenLight)
    }
}