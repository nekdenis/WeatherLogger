package com.github.nekdenis.weatherlogger.logic

import com.github.nekdenis.weatherlogger.REFRESH_SENSORS_INTERVAL
import com.github.nekdenis.weatherlogger.TIME_INTERVAL_FOR_AVERAGE_VALUE
import com.github.nekdenis.weatherlogger.db.DBProvider
import com.github.nekdenis.weatherlogger.model.WeatherModel


interface ClimateController {

    fun onNewReading(weather: WeatherModel)
    fun setCallback(climateControllerCallback: ClimateControllerCallback)
    fun removeCallback()
}

interface ClimateControllerCallback {
    fun turnOnConditioner()
    fun turnOffConditioner()
}


class ClimateControllerImpl(
        val dbProvider: DBProvider
) : ClimateController {
    private val UNDEFINED = Double.MIN_VALUE
    private val queueSize: Int = (TIME_INTERVAL_FOR_AVERAGE_VALUE / REFRESH_SENSORS_INTERVAL).toInt()

    private val lastReadings: LimitedSizeQueue<WeatherModel> = LimitedSizeQueue(queueSize)

    private var mediumTemp: Double = UNDEFINED
        get() = if (lastReadings.size == queueSize) mediumTemp(lastReadings) else UNDEFINED

    private var callback: ClimateControllerCallback? = null

    override fun setCallback(climateControllerCallback: ClimateControllerCallback) {
        callback = climateControllerCallback
    }

    override fun removeCallback() {
        callback = null
    }

    override fun onNewReading(weather: WeatherModel) {
        lastReadings.add(weather)
        callback?.let { makeDecision(mediumTemp, it) }
    }

    private fun makeDecision(temp: Double, callback: ClimateControllerCallback) {
        if (temp == UNDEFINED) return
        if (temp < dbProvider.pullBoundaryTemperature()) callback.turnOffConditioner()
        else callback.turnOnConditioner()
    }

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

