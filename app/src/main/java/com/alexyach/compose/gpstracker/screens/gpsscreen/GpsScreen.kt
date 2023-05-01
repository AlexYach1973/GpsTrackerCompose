package com.alexyach.compose.gpstracker.screens.gpsscreen

import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.viewinterop.AndroidViewBinding
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.alexyach.compose.gpstracker.R
import com.alexyach.compose.gpstracker.databinding.MapBinding
import org.osmdroid.config.Configuration
import org.osmdroid.library.BuildConfig
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay

@Composable
fun GpsScreen(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    /** Permission *//*
    val permission = arrayOf(
        android.Manifest.permission.ACCESS_FINE_LOCATION,
        android.Manifest.permission.ACCESS_COARSE_LOCATION
    )*/
    /*val pLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissionMap ->
        val areGranted = permissionMap.values.reduce { acc, next -> acc && next }
        if (areGranted) {
            // USE
            IniOsm(context = context)

        } else {
            // SHOW Dialog
        }

    }*/
/*    checkAndRequestLocationPermission(
        context = context,
        permissions = permission,
        launcher = pLauncher
    )*/
    /** *** */

    settingOsm(context)
    MapViewXML(context)
    IniOsm(context)

    Box(
        modifier = Modifier
    ) {


    }
}

private fun settingOsm(context: Context) {
    Configuration.getInstance().load(
        context,
        context.getSharedPreferences("osm_pref", Context.MODE_PRIVATE)
    )
    Configuration.getInstance().userAgentValue = BuildConfig.APPLICATION_ID
}


/** MapView  */
@Composable
fun MapViewXML(
    context: Context,
    modifier: Modifier = Modifier,
    onLoad: ((map: MapView) -> Unit)? = null
) {
    val mapViewState = rememberMapViewWithLifecycle(context)

    AndroidView(
        factory = { mapViewState },
        modifier
    ) { mapView ->
        onLoad?.invoke(mapView)
    }

//    IniOsm(context = context)

}

/**  MapLifecycle */
@Composable
fun rememberMapViewWithLifecycle(context: Context): MapView {
//    val context = LocalContext.current
    val mapView = remember {
        MapView(context).apply {
            id = R.id.map
        }
    }

    // Заставляет MapView следовать жизненному циклу этого компонуемого
    val lifecycleObserver = rememberMapLifecycleObserver(mapView)
    val lifecycle = LocalLifecycleOwner.current.lifecycle

    DisposableEffect(lifecycle) {
        lifecycle.addObserver(lifecycleObserver)
        onDispose {
            lifecycle.removeObserver(lifecycleObserver)
        }
    }

    return mapView
}

@Composable
fun rememberMapLifecycleObserver(mapView: MapView): LifecycleEventObserver =
    remember(mapView) {
        LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                else -> {}
            }
        }
    }

/** Binding */
@Composable
fun IniOsm(context: Context) {
    AndroidViewBinding(MapBinding::inflate) {
        map.controller.setZoom(17.0)
        // Provider
        val mLocProvider = GpsMyLocationProvider(context)

        // Создаем слой поверх карты для показа пути
        val mLocOverlay = MyLocationNewOverlay(mLocProvider, map)
        // Включаем местоположения
        mLocOverlay.enableMyLocation()
        // Включаем следование за  местоположением
        // (но после использования Zoom - отключается)
        mLocOverlay.enableFollowLocation()

        // Добавляем слой на карту, после определения местоположения
        mLocOverlay.runOnFirstFix {
            map.overlays.clear() // очистили карту
            map.overlays.add(mLocOverlay)
        }


        /* TEST Kyiv
        map.controller.animateTo(GeoPoint(
            50.4501,
            30.5241
        ))*/

    }
}

/** Permission */
fun checkAndRequestLocationPermission(
    context: Context,
    permissions: Array<String>,
    launcher: ManagedActivityResultLauncher<Array<String>, Map<String, Boolean>>
) {
    if (permissions.all {
            ContextCompat.checkSelfPermission(context, it) ==
                    PackageManager.PERMISSION_GRANTED
        }) {
        // Use location because permissions are already granted
    } else {
        // Request permissions
        launcher.launch(permissions)
    }
}
