package com.alexyach.compose.gpstracker

import android.app.Application
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.alexyach.compose.gpstracker.data.db.GpsDatabase
import com.alexyach.compose.gpstracker.data.preferences.UserPreferencesRepository

// DataStore
private const val PREFERENCES_NAME = "preferencesName"
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = PREFERENCES_NAME
)

class GpsTrackerApplication: Application() {
    lateinit var userPreferencesRepository: UserPreferencesRepository

    val database by lazy { GpsDatabase.getDatabase(this) }
    val databaseDao by lazy { GpsDatabase.getDatabase(this).getGpsDao() }

    override fun onCreate() {
        super.onCreate()
        userPreferencesRepository = UserPreferencesRepository(dataStore)
    }

}