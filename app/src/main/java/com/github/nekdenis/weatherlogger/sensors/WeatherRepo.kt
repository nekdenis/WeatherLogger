package com.github.nekdenis.weatherlogger.sensors

import android.os.Handler
import android.os.Looper
import com.github.nekdenis.weatherlogger.REFRESH_SENSORS_INTERVAL
import com.github.nekdenis.weatherlogger.db.DBProvider
import com.github.nekdenis.weatherlogger.model.WeatherModel
import com.github.nekdenis.weatherlogger.utils.Logger
import com.github.nekdenis.weatherlogger.utils.TimeProvider


interface WeatherRepo {
    fun setListener(onWeatherUpdate: (weather: WeatherModel) -> Unit)
    fun start()
    fun stop()
}

private const val TAG = "WEATHER_REPO::"

interface WeatherUpdateListener {
    fun onWeatherUpdate(weather: WeatherModel)
}

class WeatherRepoImpl(
        val weatherProvider: TemperatureProvider,
        val db: DBProvider,
        val timeProvider: TimeProvider,
        val log: Logger
) : WeatherRepo {

    val handler: Handler = Handler(Looper.getMainLooper())

    var weatherUpdateListener: ((weather: WeatherModel) -> Unit)? = null

    override fun setListener(onWeatherUpdate: (weather: WeatherModel) -> Unit) {
        weatherUpdateListener = onWeatherUpdate
    }

    override fun start() {
        weatherProvider.start()

        startRetriever()
    }

    private fun startRetriever() {
        val temperature = weatherProvider.temperature()
        val humidity = 0.0//weatherProvider.humidity()
        log.d("$TAG t=$temperature, h=$humidity")
        if (temperature != null && humidity != null) {
            WeatherModel(temperature, humidity, timeProvider.nowMillis()).let {
                db.saveTemperatureValue(it)
                notifyListeners(it)
            }
        }
        retrieveData()
    }

    private fun notifyListeners(weather: WeatherModel) {
        weatherUpdateListener?.invoke(weather)
    }


    private fun retrieveData() {
        handler.postDelayed(this::startRetriever, REFRESH_SENSORS_INTERVAL)
    }


    override fun stop() {
        weatherProvider.stop()
        weatherUpdateListener = null
    }

}