package com.alexyach.compose.gpstracker.data.db

import android.graphics.Bitmap
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "track")
data class TrackItem(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    @ColumnInfo(name = "time")
    val time: String = "",
    @ColumnInfo(name = "date")
    val date: String = "",
    @ColumnInfo(name = "distance")
    val distance: String = "",
    @ColumnInfo(name = "velocity")
    val speed: String = "",
    @ColumnInfo(name = "geo_points")
    val geoPoints: String = "",
    @ColumnInfo(name = "geo_map")
    val geoMap: Bitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)
)
