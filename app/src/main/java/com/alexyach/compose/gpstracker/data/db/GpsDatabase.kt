package com.alexyach.compose.gpstracker.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [TrackItem::class], version = 1)
@TypeConverters(ConverterBitmap::class)
abstract class GpsDatabase : RoomDatabase() {

    abstract fun getGpsDao(): GpsDao

    companion object {
        @Volatile
        var INSTANCE: GpsDatabase? = null

        fun getDatabase(context: Context): GpsDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context,
                    GpsDatabase::class.java,
                    "Gps_database"
                ).build()
                INSTANCE = instance
                return instance
            }
        }
    }
}