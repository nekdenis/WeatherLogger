package com.github.nekdenis.weatherlogger

import android.content.Context
import com.github.nekdenis.weatherlogger.devices.AirConditioner
import com.github.nekdenis.weatherlogger.logic.ClimateController
import com.github.nekdenis.weatherlogger.messaging.server.MessageServer
import com.github.nekdenis.weatherlogger.sensors.WeatherRepo
import com.github.nekdenis.weatherlogger.utils.Logger


private val TAG = "MAIN_CONTROLLER::"

interface MainController {
    fun start(context: Context)
    fun stop()
}

class MainControllerImpl(
        val weatherRepo: WeatherRepo,
        val messageServer: MessageServer,
        val airConditioner: AirConditioner,
        val climateController: ClimateController,
        val log: Logger
) : MainController {

    override fun start(context: Context) {
        messageServer.start()
        airConditioner.init {
            log.d("$TAG airConditioner initialized")
            climateController.setCallback(airConditioner)
            weatherRepo.setListener(climateController::onNewReading)
            weatherRepo.start()
        }
    }

    override fun stop() {
        weatherRepo.stop()
        messageServer.stop()
        climateController.removeCallback()
    }
}
