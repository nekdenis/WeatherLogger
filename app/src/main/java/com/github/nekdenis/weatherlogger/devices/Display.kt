package com.github.nekdenis.weatherlogger.devices

import android.graphics.Color
import com.github.nekdenis.weatherlogger.core.system.Logger
import com.google.android.things.contrib.driver.apa102.Apa102
import com.google.android.things.contrib.driver.ht16k33.AlphanumericDisplay
import com.google.android.things.contrib.driver.ht16k33.Ht16k33
import com.google.android.things.contrib.driver.rainbowhat.RainbowHat
import java.io.IOException


interface Display {
    fun updateDisplay(value: Double)
    fun updateDisplay(value: String)
    fun stop()
    fun setBrightness(isNight: Boolean)
    fun setRating(value: Int)
}

private const val TAG = "DISPLAY::"

class DisplayImpl(val log: Logger) : Display {
    private val display: AlphanumericDisplay = RainbowHat.openDisplay().apply {
        try {
            setEnabled(true)
            clear()
            log.d("$TAG Initialized I2C Display")
        } catch (e: IOException) {
            log.e(e, "$TAG Error initializing display")
            log.d("$TAG Display disabled")
        }
    }

    private val ledStrip: Apa102 = RainbowHat.openLedStrip().apply {
        brightness = 1
    }

    private fun generateRainbow(): IntArray {
        val rainbow = IntArray(RainbowHat.LEDSTRIP_LENGTH)
        rainbow[6] = Color.GREEN
        rainbow[5] = Color.YELLOW
        rainbow[4] = Color.argb(255, 255, 165, 0)
        rainbow[3] = Color.RED
        rainbow[2] = Color.argb(255, 148, 0, 211)
        rainbow[1] = Color.argb(255, 128, 0, 0)
        rainbow[0] = Color.WHITE
        return rainbow
    }

    private fun generateBlack(): IntArray {
        val rainbow = IntArray(RainbowHat.LEDSTRIP_LENGTH)
        for (i in rainbow.indices) {
            rainbow[i] = Color.BLACK
        }
        return rainbow
    }

    override fun updateDisplay(value: Double) {
        try {
            display.display(value)
        } catch (e: IOException) {
            log.e(e, "$TAG Error setting display")
        }
    }

    override fun updateDisplay(value: String) {
        try {
            display.display(value)
        } catch (e: IOException) {
            log.e(e, "$TAG Error setting display")
        }
    }

    override fun setRating(value: Int) {
        if (value == 0) {
            ledStrip.write(generateBlack())
        }
        if (value < RainbowHat.LEDSTRIP_LENGTH) {
            val colors = generateRainbow()
            for (i in 0 until RainbowHat.LEDSTRIP_LENGTH - value) {
                colors[i] = Color.BLACK
            }
            ledStrip.write(colors)
        }
    }

    override fun setBrightness(isNight: Boolean) {
        display.setBrightness(
                if (isNight) 1
                else Ht16k33.HT16K33_BRIGHTNESS_MAX
        )
        ledStrip.brightness = if (isNight) 1 else 2
    }

    override fun stop() {
        try {
            display.clear()
            display.setEnabled(false)
            display.close()
            ledStrip.brightness = 0
            ledStrip.close()
        } catch (e: IOException) {
            log.e(e, "$TAG Error disabling display")
        }
    }
}