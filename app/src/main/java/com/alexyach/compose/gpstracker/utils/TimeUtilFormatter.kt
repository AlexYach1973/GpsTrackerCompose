package com.alexyach.compose.gpstracker.utils

import android.annotation.SuppressLint
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.TimeZone
@SuppressLint("SimpleDateFormat")
object TimeUtilFormatter {

    private val timeFormatter = SimpleDateFormat("HH:mm:ss:SS")
    private val dateFormatter = SimpleDateFormat("dd.MM.yyyy HH:mm")

    private val calendar = Calendar.getInstance()
    fun getTime(timeInMillis: Long): String {
        timeFormatter.timeZone = TimeZone.getTimeZone("UTC")
        calendar.timeInMillis = timeInMillis

        return timeFormatter.format(calendar.time)
    }

    fun getDate(): String {
        return dateFormatter.format(calendar.time)
    }

}