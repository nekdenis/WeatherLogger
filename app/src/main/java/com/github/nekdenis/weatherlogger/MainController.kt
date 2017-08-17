package com.github.nekdenis.weatherlogger

import android.content.Context
import com.github.nekdenis.weatherlogger.devices.AirConditioner
import com.github.nekdenis.weatherlogger.devices.Buttons
import com.github.nekdenis.weatherlogger.devices.Buzzer
import com.github.nekdenis.weatherlogger.logic.ClimateController
import com.github.nekdenis.weatherlogger.logic.IndicatorController
import com.github.nekdenis.weatherlogger.messaging.server.MessageServer
import com.github.nekdenis.weatherlogger.sensors.WeatherRepo
import com.github.nekdenis.weatherlogger.utils.Logger
import com.github.nekdenis.weatherlogger.utils.dbProvider


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
        val indicatorController: IndicatorController,
        val buttons: Buttons,
        val buzzer: Buzzer,
        val log: Logger
) : MainController {

    override fun start(context: Context) {
        messageServer.start()
        airConditioner.init({
            log.d("$TAG airConditioner initialized")
            climateController.setCallback(airConditioner)
            weatherRepo.addListener(onWeatherUpdate = { climateController.onNewReading(it) })
            weatherRepo.addListener(onWeatherUpdate = { indicatorController.onNewReading(it) })
            buttons.setButtonAListener { decreaseBoundaryTemp() }
            buttons.setButtonBListener { increaseBoundaryTemp() }
            weatherRepo.start()
        }, { conditionerControllerIsAlive ->
            controllerWatchdogCallback(conditionerControllerIsAlive)
        })
    }

    private fun controllerWatchdogCallback(conditionerControllerIsAlive: Boolean) {
        if (conditionerControllerIsAlive) {
            buzzer.stopBeeping()
        } else {
            log.e(message = "$TAG conditionerControllerIsAlive = $conditionerControllerIsAlive")
            buzzer.startBeeping()
        }
    }

    private fun increaseBoundaryTemp() {
        dbProvider().saveBoundaryTemperature(dbProvider().pullBoundaryTemperature() + 1)
        weatherRepo.forceUpdate()
    }

    private fun decreaseBoundaryTemp() {
        dbProvider().saveBoundaryTemperature(dbProvider().pullBoundaryTemperature() - 1)
        weatherRepo.forceUpdate()
    }

    override fun stop() {
        weatherRepo.stop()
        messageServer.stop()
        climateController.removeCallback()
        airConditioner.stop()
        buzzer.stop()
    }
}
