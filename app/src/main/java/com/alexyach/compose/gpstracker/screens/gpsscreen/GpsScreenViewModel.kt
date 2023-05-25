package com.alexyach.compose.gpstracker.screens.gpsscreen

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.util.Log
import android.view.View
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.alexyach.compose.gpstracker.GpsTrackerApplication
import com.alexyach.compose.gpstracker.data.db.GpsDao
import com.alexyach.compose.gpstracker.data.db.TrackItem
import com.alexyach.compose.gpstracker.data.location.LocationModel
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

    var locationUpdate by mutableStateOf(LocationModel(geoPointsList = ArrayList()))

    private var pl = Polyline()

    /** mutableStateOf плохо обновляется в фоновом потоке (внутри run()),
     * поэтому наблюдаем MutableLiveData и в @Compose пишем .observeAsState()  */
    val updateTimeLiveData = MutableLiveData("00:00:00:00")

    private var startFirst = true
    private var timer: Timer? = null
    private var startTime = 0L
    private var stopTime = 0L
    var timeUpdate = 0


    init {
        // Если сервис работал в фоне- запустить счетчик
        if (LocationService.isRunning) {
            startTimer()
        }
        Log.d(TAG, "ScreenViewModel, init{}")
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

    /** Информация из Service через GpsScreen */
    fun locationUpdateFromService(location: LocationModel) {
        locationUpdate = location
        getAverageVelocity(location)

//        Log.d(TAG, "ScreenViewModel, locationUpdateFromReceiver ${location.geoPointsList.size}")
    }

    fun getAverageVelocity(location: LocationModel?): Float {
        return if (location != null ) {
            if (LocationService.isRunning) {
                location.distance / ((System.currentTimeMillis() - startTime) / 1000.0f)
            } else {
                location.distance / ((stopTime - startTime) / 1000.0f)
            }
        } else {
            0f
        }
    }

    /** Polyline */
    fun updatePolylineNew(list: List<GeoPoint>): Polyline {
        return if (list.size > 1 && startFirst) {
            startFirst = false
            fillPolyline(list, pl)
        } else {
            addOnePoint(list, pl)
        }
    }

    private fun addOnePoint(list: List<GeoPoint>, pl: Polyline): Polyline {
        if (list.isNotEmpty()) {
            pl.addPoint(list[list.size - 1])

//            Log.d(TAG, "ViewModel, addOnePoint, list: ${list}")
        }
        return pl
    }

    private fun fillPolyline(list: List<GeoPoint>, pl: Polyline): Polyline {

        if (list.isNotEmpty()) {
            list.forEach {
                pl.addPoint(it)
//                Log.d(TAG, "ViewModel, fillPolyline, list: ${list}")
            }
        }
        return pl
    }
    /** End Polyline */


    /** Database */
    fun insert(item: TrackItem) {
        viewModelScope.launch {
            databaseDao.insertTrack(item)
        }
    }

    /** ScreenShot */
    lateinit var bitmap: Bitmap

    @SuppressLint("ResourceAsColor")
    fun createScreenShot(view: View) {
        bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)

        /*val canvas = Canvas(bitmap)
        val bgDrawable = view.background
        if (bgDrawable != null) bgDrawable.draw(canvas)
        else canvas.drawColor((R.color.white))
        view.draw(canvas)*/
    }

    fun getScreenshot(): Bitmap {
        return bitmap
    }
    /** End ScreenShot */

    override fun onCleared() {
        super.onCleared()
        // Записать время перед выходом
//        saveStartTimeToDataStore()
        Log.d(TAG, "ScreenViewModel onCleared()")
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
                Log.d(TAG, "ScreenViewModel, read timeUpdate= $timeUpdate")
            }
        }
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