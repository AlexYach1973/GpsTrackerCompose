package com.alexyach.compose.gpstracker.data.location

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.alexyach.compose.gpstracker.MainActivity
import com.alexyach.compose.gpstracker.R
import com.alexyach.compose.gpstracker.screens.gpssettings.TAG
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import org.osmdroid.util.GeoPoint

class LocationService : Service() {

    private var lastLocation: Location? = null
    private var distance = 0.0f
    private lateinit var geoPointsList: ArrayList<GeoPoint>
    private lateinit var locProvider: FusedLocationProviderClient
    private lateinit var locRequest: LocationRequest

    private val binder = LocationServiceBinder()

    override fun onBind(intent: Intent?): IBinder? {
        return binder
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startNotification()
        startLocationUpdate()
        isRunning = true
        return START_STICKY
    }

    override fun onCreate() {
        super.onCreate()
        initLocation()
        geoPointsList = ArrayList()

        Log.d(TAG, "LocationService, onCreate()")
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
        ).setSmallIcon(R.drawable.ic_home)
            .setContentTitle("Tracker running")
            .setContentText("Tracker running")
            .setContentIntent(pIntent)
            .build()

        startForeground(99, notification)
    }

    override fun onDestroy() {
        super.onDestroy()
        isRunning = false
        locProvider.removeLocationUpdates(locCallback)
        Log.d(TAG, "LocationService, onDestroy()")
    }

    /** Местоположение */
    private fun initLocation() {
        locRequest = LocationRequest.create()
        locRequest.interval = 5000
        locRequest.fastestInterval = 5000
        locRequest.priority = Priority.PRIORITY_HIGH_ACCURACY

        locProvider = LocationServices.getFusedLocationProviderClient(baseContext)
    }

    // Получаем информацию здесь
    private val locCallback = object : LocationCallback() {
        override fun onLocationResult(lResult: LocationResult) {
            super.onLocationResult(lResult)

            val currentLocation = lResult.lastLocation

            if (lastLocation != null && currentLocation != null) {
                // Исправляем погрешность GPS
//                if (currentLocation.speed > 0.2) {
                    distance += lastLocation?.distanceTo(currentLocation)!!
                    geoPointsList.add(
                        GeoPoint(
                            currentLocation.latitude,
                            currentLocation.longitude
                        )
                    )
//                }
                val localModel = LocationModel(
                    currentLocation.speed,
                    distance,
                    geoPointsList
                )
                // Отправляем
                sendLocData(localModel)

            }
            lastLocation = currentLocation

//            Log.d(TAG, "LocationService, Distance: ${distance}")
        }
    }

    private fun startLocationUpdate() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        locProvider.requestLocationUpdates(
            locRequest,
            locCallback,
            Looper.myLooper()
        )
    }

    /** Передача данных на экран */
    private fun sendLocData(locModel: LocationModel) {
        val i = Intent(LOC_MODEL_INTENT)
        i.putExtra(LOC_MODEL_INTENT, locModel)
        LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(i)
    }
    /** End Местоположение */



    companion object {
        const val LOC_MODEL_INTENT = "loc_intent"
        const val CHANNEL_ID = "channel_1"
        var isRunning = false
        var startTime = 0L
    }

    inner class LocationServiceBinder : Binder() {
        fun getService(): LocationService =
            this@LocationService
    }

}