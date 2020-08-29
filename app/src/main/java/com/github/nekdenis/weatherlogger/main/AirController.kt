package com.github.nekdenis.weatherlogger.main

import com.github.nekdenis.weatherlogger.core.rx.CompositeDisposableHolder
import com.github.nekdenis.weatherlogger.core.system.Logger
import com.github.nekdenis.weatherlogger.devices.Buttons
import com.github.nekdenis.weatherlogger.devices.Buzzer
import com.github.nekdenis.weatherlogger.logic.IndicatorController
import com.github.nekdenis.weatherlogger.logic.Watchdog
import com.github.nekdenis.weatherlogger.model.WEATHER_DATA
import com.github.nekdenis.weatherlogger.sensors.WeatherProvider
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.BehaviorSubject

/**
 * implementation just to show air quality, temperature and humidity from purple air
 */

private val TAG = "AIR_CONTROLLER::"

class AirControllerImpl(
        val purpleAirProvider: WeatherProvider,
        val indicatorController: IndicatorController,
        val buttons: Buttons,
        val buzzer: Buzzer,
        val log: Logger,
        val compositeDisposableHolder: CompositeDisposableHolder,
        val purpleAirWatchdog: Watchdog
) : MainController, CompositeDisposableHolder by compositeDisposableHolder {

    var systemDisposables = CompositeDisposable()

    private val weatherDataTypeRelay = BehaviorSubject.createDefault(WEATHER_DATA.AQI)

    override fun onStart() {
        log.d("$TAG starting")
        turnOnSystem()

        buttons.setButtonAListener { show(WEATHER_DATA.AQI) }
        buttons.setButtonBListener { show(WEATHER_DATA.TEMPERATURE) }
        buttons.setButtonCListener { show(WEATHER_DATA.HUMIDITY) }
    }


    private fun turnOnSystem() {
        log.d("$TAG turning on system")
        Observable.combineLatest(
                weatherDataTypeRelay,
                purpleAirProvider.observeWeather(),
                { data, weather -> weather to data })
                .doOnNext { (weather, dataType) -> indicatorController.onNewReadingWithAqi(weather, dataType) }
                .subscribe().bindToSystem()
    }

    private fun shutDownSystem() {
        log.d("$TAG shutting down system")
        systemDisposables.clear()
    }

    private fun show(dataType: WEATHER_DATA) {
        weatherDataTypeRelay.onNext(dataType)
    }

    override fun onStop() {
        shutDownSystem()
        super.onStop()
        buzzer.stop()
    }

    fun Disposable.bindToSystem() {
        systemDisposables.add(this)
    }
}
