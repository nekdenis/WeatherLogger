package com.github.nekdenis.weatherlogger.utils

import com.github.nekdenis.weatherlogger.App
import com.github.nekdenis.weatherlogger.FIREBASE_ROOT
import com.github.nekdenis.weatherlogger.core.firebase.FirebaseRootReferenceHolder
import com.github.nekdenis.weatherlogger.core.firebase.FirebaseRootReferenceHolderImpl
import com.github.nekdenis.weatherlogger.core.firebase.RxFirebaseDatabase
import com.github.nekdenis.weatherlogger.core.network.json.GsonMaker
import com.github.nekdenis.weatherlogger.core.network.json.JsonMaker
import com.github.nekdenis.weatherlogger.core.rx.CompositeDisposableHolderImpl
import com.github.nekdenis.weatherlogger.core.system.DeviceInfo
import com.github.nekdenis.weatherlogger.core.system.DeviceInfoImpl
import com.github.nekdenis.weatherlogger.core.system.Logger
import com.github.nekdenis.weatherlogger.core.system.LoggerImpl
import com.github.nekdenis.weatherlogger.core.system.TimeProvider
import com.github.nekdenis.weatherlogger.core.system.TimeProviderImpl
import com.github.nekdenis.weatherlogger.db.ConditionerConfigRepo
import com.github.nekdenis.weatherlogger.db.firebase.ConditionerConfigFirebaseRepo
import com.github.nekdenis.weatherlogger.db.firebase.WeatherModelFirebaseRepo
import com.github.nekdenis.weatherlogger.devices.AirConditioner
import com.github.nekdenis.weatherlogger.devices.AirConditionerMqtt
import com.github.nekdenis.weatherlogger.devices.Buttons
import com.github.nekdenis.weatherlogger.devices.ButtonsImpl
import com.github.nekdenis.weatherlogger.devices.Buzzer
import com.github.nekdenis.weatherlogger.devices.BuzzerImpl
import com.github.nekdenis.weatherlogger.devices.Display
import com.github.nekdenis.weatherlogger.devices.DisplayImpl
import com.github.nekdenis.weatherlogger.devices.Leds
import com.github.nekdenis.weatherlogger.devices.LedsImpl
import com.github.nekdenis.weatherlogger.logic.ClimateController
import com.github.nekdenis.weatherlogger.logic.ClimateControllerImpl
import com.github.nekdenis.weatherlogger.logic.IndicatorController
import com.github.nekdenis.weatherlogger.logic.IndicatorControllerImpl
import com.github.nekdenis.weatherlogger.logic.WatchdogImpl
import com.github.nekdenis.weatherlogger.main.MainController
import com.github.nekdenis.weatherlogger.main.MainControllerImpl
import com.github.nekdenis.weatherlogger.messaging.client.MessageClient
import com.github.nekdenis.weatherlogger.messaging.client.MessageClientRxImpl
import com.github.nekdenis.weatherlogger.messaging.client.MqttClient
import com.github.nekdenis.weatherlogger.messaging.client.MqttClientImpl
import com.github.nekdenis.weatherlogger.messaging.server.MessageServer
import com.github.nekdenis.weatherlogger.messaging.server.MessageServerImpl
import com.github.nekdenis.weatherlogger.messaging.server.MqqtBroker
import com.github.nekdenis.weatherlogger.messaging.server.MqqtBrokerImpl
import com.github.nekdenis.weatherlogger.sensors.TemperatureProvider
import com.github.nekdenis.weatherlogger.sensors.TemperatureProviderArduino
import com.github.nekdenis.weatherlogger.sensors.WeatherProviderImpl
import com.google.android.things.pio.PeripheralManagerService
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.gson.Gson

object Injector {
    lateinit var appRef: App
    internal val log by lazy { LoggerImpl() }
    internal val time by lazy {  TimeProviderImpl() }
    internal val deviceInfo by lazy {  DeviceInfoImpl(appRef.contentResolver) }
    internal val peripheralManager by lazy {  PeripheralManagerService() }
    internal val gson by lazy {  Gson() }

