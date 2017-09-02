package com.github.nekdenis.weatherlogger.db

import com.github.nekdenis.weatherlogger.core.db.KeyValueRepo
import com.github.nekdenis.weatherlogger.core.db.SingleValueRepo
import com.github.nekdenis.weatherlogger.core.system.TimeProvider
import com.github.nekdenis.weatherlogger.model.ConditionerConfig
import com.github.nekdenis.weatherlogger.model.WeatherModel


interface ConditionerConfigRepo : SingleValueRepo<ConditionerConfig> {
    fun observeOrDefault() = observe()
            .map {
                if (it.isPresent) it.get()
                else ConditionerConfig()
            }

    fun sigleOrDefault() = observeOrDefault().firstOrError()

    fun saveNewBoundaryTemperature(modifier: (temp: Double) -> Double) = sigleOrDefault()
            .map { it.copy(boundaryTemp = modifier(it.boundaryTemp)) }
            .flatMap(this::save)
}


interface WeatherModelRepo : KeyValueRepo<String, WeatherModel> {
    fun saveTemperatureValue(data: WeatherModel, timeProvider: TimeProvider) = data.run {
        save(
                subkey = timeProvider.dayFormatted(timeStamp),
                key = timeProvider.dateTimeFormatted(timeStamp),
                value = this
        )
    }
}