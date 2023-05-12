package com.alexyach.compose.gpstracker.utils

import android.util.Log
import com.alexyach.compose.gpstracker.screens.gpssettings.TAG
import org.osmdroid.util.GeoPoint
import java.lang.StringBuilder

object GeoPointsUtils {

    fun geoPointsToString(list: List<GeoPoint>): String {
        val sb = StringBuilder()

        list.forEach {
            sb.append("${it.latitude},${it.longitude}/")
        }
//        Log.d(TAG, "GeoPointsUtils, Points: $sb")
        return sb.toString()
    }
}