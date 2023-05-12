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

    var startFirst = true
    var pl: Polyline? = Polyline()

    var locationUpdate by mutableStateOf<LocationModel>(
        LocationModel(
            geoPointsList = ArrayList()
        )
    )

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

            Log.d(TAG, "GpsScreenViewModel, init{}")
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

    // Информация из Receiver
    fun locationUpdateFromReceiver(location: LocationModel) {
        locationUpdate = location
        getAverageVelocity()
    }

    private fun getAverageVelocity() {
        averageVelocity =
            locationUpdate.distance / ((System.currentTimeMillis() - startTime) / 1000.0f)
    }

    /** Polyline */
    fun updatePolyline(list: List<GeoPoint>): Polyline? {
        return if (list.size > 1 && startFirst) {
            startFirst = false
            fillPolyline(list)
        } else {
            addOnePoint(list)
        }
    }
    private fun addOnePoint(list: List<GeoPoint>): Polyline? {
        if (list.isNotEmpty()) {
            pl?.addPoint(list[list.size - 1])
        }
        return pl
    }
    private fun fillPolyline(list: List<GeoPoint>): Polyline? {
        list.forEach {
            pl?.addPoint(it)
        }
        return pl
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
                val application = (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as GpsTrackerApplication)
                GpsScreenViewModel(application.databaseDao)
            }
        }
    }

}