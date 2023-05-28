package com.alexyach.compose.gpstracker.utils

import org.osmdroid.util.GeoPoint

object GeoPointsUtils {

    fun geoPointsToString(list: List<GeoPoint>): String {
        val sb = StringBuilder()

        list.forEach {
            sb.append("${it.latitude},${it.longitude}/")
        }
        return sb.toString()
    }

    fun stringToGeoPoints(geoPoints: String): ArrayList<GeoPoint> {
        val geoPointsList = ArrayList<GeoPoint>()
        val temporaryList = geoPoints.split("/")

        temporaryList.forEach {
            if (it.isEmpty()) return@forEach
            val points = it.split(",")
            geoPointsList.add(
                GeoPoint(
                    points[0].toDouble(),
                    points[1].toDouble()
                )
            )
        }
        return geoPointsList
    }
}