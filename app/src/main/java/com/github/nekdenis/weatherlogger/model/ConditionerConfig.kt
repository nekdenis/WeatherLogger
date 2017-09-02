package com.github.nekdenis.weatherlogger.model

import com.github.nekdenis.weatherlogger.DEFAULT_BOUNDARY_TEMPERATURE

data class ConditionerConfig(val boundaryTemp: Double = DEFAULT_BOUNDARY_TEMPERATURE)