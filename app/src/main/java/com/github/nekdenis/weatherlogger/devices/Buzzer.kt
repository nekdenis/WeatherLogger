package com.github.nekdenis.weatherlogger.devices

import android.os.Handler
import android.os.Looper
import com.google.android.things.contrib.driver.rainbowhat.RainbowHat


interface Buzzer {
    fun startBeeping()
    fun stopBeeping()
    fun stop()
}

class BuzzerImpl : Buzzer {

    val buzzer = RainbowHat.openPiezo()
    val handler = Handler(Looper.getMainLooper())
    var played = false
    var beepingRunnable: Runnable? = null

    override fun startBeeping() {
        beepingRunnable?.let { handler.removeCallbacks(it) }
        beepingRunnable = Runnable { playBeep() }
        handler.postDelayed(beepingRunnable, 400)
    }

    private fun playBeep() {
        if (played) buzzer.stop()
        else buzzer.play(1000.0)
        played = !played
        startBeeping()
    }


    override fun stopBeeping() {
        played = false
        buzzer.stop()
        beepingRunnable?.let { handler.removeCallbacks(it) }
    }

    override fun stop() {
        buzzer.close()
    }

}