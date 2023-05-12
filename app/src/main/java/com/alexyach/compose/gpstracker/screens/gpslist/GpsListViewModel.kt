package com.alexyach.compose.gpstracker.screens.gpslist

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.alexyach.compose.gpstracker.GpsTrackerApplication
import com.alexyach.compose.gpstracker.data.db.GpsDao
import com.alexyach.compose.gpstracker.data.db.TrackItem
import com.alexyach.compose.gpstracker.screens.gpsscreen.GpsScreenViewModel
import com.alexyach.compose.gpstracker.screens.gpssettings.TAG
import kotlinx.coroutines.launch

class GpsListViewModel(
    private val databaseDao: GpsDao
): ViewModel() {

    var allGpsTrack by mutableStateOf<List<TrackItem>>(emptyList<TrackItem>())

//    lateinit var trackDetails: TrackItem

    init {
        getAllGpsTrack()
        Log.d(TAG, "GpsListViewModel, init{}")
    }

    private fun getAllGpsTrack(){
        viewModelScope.launch {
            databaseDao.getAllItems().collect{
                allGpsTrack = it
            }
        }
    }

    fun delete(item: TrackItem) = viewModelScope.launch {
        databaseDao.delete(item)
    }

//    fun getTrackDetails(): TrackItem {
//        return trackDetails
//    }


    override fun onCleared() {
        super.onCleared()
        Log.d(TAG, "GpsListViewModel onCleared()")
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as GpsTrackerApplication)
                GpsListViewModel(application.databaseDao)
            }
        }
    }
}