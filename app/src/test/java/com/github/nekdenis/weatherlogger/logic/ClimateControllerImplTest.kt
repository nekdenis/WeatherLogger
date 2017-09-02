package com.github.nekdenis.weatherlogger.logic

import com.github.nekdenis.weatherlogger.REFRESH_SENSORS_INTERVAL
import com.github.nekdenis.weatherlogger.db.ConditionerConfigRepo
import com.github.nekdenis.weatherlogger.devices.AirConditioner
import com.github.nekdenis.weatherlogger.model.ConditionerConfig
import com.github.nekdenis.weatherlogger.model.WeatherModel
import com.github.nekdenis.weatherlogger.sensors.WeatherProvider
import com.github.nekdenis.weatherlogger.test.CompositeDisposableHolderTestImpl
import com.stepango.koptional.Optional
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.BehaviorSubject
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class ClimateControllerImplTest {

    var conditionerState = -1

    val weatherSubject = BehaviorSubject.create<WeatherModel>()

    val weatherProvider = object : WeatherProvider {
        override fun observeWeather(): Observable<WeatherModel> = weatherSubject
    }

    val conRepo = object : ConditionerConfigRepo {
        override fun save(value: ConditionerConfig): Single<ConditionerConfig> {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun remove(): Completable {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun observe(): Observable<Optional<ConditionerConfig>> = Observable.just(Optional.of(ConditionerConfig()))

    }

    val airConditioner = object : AirConditioner {
        override var composite: CompositeDisposable
            get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
            set(value) {}

        override fun onStart() {
        }

        override fun Disposable.bind() {
        }

        override var watchdogListener: (alive: Boolean) -> Unit
            get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
            set(value) {}

        override fun turnOnConditioner(): Completable = Completable.complete().doOnComplete { conditionerState = 1 }
        override fun turnOffConditioner(): Completable = Completable.complete().doOnComplete { conditionerState = 0 }

    }
    val controller = ClimateControllerImpl(weatherProvider, conRepo, airConditioner, CompositeDisposableHolderTestImpl())


    @Before
    fun setUp() {
        controller.onStart()
    }

    @Test
    fun shouldNotReactOnFirstReading() {
        weatherSubject.onNext(WeatherModel(30.0, 30.0, 0))
        Assert.assertEquals(-1, conditionerState)
    }

    @Test
    fun shouldNotReactOnSecondCloseReading() {
        weatherSubject.onNext(WeatherModel(30.0, 30.0, 0))
        weatherSubject.onNext(WeatherModel(30.0, 30.0, 10))
        Assert.assertEquals(-1, conditionerState)
    }

    @Test
    fun shouldTurnOn() {
        weatherSubject.onNext(WeatherModel(30.0, 30.0, 0))
        weatherSubject.onNext(WeatherModel(30.0, 30.0, REFRESH_SENSORS_INTERVAL * 1))
        weatherSubject.onNext(WeatherModel(30.0, 30.0, REFRESH_SENSORS_INTERVAL * 2))
        weatherSubject.onNext(WeatherModel(30.0, 30.0, REFRESH_SENSORS_INTERVAL * 3))
        weatherSubject.onNext(WeatherModel(30.0, 30.0, REFRESH_SENSORS_INTERVAL * 4))
        weatherSubject.onNext(WeatherModel(30.0, 30.0, REFRESH_SENSORS_INTERVAL * 5))
        Assert.assertEquals(1, conditionerState)
    }

    @Test
    fun shouldTurnOff() {
        weatherSubject.onNext(WeatherModel(25.0, 30.0, 0))
        weatherSubject.onNext(WeatherModel(25.0, 30.0, REFRESH_SENSORS_INTERVAL * 1))
        weatherSubject.onNext(WeatherModel(25.0, 30.0, REFRESH_SENSORS_INTERVAL * 2))
        weatherSubject.onNext(WeatherModel(25.0, 30.0, REFRESH_SENSORS_INTERVAL * 3))
        weatherSubject.onNext(WeatherModel(25.0, 30.0, REFRESH_SENSORS_INTERVAL * 4))
        weatherSubject.onNext(WeatherModel(25.0, 30.0, REFRESH_SENSORS_INTERVAL * 5))
        weatherSubject.onNext(WeatherModel(25.0, 30.0, REFRESH_SENSORS_INTERVAL * 5))
        weatherSubject.onNext(WeatherModel(25.0, 30.0, REFRESH_SENSORS_INTERVAL * 5))
        weatherSubject.onNext(WeatherModel(25.0, 30.0, REFRESH_SENSORS_INTERVAL * 5))
        weatherSubject.onNext(WeatherModel(25.0, 30.0, REFRESH_SENSORS_INTERVAL * 5))
        Assert.assertEquals(0, conditionerState)
    }

    @Test
    fun shouldTurnOffDespiteWarmReadings() {
        weatherSubject.onNext(WeatherModel(24.0, 30.0, 0))
        weatherSubject.onNext(WeatherModel(24.0, 30.0, REFRESH_SENSORS_INTERVAL * 1))
        weatherSubject.onNext(WeatherModel(24.0, 30.0, REFRESH_SENSORS_INTERVAL * 2))
        weatherSubject.onNext(WeatherModel(25.0, 30.0, REFRESH_SENSORS_INTERVAL * 3))
        weatherSubject.onNext(WeatherModel(28.0, 30.0, REFRESH_SENSORS_INTERVAL * 4))
        weatherSubject.onNext(WeatherModel(28.0, 30.0, REFRESH_SENSORS_INTERVAL * 5))
        Assert.assertEquals(0, conditionerState)
    }
}

