package com.github.nekdenis.weatherlogger

const val MQTT_SERVER_URL = "tcp://0.0.0.0:1883"
const val MQTT_CLIENT_NAME = "ClimateController"
const val MQTT_RECONNECT_TIMEOUT = 1000 * 2L
const val MQTT_WATCHDOG_TIMEOUT = 1000 * 60 * 10L

const val MQTT_TEMPERATURE_ROOM_TOPIC = "room/temperature"

const val MQTT_AIR_CONDITIONER_CONTROL_TOPIC = "kitchen/control"
const val MQTT_AIR_CONDITIONER_RESPONSE_TOPIC = "kitchen/response"

const val MQTT_COMMAND_TURN_ON = "1"
const val MQTT_COMMAND_TURN_OFF = "0"
