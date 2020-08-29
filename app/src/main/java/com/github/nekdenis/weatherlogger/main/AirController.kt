package com.github.nekdenis.weatherlogger.main

import com.github.nekdenis.weatherlogger.MQTT_CLIENT_NAME
import com.github.nekdenis.weatherlogger.MQTT_SERVER_URL
import com.github.nekdenis.weatherlogger.MQTT_WATCHDOG_TIMEOUT
import com.github.nekdenis.weatherlogger.core.rx.CompositeDisposableHolder
import com.github.nekdenis.weatherlogger.core.system.LCRX
import com.github.nekdenis.weatherlogger.core.system.Logger
import com.github.nekdenis.weatherlogger.db.ConditionerConfigRepo
import com.github.nekdenis.weatherlogger.devices.Buttons
import com.github.nekdenis.weatherlogger.devices.Buzzer
import com.github.nekdenis.weatherlogger.logic.ClimateController
import com.github.nekdenis.weatherlogger.logic.IndicatorController
import com.github.nekdenis.weatherlogger.logic.Watchdog
import com.github.nekdenis.weatherlogger.messaging.client.MessageClient
import com.github.nekdenis.weatherlogger.messaging.server.MessageServer
import com.github.nekdenis.weatherlogger.model.ConditionerConfig
import com.github.nekdenis.weatherlogger.model.WeatherModel
import com.github.nekdenis.weatherlogger.sensors.WeatherProvider
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.functions.BiFunction


private val TAG = "MAIN_CONTROLLER::"

interface MainController : LCRX

class MainControllerImpl(
        val weatherProvider: WeatherProvider,
        val messageServer: MessageServer,
        val messageClient: MessageClient,
        val climateController: ClimateController,
        val indicatorController: IndicatorController,
        val buttons: Buttons,
        val buzzer: Buzzer,
        val log: Logger,
        val compositeDisposableHolder: CompositeDisposableHolder,
        val airConditionerWatchdog: Watchdog,
        val temperatureWatchdog: Watchdog,
        val conditionerConfigRepo: ConditionerConfigRepo
) : MainController, CompositeDisposableHolder by compositeDisposableHolder {

    var systemDisposables = CompositeDisposable()

    override fun onStart() {
        log.d("$TAG starting")
        messageServer.onStart()
        messageClient.configClient(MQTT_SERVER_URL, MQTT_CLIENT_NAME)
        messageClient.observeConnection()
                .doOnNext { connected -> if (connected) turnOnSystem() else shutDownSystem() }
                .subscribe({ log.d("$TAG messageClient initialized") }, { onSystemError(it) })
                .bind()

        buttons.setButtonAListener { decreaseBoundaryTemp() }
        buttons.setButtonBListener { increaseBoundaryTemp() }
    }


    private fun turnOnSystem() {
        log.d("$TAG turning on system")
        initAirConditioner()
        Observable.combineLatest(
                conditionerConfigRepo.observeOrDefault(),
                weatherProvider.observeWeather(),
                BiFunction<ConditionerConfig, WeatherModel, Pair<ConditionerConfig, WeatherModel>> { config, weather -> config to weather }
        )
                .doOnNext { (config, weather) -> indicatorController.onNewReading(weather, config) }
                .subscribe().bindToSystem()
    }

    private fun initAirConditioner() {
        airConditionerWatchdog.configure(
                checkInterval = MQTT_WATCHDOG_TIMEOUT / 2,
                errorInterval = MQTT_WATCHDOG_TIMEOUT,
                onWorking = { buzzer.stopBeeping() },
                onStopped = {
                    log.e(message = "${TAG} conditionerControllerIsAlive = false")
                    buzzer.startBeeping()
                }
        )
        temperatureWatchdog.configure(
                checkInterval = MQTT_WATCHDOG_TIMEOUT / 2,
                errorInterval = MQTT_WATCHDOG_TIMEOUT,
                onWorking = { indicatorController.onTemperatureErrorSolved() },
                onStopped = {
                    log.e(message = "${TAG} temperatureProviderIsAlive = false")
                    indicatorController.onTemperatureError()
                }
        )
        climateController.onStart()
        log.d("${TAG} airConditioner initialized")
    }

    private fun shutDownSystem() {
        log.d("$TAG shutting down system")
        climateController.onStop()
        systemDisposables.clear()
    }

    private fun increaseBoundaryTemp() {
        conditionerConfigRepo.saveNewBoundaryTemperature { temperature -> temperature + 1 }
                .subscribe()
                .bindToSystem()
    }

    private fun decreaseBoundaryTemp() {
        conditionerConfigRepo.saveNewBoundaryTemperature { temperature -> temperature - 1 }
                .subscribe()
                .bindToSystem()
    }

    override fun onStop() {
        shutDownSystem()
        messageServer.onStop()
        super.onStop()
        buzzer.stop()
    }

    private fun onSystemError(it: Throwable) {
        log.e(it, "$TAG messageClient error")
        indicatorController.onSystemError()
    }

    fun Disposable.bindToSystem() {
        systemDisposables.add(this)
    }
}