    internal val messageClient by lazy {  MessageClientRxImpl(mqqtClient(), disposableHolder()) }

    internal val jsonMaker by lazy {  GsonMaker(gson) }
    internal val rxFirebaseDb by lazy {  RxFirebaseDatabase(jsonMaker(), log()) }
    internal val firebaseRootRefHolder by lazy {  FirebaseRootReferenceHolderImpl(firebaseDB(), FIREBASE_ROOT, deviceInfo()) }
    internal val conditionerConfigRepo: ConditionerConfigRepo by lazy {  ConditionerConfigFirebaseRepo(firebaseDB(), firebaseRootRefHolder(), rxFirebaseDb()) }
    internal val weatherModelRepo by lazy {  WeatherModelFirebaseRepo(firebaseDB(), firebaseRootRefHolder(), rxFirebaseDb()) }
    internal val weatherProvider by lazy {  WeatherProviderImpl(messageClient(), weatherModelRepo(), timeProvider(), log(), airConditionerWatchdog()) }
}

fun mainController(): MainController = MainControllerImpl(
        weatherProvider = weatherProvider(),
        messageServer = messageServer(),
        messageClient = messageClient(),
        climateController = climateController(),
        indicatorController = indicatorController(),
        buttons = buttons(),
        buzzer = buzzer(),
        log = log(),
        compositeDisposableHolder = disposableHolder(),
        airConditionerWatchdog = airConditionerWatchdog(),
        temperatureWatchdog = temperatureWatchdog(),
        conditionerConfigRepo = conditionerConfigRepo())

fun climateController(): ClimateController = ClimateControllerImpl(weatherProvider(), conditionerConfigRepo(), airConditioner(), disposableHolder())

fun deviceInfo(): DeviceInfo = Injector.deviceInfo
fun temperatureProvider(): TemperatureProvider = TemperatureProviderArduino()

fun timeProvider(): TimeProvider = Injector.time
fun log(): Logger = Injector.log

fun disposableHolder() = CompositeDisposableHolderImpl()

internal fun mqqtBroker(): MqqtBroker = MqqtBrokerImpl(log())
fun messageServer(): MessageServer = MessageServerImpl(mqqtBroker(), disposableHolder())
private fun mqqtClient(): MqttClient = MqttClientImpl(log())
fun messageClient(): MessageClient = Injector.messageClient

fun airConditioner(): AirConditioner = AirConditionerMqtt(messageClient(), disposableHolder(), airConditionerWatchdog())

fun peripheralManager(): PeripheralManagerService = Injector.peripheralManager
fun display(): Display = DisplayImpl(log())
fun indicatorController(): IndicatorController = IndicatorControllerImpl(display(), leds(), timeProvider())
fun buttons(): Buttons = ButtonsImpl(log())
fun leds(): Leds = LedsImpl()
fun buzzer(): Buzzer = BuzzerImpl()


//region watchdogs
private fun newWatchDog() = WatchdogImpl(timeProvider(), disposableHolder())

private val airConditionerWatchdog = newWatchDog()
private val temperatureWatchdog = newWatchDog()
fun airConditionerWatchdog() = airConditionerWatchdog
fun temperatureWatchdog() = temperatureWatchdog

//endregion

//region repos
fun firebaseDB(): FirebaseDatabase = FirebaseDatabase.getInstance()

fun jsonMaker(): JsonMaker = Injector.jsonMaker

fun firebaseRef(): DatabaseReference = FirebaseDatabase.getInstance().reference
fun firebaseRootRefHolder(): FirebaseRootReferenceHolder = Injector.firebaseRootRefHolder
fun rxFirebaseDb(): RxFirebaseDatabase = Injector.rxFirebaseDb

fun conditionerConfigRepo() = Injector.conditionerConfigRepo
fun weatherModelRepo() = Injector.weatherModelRepo
fun weatherProvider() = Injector.weatherProvider

//endregion

