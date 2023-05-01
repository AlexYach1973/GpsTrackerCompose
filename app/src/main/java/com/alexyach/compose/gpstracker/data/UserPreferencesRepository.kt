package com.alexyach.compose.gpstracker.data

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

class UserPreferencesRepository(
    private val dataStore: DataStore<Preferences>
) {

    val updateTime: Flow<Int> = dataStore.data
        .catch {error ->
            if (error is IOException) {
                Log.d("myLogs", "Error reading preferences: ", error)
                emit(emptyPreferences())
            }
        }
        .map { preference ->
        preference[UPDATE_TIME_KEY] ?: 3000
    }

    suspend fun saveUpdatePreference(updateTime: Int) {
        dataStore.edit {preference ->
            preference[UPDATE_TIME_KEY] = updateTime
        }
    }

    companion object {
        val UPDATE_TIME_KEY = intPreferencesKey("update_time")
    }
}