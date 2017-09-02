package com.github.nekdenis.weatherlogger.model

import com.github.nekdenis.weatherlogger.DEFAULT_BOUNDARY_TEMPERATURE

const val CONFIG_MODE_AUTO = "AUTO"
const val CONFIG_MODE_ON = "ON"
const val CONFIG_MODE_OFF = "OFF"

data class ConditionerConfig(val boundaryTemp: Double = DEFAULT_BOUNDARY_TEMPERATURE, val mode: String = CONFIG_MODE_AUTO)