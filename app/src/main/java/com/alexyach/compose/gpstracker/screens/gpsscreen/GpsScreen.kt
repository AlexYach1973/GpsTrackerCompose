package com.alexyach.compose.gpstracker.screens.gpsscreen

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.FloatingActionButtonDefaults
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.viewinterop.AndroidViewBinding
import androidx.core.content.ContextCompat.getColor
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.viewmodel.compose.viewModel
import com.alexyach.compose.gpstracker.R
import com.alexyach.compose.gpstracker.data.db.TrackItem
import com.alexyach.compose.gpstracker.data.location.LocationService
import com.alexyach.compose.gpstracker.data.location.LocationService.Companion.locationLiveData
import com.alexyach.compose.gpstracker.databinding.MapBinding
import com.alexyach.compose.gpstracker.ui.theme.GpsTrackerTheme
import com.alexyach.compose.gpstracker.ui.theme.Transparent100
import com.alexyach.compose.gpstracker.utils.GeoPointsUtils
import com.alexyach.compose.gpstracker.utils.SaveTrackDialog
import com.alexyach.compose.gpstracker.utils.TimeUtilFormatter
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay


@SuppressLint("UnrememberedMutableState")
@Composable
fun GpsScreen(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val gpsViewModel: GpsScreenViewModel = viewModel(
        factory = GpsScreenViewModel.Factory
    )

    // Достаем из IniOsm()
    var mLocationOverlay: MyLocationNewOverlay?  by remember { mutableStateOf(null) }

    // Привязываемся к XML
    MapViewXML(
        context,
        viewModel = gpsViewModel
    )

    // Работа с самой картой
    IniOsm(
        context,
        gpsViewModel
    ) { mLocationOverlay = it }

    // диалог сохранения
    var showSaveTrackDialog by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier,
        contentAlignment = Alignment.Center
    ) {

        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
            ) {

                ColumnTextTitle()
                ColumnTextValue(gpsViewModel)
                Spacer(
                    modifier = Modifier
                        .weight(1f)
                        .background(Color.Cyan)
                )
                ColumnTwoFab(
                    gpsViewModel,
                    mLocationOverlay
                ) {
                    showSaveTrackDialog = it
                }

            }

            /** Open Save Dialog */
            if (showSaveTrackDialog) {

                val trackItem = TrackItem(
                    time = gpsViewModel.updateTimeLiveData.observeAsState().value.toString(),
                    date = TimeUtilFormatter.getDate(),
                    distance = String.format("%.1f", gpsViewModel.locationUpdate.distance),
                    speed = String.format("%.1f", gpsViewModel.locationUpdate.velocity * 3.6f),
                    geoPoints =
                    GeoPointsUtils.geoPointsToString(gpsViewModel.locationUpdate.geoPointsList),
                    geoMap = gpsViewModel.getScreenshot()
                )

                SaveTrackDialog(trackItem,
                    listenerClick = { isSave ->
                        if (isSave) {
                            gpsViewModel.insert(trackItem)
                        }
                    },
                    {
                        showSaveTrackDialog = it
                    }
                )
            }
        }
    }
}

@Composable
fun ColumnTextTitle() {
    Column(
        modifier = Modifier
//            .background(Color.Red)
            .padding(4.dp),
        verticalArrangement = Arrangement.SpaceBetween

    ) {

        Text(
            text = stringResource(id = R.string.time),
            fontSize = 18.sp
        )
        Text(
            text = stringResource(id = R.string.velosity),
            fontSize = 18.sp
        )
        Text(
            text = stringResource(id = R.string.avr_velocity),
            fontSize = 18.sp
        )
        Text(
            text = stringResource(id = R.string.distance),
            fontSize = 28.sp
        )
    }
}

@Composable
private fun ColumnTextValue(
    viewModel: GpsScreenViewModel
) {
    val time = viewModel.updateTimeLiveData.observeAsState()

    /**  LiveData from Service */
    val locationFromService = locationLiveData.observeAsState().value
    if (locationFromService != null) {
        // передача в ViewModel
        viewModel.locationUpdateFromService(locationFromService)
    }

    val velocity = "${String.format("%.1f", (locationFromService?.velocity ?: 0f) * 3.6f)} км/год"
    val distance = "${String.format("%.1f", locationFromService?.distance)} м"
    val averageVelocity = "${String.format("%.1f", viewModel.getAverageVelocity(locationFromService) * 3.6f)} км/год"


    Column(
        modifier = Modifier
//            .background(Color.Blue)
            .padding(4.dp),
        verticalArrangement = Arrangement.SpaceBetween

    ) {

        Text(
            text = time.value.toString(),
            fontSize = 18.sp
        )
        Text(
            text = velocity,
            fontSize = 18.sp
        )
        Text(
            text = averageVelocity,
            fontSize = 18.sp
        )
        Text(
            text = distance,
            fontSize = 28.sp
        )
    }
}


