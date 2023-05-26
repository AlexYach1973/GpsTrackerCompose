package com.alexyach.compose.gpstracker.screens.gpslist

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
import com.alexyach.compose.gpstracker.utils.GeoPointsUtils
import kotlinx.coroutines.launch
import org.osmdroid.views.overlay.Polyline

class GpsListViewModel(
    private val databaseDao: GpsDao
): ViewModel() {

    var allGpsTrack by mutableStateOf<List<TrackItem>>(emptyList<TrackItem>())
    var trackDetails by mutableStateOf <TrackItem>(TrackItem())

    init {
        getAllGpsTrack()
//        Log.d(TAG, "GpsListViewModel, init{}")
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

    fun getTrackDetailsPolyline(): Polyline {
        val pl = Polyline()
            val listGeoPoints = GeoPointsUtils.stringToGeoPoints(
                trackDetails.geoPoints
            )

        if (listGeoPoints.isNotEmpty()) {
            listGeoPoints.forEach {
                pl.addPoint(it)
            }
        }

        return pl
    }


    override fun onCleared() {
        super.onCleared()
//        Log.d(TAG, "GpsListViewModel onCleared()")
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