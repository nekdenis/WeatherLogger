package com.github.nekdenis.weatherlogger

const val MQTT_SERVER_URL = "tcp://0.0.0.0:1883"
const val MQTT_CLIENT_NAME = "ClimateController"
const val MQTT_RECONNECT_TIMEOUT = 1000 * 2L

const val MQTT_AIR_CONDITIONER_CONTROL_TOPIC = "kitchen_control"
const val MQTT_AIR_CONDITIONER_RESPONSE_TOPIC = "kitchen_response"

const val MQTT_COMAND_TURN_ON = "1"
const val MQTT_COMAND_TURN_OFF = "0"
