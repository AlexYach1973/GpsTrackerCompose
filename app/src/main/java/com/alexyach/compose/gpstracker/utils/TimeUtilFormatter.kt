package com.alexyach.compose.gpstracker.utils

import android.annotation.SuppressLint
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.TimeZone

object TimeUtilFormatter {
    @SuppressLint("SimpleDateFormat")
    private val timeFormatter = SimpleDateFormat("HH:mm:ss:SS")

    fun getTime(timeInMillis: Long): String {
        val calendar = Calendar.getInstance()
        timeFormatter.timeZone = TimeZone.getTimeZone("UTC")
        calendar.timeInMillis = timeInMillis

        return timeFormatter.format(calendar.time)
    }

}