package com.github.nekdenis.weatherlogger.utils

import java.text.SimpleDateFormat
import java.util.Date


interface TimeProvider {
    fun nowMillis(): Long
    fun nowDayFormatted(): String
    fun dayFormatted(time: Long): String
    fun nowTimeFormatted(): String
    fun timeFormatted(time: Long): String
}

class TimeProviderImpl : TimeProvider {

    private val dayFormat = SimpleDateFormat("yyyy-MM-dd")
    private val timeFormat = SimpleDateFormat("HH:mm")

    override fun nowMillis(): Long = System.currentTimeMillis()
    override fun nowDayFormatted(): String = dayFormat.format(Date(nowMillis()))
    override fun dayFormatted(date: Long): String = dayFormat.format(Date(date))
    override fun nowTimeFormatted(): String = timeFormat.format(Date(nowMillis()))
    override fun timeFormatted(time: Long): String = timeFormat.format(Date(time))
}