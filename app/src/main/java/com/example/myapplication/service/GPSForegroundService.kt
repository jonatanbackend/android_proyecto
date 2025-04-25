package com.example.myapplication.service

import android.Manifest
import android.app.*
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import com.example.myapplication.R
import com.google.android.gms.location.*

class GPSForegroundService : Service() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback

    private val channelId = "gps_tracking_channel"
    private val notificationId = 1

    override fun onCreate() {
        super.onCreate()
        Log.d("GPSForegroundService", "Servicio creado")

        createNotificationChannel()
        startForeground(notificationId, createNotification("Inicializando ubicación..."))

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let { location ->
                    Log.d("GPSForegroundService", "Ubicación recibida: ${location.latitude}, ${location.longitude}")
                    updateNotification(location)
                }
            }
        }

        startLocationUpdates()
    }

    private fun startLocationUpdates() {
        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            5000L
        ).build()

        // Verificación explícita de permisos
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.e("GPSForegroundService", "Permisos de ubicación no concedidos")
            stopSelf()
            return
        }

        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            mainLooper
        )
    }

    private fun updateNotification(location: Location) {
        val notification = createNotification(
            "Ubicación actual: ${location.latitude}, ${location.longitude}"
        )
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(notificationId, notification)
    }

    private fun createNotification(content: String): Notification {
        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("Seguimiento de GPS")
            .setContentText(content)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setOnlyAlertOnce(true)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                channelId,
                "Canal de seguimiento GPS",
                NotificationManager.IMPORTANCE_HIGH
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(serviceChannel)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("GPSForegroundService", "Servicio iniciado")
        return START_STICKY
    }

    override fun onDestroy() {
        Log.d("GPSForegroundService", "Servicio destruido")
        fusedLocationClient.removeLocationUpdates(locationCallback)
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
