package com.example.park.foregroundservices

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.IBinder
import android.widget.Toast
import androidx.car.app.connection.CarConnection
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import com.example.park.MainActivity
import com.example.park.R
import com.example.park.model.Parking


class ParkingDetectorService:LifecycleService() {
    private var firstRun = false

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
                if(firstRun)
                    saveParking()
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
        firstRun = true

        val notification =NotificationCompat.Builder(this, "Parking_Service_Channel")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Parking Detector is active")
            .setContentText(message)
            .setOngoing(true)
            .build()
        startForeground(1,notification)

    }

    private fun saveParking() {
        Toast.makeText(applicationContext,"hi",Toast.LENGTH_SHORT).show()
        val lm = getSystemService(LOCATION_SERVICE) as LocationManager
        val location: Location? = if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            lm.getLastKnownLocation(LocationManager.GPS_PROVIDER)
        }else{
            null
        }
        val longitude: Double = location?.longitude ?: Double.MIN_VALUE
        val latitude: Double = location?.latitude ?: Double.MIN_VALUE
        val altitude:Double = location?.altitude ?: Double.MIN_VALUE

        if(minOf(longitude,latitude,altitude)!=Double.MIN_VALUE)
        {
            val parking:Parking = Parking("Last Saved",latitude, longitude, altitude,0)
            MainActivity.database.parkingDao().insert(parking)
        }
    }

    enum class Actions{
        START,STOP
    }
}