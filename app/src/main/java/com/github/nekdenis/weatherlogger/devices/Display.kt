package com.github.nekdenis.weatherlogger.devices

import com.github.nekdenis.weatherlogger.utils.BoardDefaults
import com.github.nekdenis.weatherlogger.utils.Logger
import com.google.android.things.contrib.driver.ht16k33.AlphanumericDisplay
import java.io.IOException


interface Display {
    fun updateDisplay(value: Double)
    fun stop()
}

private const val TAG = "DISPLAY::"

class DisplayImpl(val log: Logger) : Display {
    private val display: AlphanumericDisplay = AlphanumericDisplay(BoardDefaults.getI2cBus()).apply {
        try {
            setEnabled(true)
            clear()
            log.d("$TAG Initialized I2C Display")
        } catch (e: IOException) {
            log.e(e, "$TAG Error initializing display")
            log.d("$TAG Display disabled")
        }
    }

    override fun updateDisplay(value: Double) {
        try {
            display.display(value)
        } catch (e: IOException) {
            log.e(e, "$TAG Error setting display")
        }
    }

    override fun stop() {
        try {
            display.clear()
            display.setEnabled(false)
            display.close()
        } catch (e: IOException) {
            log.e(e, "$TAG Error disabling display")
        }
    }
}