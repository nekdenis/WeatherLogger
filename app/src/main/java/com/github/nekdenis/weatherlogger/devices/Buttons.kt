package com.github.nekdenis.weatherlogger.devices

import com.github.nekdenis.weatherlogger.core.system.Logger
import com.google.android.things.contrib.driver.button.Button
import com.google.android.things.contrib.driver.rainbowhat.RainbowHat


interface Buttons {

    fun setButtonAListener(listener: () -> Unit)
    fun setButtonBListener(listener: () -> Unit)
    fun setButtonCListener(listener: () -> Unit)
    fun stop()
}

private const val TAG = "BUTTONS::"

class ButtonsImpl(
        val log: Logger
) : Buttons {
    private val buttonA: Button = RainbowHat.openButtonA()
    private val buttonB: Button = RainbowHat.openButtonB()
    private val buttonC: Button = RainbowHat.openButtonC()
    private var buttonAListener: (() -> Unit)? = null
    private var buttonBListener: (() -> Unit)? = null
    private var buttonCListener: (() -> Unit)? = null

    init {
        buttonA.setOnButtonEventListener { _: Button, pressed: Boolean ->
            log.d("$TAG button A pressed:" + pressed)
            if (pressed) buttonAListener?.invoke()
        }
        buttonB.setOnButtonEventListener { _: Button, pressed: Boolean ->
            log.d("$TAG button B pressed:" + pressed)
            if (pressed) buttonBListener?.invoke()
        }
        buttonC.setOnButtonEventListener { _: Button, pressed: Boolean ->
            log.d("$TAG button C pressed:" + pressed)
            if (pressed) buttonCListener?.invoke()
        }
    }

    override fun setButtonAListener(listener: () -> Unit) {
        buttonAListener = listener
    }

    override fun setButtonBListener(listener: () -> Unit) {
        buttonBListener = listener
    }

    override fun setButtonCListener(listener: () -> Unit) {
        buttonCListener = listener
    }

    override fun stop() {
        buttonA.close()
        buttonB.close()
        buttonC.close()
    }
}