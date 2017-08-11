package com.github.nekdenis.weatherlogger.sensors

import com.github.nekdenis.weatherlogger.utils.BoardDefaults
import rocks.androidthings.arduwrap.Arduino
import rocks.androidthings.arduwrap.Dht22Driver

interface TemperatureProvider {
    fun start()
    fun stop()
    fun humidity(): Double?
    fun temperature(): Double?
}

class TemperatureProviderImpl : TemperatureProvider {
    val mArduino = Arduino.ArduinoBuilder().uartDeviceName(BoardDefaults.getUartName()).build()
    val dht22Driver = Dht22Driver(mArduino)

    override fun start() {
        dht22Driver.startup()
    }

    override fun temperature(): Double? = dht22Driver.temperature.toDoubleOrNull()
    override fun humidity(): Double? = dht22Driver.humidity.toDoubleOrNull()

    private fun String.toDoubleOrNull() = if (isEmpty()) null else toDouble()

    override fun stop() {
        dht22Driver.shutdown()
    }
}
