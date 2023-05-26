package com.alexyach.compose.gpstracker.screens.gpsscreen

import android.content.Context
import android.content.Intent
import android.util.Log
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
import com.alexyach.compose.gpstracker.screens.gpssettings.TAG
import com.alexyach.compose.gpstracker.utils.TimeUtilFormatter
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

    private var pl = Polyline()

    /** mutableStateOf плохо обновляется в фоновом потоке (внутри run()),
     * поэтому наблюдаем MutableLiveData и в @Compose пишем .observeAsState()  */
    val updateTimeLiveData = MutableLiveData("00:00:00:00")

    var startFirst = true
    private var timer: Timer? = null
    private var startTime = 0L
    private var stopTime = 0L
    var timeUpdate = 0


    init {
        // Если сервис работал в фоне- запустить счетчик
        if (LocationService.isRunning) {
            startTimer()
        }

        Log.d(TAG, "ScreenViewModel init")
    }

    fun startTimer() {
        timer?.cancel()
        timer = Timer()
        readStartTimeFromDataStore()
        // Обнулили линию
        pl = Polyline()

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
       fun updatePolyline(): Polyline {
        val list = locationUpdate.value!!.geoPointsList
        return if (list.size > 1 && startFirst) {
            startFirst = false
            fillPolyline(list, pl)
        } else {
            addOnePoint(list, pl)
        }
    }

    private fun addOnePoint(list: List<GeoPoint>, pl: Polyline): Polyline {
//        Log.d(TAG, "ScreenViewModel, addOnePoint, pl: ${pl}")

        if (list.isNotEmpty()) {
            pl.addPoint(list.last())

//            Log.d(TAG, "ScreenViewModel, addOnePoint, list: ${list.last()}")
        }
        return pl
    }

    private fun fillPolyline(list: List<GeoPoint>, pl: Polyline): Polyline {

        if (list.isNotEmpty()) {
            list.forEach {
                pl.addPoint(it)
            }
        }
//        Log.d(TAG, "ViewModel, fillPolyline, list: ${list.last()}")
        return pl
    }
    /** End Polyline */


    /** Database */
    fun insert(item: TrackItem) {
        viewModelScope.launch {
            databaseDao.insertTrack(item)
        }
    }

    fun createIntentForService(context: Context): Intent {
        readUpdateTimeFromDataStore()
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
        Log.d(TAG, "ScreenViewModel onCleared()")
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