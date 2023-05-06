package com.alexyach.compose.gpstracker.screens.gpssettings

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.alexyach.compose.gpstracker.GpsTrackerApplication
import com.alexyach.compose.gpstracker.data.preferences.UserPreferencesRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

const val TAG = "myLogs"

class GpsSettingsViewModel(
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    var updateTime by mutableStateOf(3000)
        private set

    var updateTimeText by mutableStateOf("3 sec")
        private set

    val updateTimePref: StateFlow<Int> =
        userPreferencesRepository.updateTime.map {
            updateTime = it
            updateTimeText = getPrefTextByUpdateTime(it)

//            Log.d(TAG, "GpsSettingsViewModel, updateTimePref it= $it")
            Log.d(TAG, "GpsSettingsViewModel, updateTimePref Thread= ${Thread.currentThread().name}")

        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = 3000
        )

    fun savePreferences(selectedOptionsText: String) {
        // Обновили текст для наблюдения
        updateTimeText = selectedOptionsText

//        Log.d(TAG, "GpsSettingsViewModel, savePreferences text: $selectedOptionsText")
//        Log.d(TAG, "GpsSettingsViewModel, savePreferences text: ${getUpdateTimeByText(selectedOptionsText)}")

        viewModelScope.launch {
            userPreferencesRepository.saveUpdatePreference(
                getUpdateTimeByText(selectedOptionsText)
            )
        }
    }


    private fun getPrefTextByUpdateTime(updateTime: Int): String {
        return when (updateTime) {
            UpdateTimeSelected.Sec3.time -> UpdateTimeSelected.Sec3.text
            UpdateTimeSelected.Sec5.time ->  UpdateTimeSelected.Sec5.text
            UpdateTimeSelected.Sec15.time -> UpdateTimeSelected.Sec15.text
            UpdateTimeSelected.Sec30.time -> UpdateTimeSelected.Sec30.text
            else -> {UpdateTimeSelected.Sec3.text}
        }
    }

    private fun getUpdateTimeByText(text: String): Int {
        return when (text) {
            UpdateTimeSelected.Sec3.text -> UpdateTimeSelected.Sec3.time
            UpdateTimeSelected.Sec5.text -> UpdateTimeSelected.Sec5.time
            UpdateTimeSelected.Sec15.text -> UpdateTimeSelected.Sec15.time
            UpdateTimeSelected.Sec30.text -> UpdateTimeSelected.Sec30.time
            else ->  UpdateTimeSelected.Sec3.time
        }
    }


    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[APPLICATION_KEY] as GpsTrackerApplication)
                GpsSettingsViewModel(application.userPreferencesRepository)
            }
        }
    }
}