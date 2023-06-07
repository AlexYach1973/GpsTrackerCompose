package com.alexyach.compose.gpstracker.utils

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.alexyach.compose.gpstracker.R
import com.alexyach.compose.gpstracker.screens.gpssettings.TAG
import org.osmdroid.views.MapView

/**
 * Запоминает MapView и передает ему жизненный цикл текущего LifecycleOwner
  */

@Composable
fun rememberMapViewWithLifecycleUtil(): MapView {
    val context = LocalContext.current
    val  mapView = remember {
        MapView(context).apply {
            id = R.id.map
        }
    }

    val lifecycle = LocalLifecycleOwner.current.lifecycle
    DisposableEffect(key1 = lifecycle, key2 = mapView){
        val lifecycleObserver = getMapLifecycleObserver(mapView)
        lifecycle.addObserver(lifecycleObserver)
//                Log.d(TAG, "addObserver map: ${mapView.id}")
        onDispose {
            lifecycle.removeObserver(lifecycleObserver)
//                Log.d(TAG, "onDispose map: ${mapView.id}")
        }
    }
    return mapView
}

private fun getMapLifecycleObserver(mapView: MapView): LifecycleEventObserver =
    LifecycleEventObserver { _, event ->
        when (event) {
            Lifecycle.Event.ON_CREATE -> {Log.d(TAG, "MapView ON_CREATE")}
            Lifecycle.Event.ON_START -> {Log.d(TAG, "MapView ON_START")}
            Lifecycle.Event.ON_RESUME -> {
                mapView.onResume()
                Log.d(TAG, "MapView ON_RESUME")
            }
            Lifecycle.Event.ON_PAUSE -> {
                mapView.onPause()
                Log.d(TAG, "MapView ON_PAUSE")
            }
            Lifecycle.Event.ON_STOP -> {Log.d(TAG, "MapView ON_STOP")}
            Lifecycle.Event.ON_DESTROY -> {Log.d(TAG, "MapView ON_DESTROY")}
            Lifecycle.Event.ON_ANY -> {Log.d(TAG, "MapView ON_ANY")}
        }
    }

