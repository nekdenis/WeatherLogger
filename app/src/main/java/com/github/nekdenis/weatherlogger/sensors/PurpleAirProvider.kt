package com.github.nekdenis.weatherlogger.sensors

import com.github.nekdenis.weatherlogger.REFRESH_PURPLE_AIR_INTERVAL
import com.github.nekdenis.weatherlogger.core.system.Logger
import com.github.nekdenis.weatherlogger.logic.Watchdog
import com.github.nekdenis.weatherlogger.model.RatedDouble
import com.github.nekdenis.weatherlogger.model.WeatherModel
import com.google.gson.annotations.SerializedName
import com.thanglequoc.aqicalculator.AQICalculator
import com.thanglequoc.aqicalculator.Pollutant
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import java.util.concurrent.TimeUnit


private const val TAG = "PURPLE_AIR_REPO::"

class PurpleAirProviderImpl(
        val log: Logger,
        val watchdog: Watchdog,
        val aqiCalculator: AQICalculator
) : WeatherProvider {

    //TODO: inject retrofit
    private val retrofit = Retrofit.Builder()
            .baseUrl("https://www.purpleair.com/")
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    private val service: PurpleAirApi = retrofit.create(PurpleAirApi::class.java)

    private val weatherObservable = retrieveData()

    override fun observeWeather(): Observable<WeatherModel> = weatherObservable

    private fun retrieveData(): Observable<WeatherModel> =
            service.getSensorData()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .map(this::parseResponse)
                    .doOnError { log.e(it, "$TAG error fetching purple air") }
                    .onErrorReturn { emptyWeather() }
                    .repeatWhen { completed -> completed.delay(REFRESH_PURPLE_AIR_INTERVAL, TimeUnit.MILLISECONDS) }.toObservable()
                    .doOnNext { pingReceived() }
                    .doOnNext { log.d("$TAG t=${it.temperature}, h=${it.humidity}, aqi = ${it.airQualityIndex}") }

    private fun pingReceived() {
        watchdog.onEvent()
    }

    private fun parseResponse(response: PurpleAirSensorResponse) =
            response.results.getOrNull(0)
                    .let {
                        WeatherModel(
                                temperature = it?.temp?.toDouble()
                                        ?: UNKNOWN_TEMPERATURE,
                                humidity = it?.humid?.toDouble() ?: UNKNOWN_HUMIDITY,
                                airQualityIndex = it?.aqi?.toDouble()?.let(::pm2_5ToAQI)
                                        ?: UNKNOWN_AIR_QUALITY,
                                timeStamp = (it?.timestamp ?: 0) * 1000L
                        )
                    }

    private fun emptyWeather() = WeatherModel()


    private fun pm2_5ToAQI(pm25: Double): RatedDouble = aqiCalculator.getAQI(Pollutant.PM25, pm25).let {
        RatedDouble(
                value = it.aqi.toDouble(),
                rating = when (it.category) {
                    "Good" -> 1
                    "Moderate" -> 2
                    "Unhealthy for Sensitive Groups" -> 3
                    "Unhealthy" -> 4
                    "Very Unhealthy" -> 5
                    "Hazardous" -> 6
                    else -> 7
                })
    }
}

private data class PurpleAirSensorResponse(
        @SerializedName("results")
        val results: List<Sensor>,
)

private data class Sensor(
        @SerializedName("PM2_5Value")
        val aqi: Float?,
        @SerializedName("temp_f")
        val temp: Int?,
        @SerializedName("humidity")
        val humid: Int?,
        @SerializedName("LastUpdateCheck")
        val timestamp: Int?
)

private interface PurpleAirApi {
    @GET("/json?show=18471")
    fun getSensorData(): Single<PurpleAirSensorResponse>
}