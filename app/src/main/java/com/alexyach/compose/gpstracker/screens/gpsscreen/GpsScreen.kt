package com.alexyach.compose.gpstracker.screens.gpsscreen

import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.ServiceConnection
import android.os.Build
import android.os.IBinder
import android.util.Log
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.viewinterop.AndroidViewBinding
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.alexyach.compose.gpstracker.MainActivity
import com.alexyach.compose.gpstracker.R
import com.alexyach.compose.gpstracker.data.location.LocationModel
import com.alexyach.compose.gpstracker.data.location.LocationService
import com.alexyach.compose.gpstracker.databinding.MapBinding
import com.alexyach.compose.gpstracker.screens.gpssettings.TAG
import com.alexyach.compose.gpstracker.ui.theme.GpsTrackerTheme
import com.alexyach.compose.gpstracker.ui.theme.Transparent100
import org.osmdroid.config.Configuration
import org.osmdroid.library.BuildConfig
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import java.util.Timer
import java.util.TimerTask

@Composable
fun GpsScreen(
    modifier: Modifier = Modifier
) {

    /** Srvice */
//    lateinit var locationService: LocationService
//    var isBound by mutableStateOf(false)

    val context = LocalContext.current
    val gpsViewModel: GpsScreenViewModel = viewModel()

    /** LocalBroadcastReceiver */
    val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == LocationService.LOC_MODEL_INTENT) {
                val locModel = intent.getSerializableExtra(
                    LocationService.LOC_MODEL_INTENT) as LocationModel

                gpsViewModel.locationUpdate = locModel
            }
        }
    }
    val locFilter = IntentFilter(LocationService.LOC_MODEL_INTENT)
    LocalBroadcastManager.getInstance(context).registerReceiver(receiver,locFilter)
    /** *** */

    // Конфигурации для карт
    settingOsm(context)
    // Привязываемся к XML
    MapViewXML(context)
    // Работа с самой картой
    IniOsm(context)


    Box(
        modifier = Modifier
    ) {
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
            ColumnTwoFab(gpsViewModel)
        }

    }
}

@Composable
private fun ColumnTextTitle() {
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
    val state = viewModel.updateTimeLiveData.observeAsState()
    val distance = "${String.format("%.1f",viewModel.locationUpdate.distance)} м"
    val velocity = "${String.format("%.1f",viewModel.locationUpdate.velocity * 3.6)} км/ч"

    Column(
        modifier = Modifier
//            .background(Color.Blue)
            .padding(4.dp),
        verticalArrangement = Arrangement.SpaceBetween

    ) {

        Text(
            text = state.value.toString(),
            fontSize = 18.sp
        )
        Text(
            text = velocity,
            fontSize = 18.sp
        )
        Text(
            text = "??? км/ч",
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
    gpsViewModel: GpsScreenViewModel
) {
    val context = LocalContext.current
    var isServiceRunning by remember { mutableStateOf(false) }

    // Проверка, запущен ли сервис в фоне
    isServiceRunning = LocationService.isRunning

    Column(
        modifier = Modifier
//            .background(Color.Blue)
            .padding(4.dp),
    ) {
        FloatingActionButton(
            onClick = {

            },
            modifier = Modifier.padding(bottom = 4.dp),
            backgroundColor = Transparent100,
            elevation = FloatingActionButtonDefaults.elevation(0.dp)
        ) {
            Icon(
                painterResource(id = R.drawable.ic_my_location),
                contentDescription = null,
                modifier = Modifier.size(46.dp)
//                    .border(width = 1.dp, color = Color.Black, shape = CircleShape)
            )

        }

        FloatingActionButton(
            onClick = {
                isServiceRunning = !isServiceRunning

                /** Запускаем Service */
                startStopService(
                    context,
                    isServiceRunning,
                    gpsViewModel
                )

            },
            modifier = Modifier,
            backgroundColor = Transparent100,
            elevation = FloatingActionButtonDefaults.elevation(0.dp)
        ) {
            Icon(
                painterResource(
                    id = if (!isServiceRunning) {
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
    viewModel: GpsScreenViewModel
) {
    if (isServiceRunning) {
        startLockService(context)

        // Записать начальное время в Сервис, чтоб не обнулялся при выходе из App
        LocationService.startTime = System.currentTimeMillis()
        viewModel.startTimer()
    } else {
        context.stopService(Intent(context, LocationService::class.java))
        viewModel.stopTimer()
    }
}

private fun startLockService(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        context.startForegroundService(Intent(context, LocationService::class.java))
    } else {
        context.startService(Intent(context, LocationService::class.java))
    }
}
/** End Service */


/** Работа с картой  */
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
/** END Работа с картой  */


/** LocalBroadcastReceiver *//*
val receiver = object : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == LocationService.LOC_MODEL_INTENT) {
            val locModel = intent.getSerializableExtra(
                LocationService.LOC_MODEL_INTENT) as LocationModel

            gpsV

            Log.d(TAG, "GpsScreen, L= ${locModel.distance}, " +
                    "v = ${locModel.velocity} km/h")
        }
    }
}*/
/*@Composable
fun RegisterLocReceiver(gpsViewModel: GpsScreenViewModel) {
    val context = LocalContext.current
    val locFilter = IntentFilter(LocationService.LOC_MODEL_INTENT)
    LocalBroadcastManager.getInstance(context).registerReceiver(receiver,locFilter)
}*/

/** Service */
/*val connection = object : ServiceConnection {
    override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
        val binder = service as LocationService.LocationServiceBinder
        locationService = binder.getService()
        isBound = true
    }

    override fun onServiceDisconnected(name: ComponentName?) {
        isBound = false
    }
}*/


@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    GpsTrackerTheme {
        GpsScreen()
    }
}
