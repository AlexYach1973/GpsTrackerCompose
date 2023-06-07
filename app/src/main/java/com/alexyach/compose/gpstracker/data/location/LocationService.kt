package com.alexyach.compose.gpstracker.data.location

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.MutableLiveData
import com.alexyach.compose.gpstracker.MainActivity
import com.alexyach.compose.gpstracker.R
import com.alexyach.compose.gpstracker.data.preferences.UserPreferencesRepository
import com.alexyach.compose.gpstracker.screens.gpsscreen.GpsScreenViewModel
import com.alexyach.compose.gpstracker.screens.gpssettings.TAG
import com.alexyach.compose.gpstracker.utils.GeoPointsUtils
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.osmdroid.util.GeoPoint


private const val PREFERENCES_LOCATION_NAME = "prefLocationName"
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = PREFERENCES_LOCATION_NAME
)

class LocationService : Service() {

    private lateinit var prefRepositoryLocation: UserPreferencesRepository

    private lateinit var locProvider: FusedLocationProviderClient
    private lateinit var locRequest: LocationRequest
    private var lastLocation: Location? = null
    private var geoPointsList: ArrayList<GeoPoint> = ArrayList()
    private var distance: Float = 0.0f
    private var intervalMillis = 0

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intervalMillis = intent?.extras?.getInt(GpsScreenViewModel.UPDATE_TIME_KEY) ?: 3000
        Log.d(TAG, " Service intervalMillis = $intervalMillis")

        startNotification()
        startLocationUpdate()
        isRunning = true

        isRunningLiveData.value = true

        return START_STICKY
    }

    override fun onCreate() {
        super.onCreate()

        prefRepositoryLocation = UserPreferencesRepository(dataStore)

        observeDataFromDataStore()
        initLocation()

        Log.d(TAG, "LocationService, onCreate()")
    }

    override fun onDestroy() {
        super.onDestroy()
        isRunning = false
        locProvider.removeLocationUpdates(locCallback)

        isRunningLiveData.value = false

        // Обнулить DataStore
        saveLocDataToDataStore(
            LocationModel(
                velocity = 0.0f,
                distance = 0.0f,
                geoPointsList = emptyList<GeoPoint>()
            )
        )

        Log.d(TAG, "LocationService, onDestroy()")
    }


    private fun startNotification() {
        // VERSION_CODES.O (Oreo) - Android 8 (API 26)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nChanel = NotificationChannel(
                CHANNEL_ID,
                "Location Service",
                NotificationManager.IMPORTANCE_DEFAULT
            )

            val nManager = getSystemService(NotificationManager::class.java) as NotificationManager
            nManager.createNotificationChannel(nChanel)
        }

        val nIntent = Intent(this, MainActivity::class.java)
        val pIntent = PendingIntent.getActivity(
            this,
            0,
            nIntent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(
            this,
            CHANNEL_ID
        ).setSmallIcon(R.drawable.cross)
            .setColor(resources.getColor(R.color.purple_200, theme))
            .setContentTitle(resources.getString(R.string.title_notification))
//            .setContentText("")
            .setContentIntent(pIntent)
            .build()

        startForeground(99, notification)
    }


    /** Местоположение */
    private fun initLocation() {
        locRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            intervalMillis.toLong()
        ).build()

        locProvider = LocationServices.getFusedLocationProviderClient(baseContext)
    }

    /** Получаем информацию здесь */
    private val locCallback = object : LocationCallback() {
        override fun onLocationResult(lResult: LocationResult) {
            super.onLocationResult(lResult)

            val currentLocation = lResult.lastLocation

            if (lastLocation != null && currentLocation != null) {
                /** Исправляем погрешность GPS */
//                if (currentLocation.speed > 0.4) { // м/с
                distance += lastLocation?.distanceTo(currentLocation)!!
                geoPointsList.add(
                    GeoPoint(currentLocation.latitude, currentLocation.longitude)
                )
//                }
                val locModel = LocationModel(
                    currentLocation.speed,
                    distance,
                    geoPointsList
                )
                saveLocDataToDataStore(locModel)

                /** LiveData */
                locationLiveData.value = locModel
//                Log.d(TAG, "Service location: ${locationLiveData.value}")
            }
            lastLocation = currentLocation

//            Log.d(TAG, "LocationService callback, Distance: ${distance}")
        }
    }

    private fun startLocationUpdate() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.d(TAG, "Service NO Permission!")
            return
        }
        locProvider.requestLocationUpdates(
            locRequest,
            locCallback,
            Looper.myLooper()
        )
    }
    /** End Местоположение */


    /** Сохранение данных в DataStore */
    private fun saveLocDataToDataStore(locModel: LocationModel) {
        CoroutineScope(Dispatchers.IO).launch {
            prefRepositoryLocation.saveDistance(locModel.distance)
        }

        CoroutineScope(Dispatchers.IO).launch {
            prefRepositoryLocation.saveGeopoints(
                GeoPointsUtils.geoPointsToString(
                    locModel.geoPointsList
                )
            )
        }
//        Log.d(TAG, "Service save data, geopoints.size= ${locModel.geoPointsList.size}")
    }

    /** Чтение данных из DataStore */
    private fun observeDataFromDataStore() {
        CoroutineScope(Dispatchers.Main).launch {
            prefRepositoryLocation.distance.collect {
                distance = it
//                Log.d(TAG, "Service observe distance= $it")
            }
        }

        CoroutineScope(Dispatchers.Main).launch {
            prefRepositoryLocation.geoPoints.collect {
                geoPointsList = GeoPointsUtils.stringToGeoPoints(it)
//                Log.d(TAG, "Service observe geopoints.size= ${geoPointsList.size}")
            }
        }
    }

    companion object {
        const val CHANNEL_ID = "channel_1"
        var isRunning = false

        var isRunningLiveData = MutableLiveData(false)


        var locationLiveData: MutableLiveData<LocationModel> = MutableLiveData(
            LocationModel(
                velocity = 0f,
                distance = 0f,
                geoPointsList = emptyList()
            )
        )
    }

}