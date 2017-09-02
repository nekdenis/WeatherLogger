package com.github.nekdenis.weatherlogger.sensors

import com.github.nekdenis.weatherlogger.core.iot.BoardDefaults
import com.github.nekdenis.weatherlogger.core.system.LC
import rocks.androidthings.arduwrap.Arduino
import rocks.androidthings.arduwrap.Dht22Driver

interface TemperatureProvider : LC {
    fun humidity(): Double?
    fun temperature(): Double?
}

class TemperatureProviderArduino : TemperatureProvider {
    val mArduino = Arduino.ArduinoBuilder().uartDeviceName(BoardDefaults.getUartName()).build()
    val dht22Driver = Dht22Driver(mArduino)

    override fun onStart() {
        dht22Driver.startup()
    }

    override fun temperature(): Double? = dht22Driver.temperature.toDoubleOrNull()
    override fun humidity(): Double? = dht22Driver.humidity.toDoubleOrNull()

    private fun String.toDoubleOrNull() = if (isEmpty()) null else toDouble()

    override fun onStop() {
        dht22Driver.shutdown()
    }
}
