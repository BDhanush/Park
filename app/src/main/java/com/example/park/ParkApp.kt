package com.example.park

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context

class ParkApp:Application() {
    override fun onCreate() {
        super.onCreate()
        val serviceChannel = NotificationChannel(
            "Parking_Service_Channel",
            "Park Notifications",
            NotificationManager.IMPORTANCE_HIGH
        )
        val parkedChannel = NotificationChannel(
            "Parked_indicator",
            "Parked Indicator",
            NotificationManager.IMPORTANCE_HIGH
        )
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(serviceChannel)
        notificationManager.createNotificationChannel(parkedChannel)

    }
}