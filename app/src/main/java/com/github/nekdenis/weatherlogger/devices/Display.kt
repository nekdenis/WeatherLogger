package com.github.nekdenis.weatherlogger.devices

import com.github.nekdenis.weatherlogger.utils.Logger
import com.google.android.things.contrib.driver.ht16k33.AlphanumericDisplay
import com.google.android.things.contrib.driver.ht16k33.Ht16k33
import com.google.android.things.contrib.driver.rainbowhat.RainbowHat
import java.io.IOException


interface Display {
    fun updateDisplay(value: Double)
    fun stop()
    fun setBrightness(isNight: Boolean)
}

private const val TAG = "DISPLAY::"

class DisplayImpl(val log: Logger) : Display {
    private val display: AlphanumericDisplay = RainbowHat.openDisplay().apply {
        try {
            setEnabled(true)
            setBrightness(Ht16k33.HT16K33_BRIGHTNESS_MAX)
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

    override fun setBrightness(isNight: Boolean) {
        display.setBrightness(
                if (isNight) Ht16k33.HT16K33_BRIGHTNESS_MAX / 4
                else Ht16k33.HT16K33_BRIGHTNESS_MAX
        )
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