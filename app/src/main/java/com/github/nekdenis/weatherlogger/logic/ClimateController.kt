package com.github.nekdenis.weatherlogger.logic

import com.github.nekdenis.weatherlogger.REFRESH_SENSORS_INTERVAL
import com.github.nekdenis.weatherlogger.TIME_INTERVAL_FOR_AVERAGE_VALUE
import com.github.nekdenis.weatherlogger.core.rx.CompositeDisposableHolder
import com.github.nekdenis.weatherlogger.core.system.LCRX
import com.github.nekdenis.weatherlogger.db.ConditionerConfigRepo
import com.github.nekdenis.weatherlogger.devices.AirConditioner
import com.github.nekdenis.weatherlogger.model.CONFIG_MODE_AUTO
import com.github.nekdenis.weatherlogger.model.CONFIG_MODE_ON
import com.github.nekdenis.weatherlogger.model.ConditionerConfig
import com.github.nekdenis.weatherlogger.model.WeatherModel
import com.github.nekdenis.weatherlogger.sensors.WeatherProvider
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.functions.BiFunction


interface ClimateController : LCRX {
}

class ClimateControllerImpl(
        val weatherProvider: WeatherProvider,
        val conditionerConfigRepo: ConditionerConfigRepo,
        val airConditioner: AirConditioner,
        val compositeDisposableHolder: CompositeDisposableHolder
) : ClimateController, CompositeDisposableHolder by compositeDisposableHolder {

    private val UNDEFINED = Double.MIN_VALUE
    private val queueSize: Int = (TIME_INTERVAL_FOR_AVERAGE_VALUE / REFRESH_SENSORS_INTERVAL).toInt()

    private val lastReadings: LimitedSizeQueue<WeatherModel> = LimitedSizeQueue(queueSize)

    private var mediumTemp: Double = UNDEFINED
        get() = if (lastReadings.size == queueSize) mediumTemp(lastReadings) else UNDEFINED

    override fun onStart() {
        Observable.combineLatest(
                conditionerConfigRepo.observeOrDefault(),
                weatherProvider.observeWeather(),
                BiFunction<ConditionerConfig, WeatherModel, Pair<ConditionerConfig, WeatherModel>>(::Pair))
                .doOnNext { lastReadings.add(it.second) }
                .flatMapCompletable { processIncomingValues(it.first) }
                .subscribe()
                .bind()
    }

    private fun processIncomingValues(config: ConditionerConfig): Completable = when (config.mode) {
        CONFIG_MODE_AUTO -> makeDecision(mediumTemp, config.boundaryTemp)
        CONFIG_MODE_ON   -> turnOn()
        else             -> turnOff()
    }

    private fun makeDecision(temp: Double, boundaryTemp: Double) =
            if (temp == UNDEFINED) Completable.complete()
            else if (temp < boundaryTemp) turnOff()
            else turnOn()

    private fun turnOn() = airConditioner.turnOnConditioner()

    private fun turnOff() = airConditioner.turnOffConditioner()

    private fun mediumTemp(temps: Collection<WeatherModel>) = temps.run {
        var sum = 0.0
        val iter = iterator()
        while (iter.hasNext()) {
            sum += iter.next().temperature
        }
        sum / size
    }
}

class LimitedSizeQueue<K>(private val maxSize: Int) : ArrayList<K>() {

    override fun add(k: K): Boolean {
        val r = super.add(k)
        if (size > maxSize) {
            removeRange(0, size - maxSize)
        }
        return r
    }

    val yongest: K
        get() = get(size - 1)

    val oldest: K
        get() = get(0)
}

