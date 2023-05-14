package com.alexyach.compose.gpstracker.screens.gpsscreen

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.Canvas
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
import com.alexyach.compose.gpstracker.R
import com.alexyach.compose.gpstracker.data.db.GpsDao
import com.alexyach.compose.gpstracker.data.db.TrackItem
import com.alexyach.compose.gpstracker.data.location.LocationModel
import com.alexyach.compose.gpstracker.data.location.LocationService
import com.alexyach.compose.gpstracker.screens.gpssettings.TAG
import com.alexyach.compose.gpstracker.utils.TimeUtilFormatter
import kotlinx.coroutines.launch
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.Polyline
import java.util.Timer
import java.util.TimerTask

class GpsScreenViewModel(
    private val databaseDao: GpsDao
) : ViewModel() {

    var testCount: Int = 0

    var startFirst = true

    var locationUpdate by mutableStateOf(LocationModel(geoPointsList = ArrayList()))

    var polylineUpdate by mutableStateOf(Polyline())

//    private lateinit var listLocation: List<GeoPoint>

    var averageVelocity by mutableStateOf(0.0f)

    /** mutableStateOf плохо обновляется в фоновом потоке (внутри run()),
     * поэтому наблюдаем MutableLiveData и в @Compose пишем .observeAsState()  */
    val updateTimeLiveData = MutableLiveData("00:00:00:00")


    private var timer: Timer? = null
    private var startTime = 0L

    init {
        // Если сервис работал в фоне- запустить счетчик
        if (LocationService.isRunning) {
            startTimer()

            Log.d(TAG, "ScreenViewModel, init{}")
        }
    }

    fun startTimer() {
        timer?.cancel()
        timer = Timer()
        startTime = LocationService.startTime

        timer?.schedule(object : TimerTask() {
            override fun run() {
                updateTimeLiveData.postValue(getCurrentTime())

//                Log.d(TAG, "GpsScreenViewModel, run() Thread ${Thread.currentThread().name}")
            }
        }, 1, 1)
    }

    private fun getCurrentTime(): String {
        return TimeUtilFormatter.getTime(
            System.currentTimeMillis() - startTime
        )
    }

    fun stopTimer() {
        timer?.cancel()
    }

    /** Информация из Receiver */
    fun locationUpdateFromReceiver(location: LocationModel) {
        locationUpdate = location
        getAverageVelocity(location)

        updatePolylineNew(location.geoPointsList)


//        Log.d(TAG, "ScreenViewModel, locationUpdateFromReceiver ${testCount++}")
    }

    private fun getAverageVelocity(location: LocationModel) {
        averageVelocity =
            location.distance / ((System.currentTimeMillis() - startTime) / 1000.0f)
    }

    /** Polyline */
    /*fun updatePolyline(geoPointsList: ArrayList<GeoPoint>): Polyline? {
        return if (geoPointsList.size > 1 && startFirst) {
            startFirst = false
            fillPolyline(geoPointsList)
        } else {
            addOnePoint(geoPointsList)
        }
    }
    */
    private fun updatePolylineNew(list: List<GeoPoint>) {
            if (list.size > 1 && startFirst) {
                startFirst = false
                fillPolyline(list)
            } else {
                addOnePoint(list)
            }
    }

    private fun addOnePoint(list: List<GeoPoint>) {
        if (list.isNotEmpty()) {
            Log.d(TAG, "addOnePoint, list.size: ${list.size}")
//            pl?.addPoint(list.last())
            polylineUpdate.addPoint(list[list.size - 1])
        }
    }

    private fun fillPolyline(list: List<GeoPoint>) {
        list.forEach {
            polylineUpdate.addPoint(it)
        }
    }
    /** End Polyline */


    /** Database */
    fun insert(item: TrackItem) = viewModelScope.launch {
        databaseDao.insertTrack(item)
    }

    /** ScreenShot */
    lateinit var bitmap: Bitmap

    @SuppressLint("ResourceAsColor")
    fun createScreenShot(view: View) {
        bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val bgDrawable = view.background
        if (bgDrawable != null) bgDrawable.draw(canvas)
        else canvas.drawColor((R.color.white))
        view.draw(canvas)
    }

    fun getScreenshot(): Bitmap {
        return bitmap
    }

    /** End ScreenShot */

    override fun onCleared() {
        super.onCleared()
        Log.d(TAG, "GpsScreenViewModel onCleared()")
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application =
                    (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as GpsTrackerApplication)
                GpsScreenViewModel(application.databaseDao)
            }
        }
    }

}