package com.github.nekdenis.weatherlogger.utils

import com.github.nekdenis.weatherlogger.MainController
import com.github.nekdenis.weatherlogger.MainControllerImpl
import com.github.nekdenis.weatherlogger.db.DBProvider
import com.github.nekdenis.weatherlogger.db.DBProviderImpl
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
import com.github.nekdenis.weatherlogger.messaging.ClientRunner
import com.github.nekdenis.weatherlogger.messaging.ClientRunnerAndroidImpl
import com.github.nekdenis.weatherlogger.messaging.ServerRunner
import com.github.nekdenis.weatherlogger.messaging.ServerRunnerAndroidImpl
import com.github.nekdenis.weatherlogger.messaging.client.MessageClient
import com.github.nekdenis.weatherlogger.messaging.client.MessageClientImpl
import com.github.nekdenis.weatherlogger.messaging.server.MessageHandler
import com.github.nekdenis.weatherlogger.messaging.server.MessageServer
import com.github.nekdenis.weatherlogger.messaging.server.MessageServerImpl
import com.github.nekdenis.weatherlogger.messaging.server.MqqtBroker
import com.github.nekdenis.weatherlogger.messaging.server.MqqtBrokerImpl
import com.github.nekdenis.weatherlogger.sensors.TemperatureProvider
import com.github.nekdenis.weatherlogger.sensors.TemperatureProviderImpl
import com.github.nekdenis.weatherlogger.sensors.WeatherRepo
import com.github.nekdenis.weatherlogger.sensors.WeatherRepoImpl
import com.google.android.things.pio.PeripheralManagerService
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase


private val log = LoggerImpl()
private val time = TimeProviderImpl()
private val dbProvider = DBProviderImpl(firebase(), timeProvider(), log())
private val peripheralManager = PeripheralManagerService()

fun mainController(): MainController = MainControllerImpl(temperatureLogger(), messageServer(), airConditioner(), climateController(), indicatorController(), buttons(), buzzer(), log())
fun climateController(): ClimateController = ClimateControllerImpl(dbProvider())

fun temperatureLogger(): WeatherRepo = WeatherRepoImpl(temperatureProvider(), dbProvider(), timeProvider(), log())
fun temperatureProvider(): TemperatureProvider = TemperatureProviderImpl()

fun firebase(): DatabaseReference = FirebaseDatabase.getInstance().reference
fun timeProvider(): TimeProvider = time
fun log(): Logger = log

fun dbProvider(): DBProvider = dbProvider

fun messageServer(): MessageServer = MessageServerImpl(mqqtBroker(), serverRunner(), messageHandler())
fun serverRunner(): ServerRunner = ServerRunnerAndroidImpl()

fun mqqtBroker(): MqqtBroker = MqqtBrokerImpl()
fun messageHandler(): MessageHandler = object : MessageHandler {
    override fun handleMessage() {}
}

fun messageClient(): MessageClient = MessageClientImpl(log())
fun clientRunner(): ClientRunner = ClientRunnerAndroidImpl(messageClient())

fun airConditioner(): AirConditioner = AirConditionerMqtt(clientRunner(), timeProvider())

fun peripheralManager(): PeripheralManagerService = peripheralManager
fun display(): Display = DisplayImpl(log())
fun indicatorController(): IndicatorController = IndicatorControllerImpl(display(), leds(), dbProvider(), timeProvider())
fun buttons(): Buttons = ButtonsImpl(log())
fun leds(): Leds = LedsImpl()
fun buzzer(): Buzzer = BuzzerImpl()

