package com.github.nekdenis.weatherlogger.db.firebase

import com.github.nekdenis.weatherlogger.core.db.KeyValueRepo
import com.github.nekdenis.weatherlogger.core.db.SingleValueRepo
import com.github.nekdenis.weatherlogger.core.firebase.FirebaseKeyValueRepo
import com.github.nekdenis.weatherlogger.core.firebase.FirebaseRootReferenceHolder
import com.github.nekdenis.weatherlogger.core.firebase.FirebaseSingleValueRepo
import com.github.nekdenis.weatherlogger.core.firebase.RxFirebaseDatabase
import com.github.nekdenis.weatherlogger.db.ConditionerConfigRepo
import com.github.nekdenis.weatherlogger.db.WeatherModelRepo
import com.github.nekdenis.weatherlogger.model.ConditionerConfig
import com.github.nekdenis.weatherlogger.model.WeatherModel
import com.google.firebase.database.FirebaseDatabase

private const val FB_PATH_CLIMATE_READINGS = "climate_readings"
private const val FB_PATH_CONFIG = "config"

class ConditionerConfigFirebaseRepo(
        db: FirebaseDatabase,
        rootRefHolder: FirebaseRootReferenceHolder,
        rxFirebaseDatabase: RxFirebaseDatabase
) : ConditionerConfigRepo, SingleValueRepo<ConditionerConfig> by FirebaseSingleValueRepo(db, FB_PATH_CONFIG, rootRefHolder, rxFirebaseDatabase)

class WeatherModelFirebaseRepo(
        db: FirebaseDatabase,
        rootRefHolder: FirebaseRootReferenceHolder,
        rxFirebaseDatabase: RxFirebaseDatabase
) : WeatherModelRepo, KeyValueRepo<String, WeatherModel> by FirebaseKeyValueRepo(db, FB_PATH_CLIMATE_READINGS, rootRefHolder, rxFirebaseDatabase)

