package com.github.nekdenis.weatherlogger.model

import com.github.nekdenis.weatherlogger.sensors.UNKNOWN_AIR_QUALITY
import com.github.nekdenis.weatherlogger.sensors.UNKNOWN_HUMIDITY
import com.github.nekdenis.weatherlogger.sensors.UNKNOWN_TEMPERATURE

enum class WEATHER_DATA {
    TEMPERATURE,
    HUMIDITY,
    AQI
}

data class WeatherModel(
        val temperature: Double = UNKNOWN_TEMPERATURE,
        val humidity: Double = UNKNOWN_HUMIDITY,
        val timeStamp: Long = 0L,
        val airQualityIndex: RatedDouble = UNKNOWN_AIR_QUALITY
)

data class RatedDouble(
        val value: Double,
        val rating: Int
)