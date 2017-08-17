package com.github.nekdenis.weatherlogger.devices

import com.google.android.things.contrib.driver.rainbowhat.RainbowHat


interface Leds {
    fun turnOnRed(enable: Boolean)
    fun turnOnBlue(enable: Boolean)
    fun turnOnGreen(enable: Boolean)
}

class LedsImpl : Leds {

    val red = RainbowHat.openLedRed()
    val blue = RainbowHat.openLedBlue()
    val green = RainbowHat.openLedGreen()

    override fun turnOnRed(enable: Boolean) {
        red.value = enable
    }

    override fun turnOnBlue(enable: Boolean) {
        blue.value = enable
    }

    override fun turnOnGreen(enable: Boolean) {
        green.value = enable
    }
}