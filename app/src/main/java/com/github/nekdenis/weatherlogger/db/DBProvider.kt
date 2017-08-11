package com.github.nekdenis.weatherlogger.db

import com.github.nekdenis.weatherlogger.FIREBASE_ROOT
import com.github.nekdenis.weatherlogger.model.WeatherModel
import com.github.nekdenis.weatherlogger.utils.Logger
import com.github.nekdenis.weatherlogger.utils.TimeProvider
import com.google.firebase.database.DatabaseReference

interface DBProvider {
    fun saveTemperatureValue(data: WeatherModel)
}

class DBProviderImpl(
        private val firebase: DatabaseReference,
        private val timeProvider: TimeProvider,
        private val log: Logger) : DBProvider {

    override fun saveTemperatureValue(data: WeatherModel) {
        log.d("saving: $data")
        firebase.child(getDBRoot()).child(getKey(data.timeStamp)).child(timeProvider.timeFormatted(data.timeStamp))
                .setValue(data)
                .addOnFailureListener { log.e(it, "error saving weatherProvider") }
                .addOnSuccessListener { log.d("saved weatherProvider") }
    }

    private fun getDBRoot() = FIREBASE_ROOT
    private fun getKey(timeStamp: Long) = timeProvider.dayFormatted(timeStamp)
}
