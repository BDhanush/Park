package com.example.park.foregroundservices

import android.Manifest
import android.app.Application
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION
import android.location.Location
import android.location.LocationManager
import android.os.IBinder
import androidx.car.app.connection.CarConnection
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import com.example.park.MainActivity
import com.example.park.R
import com.example.park.model.Parking
import java.util.function.Consumer


class ParkingDetectorService:LifecycleService() {
    private var firstRun = true

    override fun onBind(intent: Intent): IBinder? {
        super.onBind(intent)
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when(intent?.action)
        {
            Actions.START.toString() -> start()
            Actions.STOP.toString() -> stopSelf()
        }
        return super.onStartCommand(intent, flags, startId)

    }

    private fun start()
    {
        CarConnection(this).type.observe(this,::onConnectionStateUpdated)
    }

    private fun onConnectionStateUpdated(connectionState: Int) {
        val message = when(connectionState) {
            CarConnection.CONNECTION_TYPE_NOT_CONNECTED -> {
                if(!firstRun)
                    saveParking(application)
                "Not connected to a car"
            }
            CarConnection.CONNECTION_TYPE_NATIVE -> {
                "Connected to Android Automotive OS"
            }
            CarConnection.CONNECTION_TYPE_PROJECTION -> {
                "Connected to Android Auto"
            }
            else -> "Unknown car connection type"
        }
        firstRun = false

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        val notification =NotificationCompat.Builder(this, "Parking_Service_Channel")
            .setSmallIcon(R.drawable.car_24)
            .setContentTitle("Parking Detector is active")
            .setContentText(message)
            .setContentIntent(pendingIntent)
            .build()
        startForeground(1,notification,FOREGROUND_SERVICE_TYPE_LOCATION)

    }


    companion object{
        fun saveParking(application:Application):Boolean {
            val lm = application.getSystemService(LOCATION_SERVICE) as LocationManager

            if (ActivityCompat.checkSelfPermission(
                    application.applicationContext,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                lm.getCurrentLocation(
                    LocationManager.GPS_PROVIDER,
                    null,
                    application.mainExecutor,
                    Consumer<Location> { location ->
                        val longitude: Double = location?.longitude ?: Double.MIN_VALUE
                        val latitude: Double = location?.latitude ?: Double.MIN_VALUE
                        val altitude: Double = location?.altitude ?: Double.MIN_VALUE

                        if (minOf(longitude, latitude, altitude) != Double.MIN_VALUE) {
                            val parking: Parking = Parking("Last Saved", latitude, longitude, altitude, 0)
                            MainActivity.database.parkingDao().insert(parking)
                        }
                    }
                )
            }else{
                return false
            }
            return true
        }
    }

    enum class Actions{
        START,STOP
    }
}