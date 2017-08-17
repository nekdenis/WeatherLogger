package com.github.nekdenis.weatherlogger.utils

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.TimeZone


interface TimeProvider {
    fun nowMillis(): Long
    fun nowDayFormatted(): String
    fun dayFormatted(time: Long): String
    fun nowTimeFormatted(): String
    fun timeFormatted(time: Long): String
    fun isNight(): Boolean
}

class TimeProviderImpl : TimeProvider {
    private val dayFormat = SimpleDateFormat("yyyy-MM-dd").apply { timeZone = timeZone() }
    private val timeFormat = SimpleDateFormat("HH:mm:ss").apply { timeZone = timeZone() }
    private fun timeZone() = TimeZone.getTimeZone("GMT+8")

    override fun nowMillis(): Long = System.currentTimeMillis()
    override fun nowDayFormatted(): String = dayFormat.format(Date(nowMillis()))
    override fun dayFormatted(date: Long): String = dayFormat.format(Date(date))
    override fun nowTimeFormatted(): String = timeFormat.format(Date(nowMillis()))
    override fun timeFormatted(time: Long): String = timeFormat.format(Date(time))
    override fun isNight(): Boolean = Calendar.getInstance().run {
        timeInMillis = nowMillis()
        val hour = get(Calendar.HOUR_OF_DAY)
        return@run hour < 7 || hour > 19
    }
}