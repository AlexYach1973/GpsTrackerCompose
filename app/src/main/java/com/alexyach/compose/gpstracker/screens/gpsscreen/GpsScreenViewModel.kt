package com.alexyach.compose.gpstracker.screens.gpsscreen

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alexyach.compose.gpstracker.data.location.LocationService
import com.alexyach.compose.gpstracker.screens.gpssettings.TAG
import com.alexyach.compose.gpstracker.utils.TimeUtilFormatter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Timer
import java.util.TimerTask

class GpsScreenViewModel : ViewModel() {

//    var updateTime by mutableStateOf("00:00:00")

    /** mutableStateOf плохо обновляется в фоновом потоке (внутри run()),
     * поэтому наблюдаем MutableLiveData и в @Compose пишем .observeAsState()  */
    val updateTimeLiveData = MutableLiveData("00:00:00:00")


    var timer: Timer? = null
    private var startTime = 0L

    init {
        // Если сервис работал в фоне- запустить счетчик
        if (LocationService.isRunning) {
            startTimer()
        }
    }

    fun startTimer() {

            timer?.cancel()
            timer = Timer()
            startTime = LocationService.startTime

            timer?.schedule(object : TimerTask() {
                override fun run() {
//                    updateTime = getCurrentTime()
                    updateTimeLiveData.postValue(getCurrentTime())

//                Log.d(TAG, "GpsScreenViewModel, run() Thread ${Thread.currentThread().name}")
                }
            }, 1, 1)

    }

    private fun getCurrentTime(): String {
        return TimeUtilFormatter.getTime(System.currentTimeMillis() - startTime)
    }

    fun stopTimer() {
        timer?.cancel()
    }


}