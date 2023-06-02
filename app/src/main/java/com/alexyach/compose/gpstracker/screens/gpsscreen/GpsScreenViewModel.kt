package com.alexyach.compose.gpstracker.screens.gpsscreen

import android.content.Context
import android.content.Intent
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.alexyach.compose.gpstracker.GpsTrackerApplication
import com.alexyach.compose.gpstracker.data.db.GpsDao
import com.alexyach.compose.gpstracker.data.db.TrackItem
import com.alexyach.compose.gpstracker.data.location.LocationService
import com.alexyach.compose.gpstracker.data.preferences.UserPreferencesRepository
import com.alexyach.compose.gpstracker.utils.TimeUtilFormatter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.Polyline
import java.util.Timer
import java.util.TimerTask

class GpsScreenViewModel(
    private val databaseDao: GpsDao,
    private val dataStore: UserPreferencesRepository
) : ViewModel() {

    /** Update Location From Service */
    var locationUpdate = LocationService.locationLiveData

    var geopointsList = MutableStateFlow(Polyline())

    /** mutableStateOf плохо обновляется в фоновом потоке (внутри run()),
     * поэтому наблюдаем MutableLiveData и в @Compose пишем .observeAsState()  */
    val updateTimeLiveData = MutableLiveData("00:00:00:00")

    private var startFirst = true
    private var timer: Timer? = null
    private var startTime = 0L
    private var stopTime = 0L
    private var timeUpdate = 0


    init {
        // Если сервис работал в фоне- запустить счетчик
        if (LocationService.isRunning) {
            startTimer()
        }
        // Сразу считали updateTime из DataStore
        readUpdateTimeFromDataStore()
    }

    fun startTimer() {
        timer?.cancel()
        timer = Timer()
        readStartTimeFromDataStore()
        // Обнулили линию
        geopointsList.value = Polyline()

        // Заруск таймера
        timer?.schedule(object : TimerTask() {
            override fun run() {
                updateTimeLiveData.postValue(getCurrentTimeString())
            }
        }, 1, 1)
    }

    private fun getCurrentTimeString(): String {
        return TimeUtilFormatter.getTime(
            System.currentTimeMillis() - startTime
        )
    }

    fun stopTimer() {
        timer?.cancel()
        stopTime = System.currentTimeMillis()
    }

    fun getAverageVelocity(): Float {
        return if (LocationService.isRunning) {
            locationUpdate.value!!.distance / ((System.currentTimeMillis() - startTime) / 1000.0f)
        } else {
            locationUpdate.value!!.distance / ((stopTime - startTime) / 1000.0f)
        }
    }

    /** Polyline */
    fun updatePolyline() {
        val list = locationUpdate.value!!.geoPointsList
        if (list.size > 1 && startFirst) {
            startFirst = false
            fillPolyline(list)
        } else {
            addOnePoint(list)
        }
    }

    private fun addOnePoint(list: List<GeoPoint>) {
        if (list.isNotEmpty()) {
            geopointsList.value.addPoint(list.last())
        }
    }

    private fun fillPolyline(list: List<GeoPoint>) {
        if (list.isNotEmpty()) {
            list.forEach {
                geopointsList.value.addPoint(it)
            }
        }
    }
    /** End Polyline */


    /** Database */
    fun insert(item: TrackItem) {
        viewModelScope.launch {
            databaseDao.insertTrack(item)
        }
    }

    fun createIntentForService(context: Context): Intent {

        val intent = Intent(context, LocationService::class.java).apply {
            putExtra(UPDATE_TIME_KEY, timeUpdate)
        }

        return intent
    }

    /** DataStore */
    fun saveStartTimeToDataStore(time: Long) {
        viewModelScope.launch {
            dataStore.saveStartTime(time)
        }
//        Log.d(TAG, "ScreenViewModel, save StartTimer= ${TimeUtilFormatter.getTime(startTime)}")
    }

    private fun readStartTimeFromDataStore() {
        viewModelScope.launch {
            dataStore.startTime.collect {
                startTime = it
//                Log.d(TAG, "ScreenViewModel, read StartTimer= ${TimeUtilFormatter.getTime(startTime)}")
            }
        }
    }

    private fun readUpdateTimeFromDataStore() {
        viewModelScope.launch {
            dataStore.updateTime.collect {
                timeUpdate = it
//                Log.d(TAG, "ScreenViewModel, read timeUpdate= $timeUpdate")
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
//        Log.d(TAG, "ScreenViewModel onCleared()")
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application =
                    (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as GpsTrackerApplication)
                GpsScreenViewModel(
                    application.databaseDao,
                    application.userPreferencesRepository
                )
            }
        }

        const val UPDATE_TIME_KEY = "updateTime"
    }

}