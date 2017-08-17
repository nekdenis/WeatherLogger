package com.github.nekdenis.weatherlogger.sensors

import android.os.Handler
import android.os.Looper
import com.github.nekdenis.weatherlogger.REFRESH_SENSORS_INTERVAL
import com.github.nekdenis.weatherlogger.db.DBProvider
import com.github.nekdenis.weatherlogger.model.WeatherModel
import com.github.nekdenis.weatherlogger.utils.Logger
import com.github.nekdenis.weatherlogger.utils.TimeProvider


interface WeatherRepo {
    fun addListener(onWeatherUpdate: (WeatherModel) -> Unit)
    fun start()
    fun forceUpdate()
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
    private val handler: Handler = Handler(Looper.getMainLooper())
    private lateinit var lastRunnable: Runnable

    private var weatherUpdateListeners: MutableList<((WeatherModel) -> Unit)> = mutableListOf()

    override fun addListener(onWeatherUpdate: (WeatherModel) -> Unit) {
        weatherUpdateListeners.add(onWeatherUpdate)
    }

    override fun start() {
        weatherProvider.start()

        startRetriever()
    }

    override fun forceUpdate() {
        cancelReadings()
        startRetriever()
    }

    private fun startRetriever() {
        val temperature = weatherProvider.temperature()
        val humidity = weatherProvider.humidity()
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
        weatherUpdateListeners.forEach { it.invoke(weather) }
    }

    private fun retrieveData() {
        lastRunnable = Runnable { startRetriever() }
        handler.postDelayed(lastRunnable, REFRESH_SENSORS_INTERVAL)
    }

    private fun cancelReadings() {
        handler.removeCallbacks(lastRunnable)
    }

    override fun stop() {
        weatherProvider.stop()
        weatherUpdateListeners.clear()
        cancelReadings()
    }

}