@Composable
private fun ColumnTwoFab(
    gpsViewModel: GpsScreenViewModel,
    mLocOverlay: MyLocationNewOverlay?,
    showSaveDialog: (Boolean) -> Unit
) {
    val context = LocalContext.current
    var isServiceRunning by remember { mutableStateOf(false) }

    // Проверка, запущен ли сервис в фоне
    isServiceRunning = LocationService.isRunning

    // переменная для создания Screenshot
    var isScreenshot by remember { mutableStateOf(false) }
    if (isScreenshot) {
        gpsViewModel.createScreenShot(LocalView.current)
        isScreenshot = false
    }

    // CenterLocation
    var centerLocation by remember { mutableStateOf(false) }
    if (centerLocation) {
        CenterLocation(mLocOverlay)
        centerLocation = false
    }

    Column(
        modifier = Modifier
//            .background(Color.Blue)
            .padding(4.dp),
    ) {

        // отображаем, толькл когда определится местоположение
        if (mLocOverlay != null) {
            FloatingActionButton(
                onClick = {
                    centerLocation = true
                },
                modifier = Modifier.padding(bottom = 4.dp),
                backgroundColor = Transparent100,
                elevation = FloatingActionButtonDefaults.elevation(0.dp)
            ) {
                Icon(
                    painterResource(id = R.drawable.ic_my_location),
                    contentDescription = null,
                    modifier = Modifier.size(46.dp)
                )

            }
        }


        FloatingActionButton(
            onClick = {
                isServiceRunning = !isServiceRunning

                /** Запускаем Service */
                startStopService(
                    context,
                    isServiceRunning,
                    gpsViewModel,
//                    receiver,
                    showSaveDialog,
                    { isScreenshot = it }
                )

            },
            modifier = Modifier,
            backgroundColor = Transparent100,
            elevation = FloatingActionButtonDefaults.elevation(0.dp)
        ) {
            Icon(
                painterResource(
                    if (!isServiceRunning) {
                        R.drawable.ic_play
                    } else {
                        R.drawable.ic_stop
                    }
                ),
                contentDescription = null,
                modifier = Modifier.size(46.dp)
//                    .border(width = 1.dp, color = Color.Black)
            )

        }
    }
}

/** Service */
private fun startStopService(
    context: Context,
    isServiceRunning: Boolean,
    viewModel: GpsScreenViewModel,
//    receiver: BroadcastReceiver,
    showSaveDialog: (Boolean) -> Unit,
    isScreenshot: (Boolean) -> Unit
) {
    if (isServiceRunning) {
        startLockService(context, viewModel)

        // Записать начальное время в DataStore, чтоб HE обнулялся при выходе из App
        viewModel.saveStartTimeToDataStore(System.currentTimeMillis())
        viewModel.startTimer()
//        showSaveDialog(false)
    } else {
        // Screenshot
        isScreenshot(true)

        context.stopService(Intent(context, LocationService::class.java))
//        stopReceiver(context, receiver)
        viewModel.stopTimer()
        showSaveDialog(true)
    }
}

private fun startLockService(context: Context, viewModel: GpsScreenViewModel) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        context.startForegroundService(viewModel.createIntentForService(context))
//        context.startForegroundService(Intent(context, LocationService::class.java))
    } else {
        context.startService(viewModel.createIntentForService(context))
//        context.startService(Intent(context, LocationService::class.java))
    }
}
/** End Service */

/** MapView  */
@Composable
private fun MapViewXML(
    context: Context,
    modifier: Modifier = Modifier,
    onLoad: ((map: MapView) -> Unit)? = null,
    viewModel: GpsScreenViewModel
) {
    val mapViewState = rememberMapViewWithLifecycle(context, viewModel)

    // MAP
    AndroidView(
        factory = { mapViewState },
        modifier
    ) { mapView ->
        onLoad?.invoke(mapView)
    }
}

/**  MapLifecycle */
@Composable
private fun rememberMapViewWithLifecycle(
    context: Context,
    viewModel: GpsScreenViewModel
): MapView {
    val mapView = remember {
        MapView(context).apply {
            id = R.id.map
        }
    }

    // Заставляет MapView следовать жизненному циклу этого компонуемого
    val lifecycleObserver = rememberMapLifecycleObserver(mapView, viewModel)
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
private fun rememberMapLifecycleObserver(
    mapView: MapView,
    viewModel: GpsScreenViewModel
): LifecycleEventObserver =
    remember(mapView) {
        LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                else -> {}
            }
        }
    }

@Composable
private fun IniOsm(
    context: Context,
    viewModel: GpsScreenViewModel,
    mLocationOverlay: (MyLocationNewOverlay) -> Unit
) {

//    Log.d(TAG, " GpsScreen, InitOSM()")

    val pl = viewModel.updatePolylineNew(viewModel.locationUpdate.geoPointsList)
    pl.outlinePaint?.color = getColor(context, R.color.purple_500)

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
            map.overlays.add(mLocOverlay) // Местоположение

            if ( LocationService.isRunning) {
                map.overlays.add(pl) // Линия
            }

            // Передаем mLocationOverlay наверх
            mLocationOverlay(mLocOverlay)

            // Всегда показывать Zoom (+ -)
            map.zoomController.setVisibility((CustomZoomButtonsController.Visibility.ALWAYS))
        }

        /** Compass */
         /*val compassOverlay =
             CompassOverlay(context, InternalCompassOrientationProvider(context), map)
         compassOverlay.enableCompass()
         map.overlays.add(compassOverlay)*/

        /* TEST Kyiv
        map.controller.animateTo(GeoPoint(
            50.4501,
            30.5241
        ))*/
    }
}

@Composable
private fun CenterLocation(mLocOverlay: MyLocationNewOverlay?) {
    AndroidViewBinding(MapBinding::inflate) {

        if (mLocOverlay != null) {
            map.controller.animateTo(mLocOverlay.myLocation)
            mLocOverlay.enableFollowLocation()
        }
    }
//    Log.d(TAG, "GsScreen CenterLocation, mLocOverlay: ${mLocOverlay?.myLocation}")
}
/** END Работа с картой  */


@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    GpsTrackerTheme {
        GpsScreen()
    }
}
