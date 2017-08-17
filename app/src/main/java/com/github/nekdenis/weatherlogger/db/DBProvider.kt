package com.github.nekdenis.weatherlogger.db

import com.github.nekdenis.weatherlogger.DEFAULT_BOUNDARY_TEMPERATURE
import com.github.nekdenis.weatherlogger.FIREBASE_ROOT
import com.github.nekdenis.weatherlogger.model.WeatherModel
import com.github.nekdenis.weatherlogger.utils.Logger
import com.github.nekdenis.weatherlogger.utils.TimeProvider
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener


interface DBProvider {
    fun saveTemperatureValue(data: WeatherModel)
    fun saveBoundaryTemperature(temp: Double)
    fun pullBoundaryTemperature(): Double
}

private const val TAG = "DBProvider::"
private const val FB_PATH_CLIMATE_READINGS = "climate_readings"
private const val FB_PATH_CONFIG = "config"
private const val FB_KEY_BOUNDARY_TEMP = "boundary_temp"

class DBProviderImpl(
        private val firebase: DatabaseReference,
        private val timeProvider: TimeProvider,
        private val log: Logger) : DBProvider {

    private var boundaryTemp: Double = DEFAULT_BOUNDARY_TEMPERATURE

    init {
        initBoundaryTempListener()
    }

    private fun initBoundaryTempListener() {
        firebase.child(getDBRoot()).child(FB_PATH_CONFIG).child(FB_KEY_BOUNDARY_TEMP)
                .addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        boundaryTemp = dataSnapshot.getValue(Double::class.java) ?: DEFAULT_BOUNDARY_TEMPERATURE
                    }

                    override fun onCancelled(error: DatabaseError) {
                        log.e(error.toException(), "$TAG Failed to read value.")
                    }
                })
    }

    private fun DatabaseReference.save(data: Any) = setValue(data)
            .addOnFailureListener { log.e(it, "$TAG error saving weatherProvider") }
            .addOnSuccessListener { log.d("$TAG saved $data") }

    private fun getDBRoot() = FIREBASE_ROOT
    private fun getKey(timeStamp: Long) = timeProvider.dayFormatted(timeStamp)

    override fun saveTemperatureValue(data: WeatherModel) {
        log.d("$TAG saving: $data")
        firebase.child(getDBRoot()).child(FB_PATH_CLIMATE_READINGS).child(getKey(data.timeStamp)).child(timeProvider.timeFormatted(data.timeStamp))
                .save(data)
    }

    override fun saveBoundaryTemperature(temp: Double) {
        boundaryTemp = temp
        firebase.child(getDBRoot()).child(FB_PATH_CONFIG).child(FB_KEY_BOUNDARY_TEMP).save(temp)
    }

    override fun pullBoundaryTemperature(): Double = boundaryTemp

}
