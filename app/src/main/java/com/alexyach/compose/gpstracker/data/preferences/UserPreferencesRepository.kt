package com.alexyach.compose.gpstracker.data.preferences

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

class UserPreferencesRepository(
    private val dataStore: DataStore<Preferences>
) {


    suspend fun saveUpdatePreference(updateTime: Int) {
        dataStore.edit { preference ->
            preference[UPDATE_TIME_KEY] = updateTime
        }
    }

    val updateTime: Flow<Int> = dataStore.data
        .catch { error ->
            if (error is IOException) {
                Log.d("myLogs", "Error reading preferences: ", error)
                emit(emptyPreferences())
            }
        }
        .map { preference ->
            preference[UPDATE_TIME_KEY] ?: 3000
        }


    suspend fun saveGeopoints(geopoints: String) {
        dataStore.edit { preference ->
            preference[GEOPOINTS_KEY] = geopoints
        }
    }

    val geoPoints: Flow<String> = dataStore.data
        .catch { error ->
            if (error is IOException) {
                Log.d("myLogs", "Error reading geoPoints: ", error)
                emit(emptyPreferences())
            }
        }
        .map { preference ->
            preference[GEOPOINTS_KEY] ?: ""
        }


    suspend fun saveDistance(distance: Float) {
        dataStore.edit { preference ->
            preference[DISTANCE_KEY] = distance
        }
//        Log.d(TAG, "UserPreferencesRepository, saveDistance")
    }

    val distance: Flow<Float> = dataStore.data
        .catch { error ->
            if (error is IOException) {
                Log.d("myLogs", "Error reading distance: ", error)
                emit(emptyPreferences())
            }
        }
        .map { preference ->
            preference[DISTANCE_KEY] ?: 111.111f
        }

    suspend fun saveStartTime(time: Long) {
        dataStore.edit { preference ->
            preference[START_TIME_KEY] = time
        }
    }
    val startTime: Flow<Long> = dataStore.data
        .catch {error ->
            if (error is IOException) {
                Log.d("myLogs", "Error reading start time: ", error)
                emit(emptyPreferences())
            }
        }
        .map { preference ->
            preference[START_TIME_KEY] ?: 0L
        }


    companion object {
        val UPDATE_TIME_KEY = intPreferencesKey("update_time_key")
        val GEOPOINTS_KEY = stringPreferencesKey("geopoints_key")
        val DISTANCE_KEY = floatPreferencesKey("distance_key")
        val START_TIME_KEY = longPreferencesKey("start_time_key")
    }
}