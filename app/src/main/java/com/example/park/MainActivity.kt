package com.example.park

import android.Manifest
import android.app.ActivityManager
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.park.databinding.ActivityMainBinding
import com.example.park.foregroundservices.ParkingDetectorService
import com.google.android.material.dialog.MaterialAlertDialogBuilder


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    var locationPermission = false
    var notificationPermission = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        setSavedToggle()

        binding.autoSaveToggle.setOnCheckedChangeListener { buttonView, isChecked ->

//            val preferences = getPreferences(MODE_PRIVATE)
//            val editor: SharedPreferences.Editor = preferences.edit()
//            editor.putBoolean("autoSaveToggle", isChecked)
//            editor.apply()

            checkPermissions()
            if(notificationPermission  && locationPermission) {
                if (isChecked) {
                    Intent(applicationContext, ParkingDetectorService::class.java).also {
                        it.action = ParkingDetectorService.Actions.START.toString()
                        startService(it)
                    }
                } else {
                    Intent(applicationContext, ParkingDetectorService::class.java).also {
                        it.action = ParkingDetectorService.Actions.STOP.toString()
                        startService(it)
                    }
                }
            }
        }

        binding.autoSaveToggle.setOnClickListener {
            checkPermissions()
            if(!notificationPermission || !locationPermission) {
                binding.autoSaveToggle.isChecked = false
                val alert = MaterialAlertDialogBuilder(this)
                    .setTitle("Note")
                    .setMessage("Notification and Location permissions are required for this feature. Precise Location needs to be on for this feature to work. App should always have location access. Saves Parking space when phone is disconnected from Android Auto.")
                    .setPositiveButton("OK") { dialog, which ->

                    }
                    .show()
                alert.setOnDismissListener {
                    askPermissions()
                }
            }
        }


    }
    private fun isServiceRunning(serviceClass: Class<*>): Boolean {
        val manager = getSystemService(ACTIVITY_SERVICE) as ActivityManager
        for (service in manager.getRunningServices(Int.MAX_VALUE)) {
            if (serviceClass.name == service.service.className) {
                return true
            }
        }
        return false
    }

    private fun setSavedToggle()
    {
//        val preferences = getPreferences(MODE_PRIVATE)
//
//        val autoSaveSetting = preferences.getBoolean("autoSaveToggle", false)
//
//        binding.autoSaveToggle.isChecked = autoSaveSetting

        binding.autoSaveToggle.isChecked = isServiceRunning(ParkingDetectorService::class.java)
    }

    private fun checkPermissions()
    {
        notificationPermission = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED

        locationPermission = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
    }

//    val requestPermissionLauncherNotification =
//        registerForActivityResult(
//            ActivityResultContracts.RequestPermission()
//        ) { isGranted: Boolean ->
//            notificationPermission = isGranted
//        }
//
//    val requestPermissionLauncherLocation =
//        registerForActivityResult(
//            ActivityResultContracts.RequestPermission()
//        ) { isGranted: Boolean ->
//            locationPermission = isGranted
//        }
//
//    private fun askLocationPermission()
//    {
//        if (ContextCompat.checkSelfPermission(
//                this@MainActivity,
//                Manifest.permission.ACCESS_FINE_LOCATION
//            ) == PackageManager.PERMISSION_GRANTED
//        ) {
//            locationPermission=true
//        } else {
//            requestPermissionLauncherLocation.launch(
//                Manifest.permission.ACCESS_FINE_LOCATION
//            )
//        }
//    }
//
//    private fun askNotificationPermission()
//    {
//        if (ContextCompat.checkSelfPermission(
//                this@MainActivity,
//                Manifest.permission.POST_NOTIFICATIONS
//            ) == PackageManager.PERMISSION_GRANTED
//        ) {
//            notificationPermission=true
//        } else {
//            requestPermissionLauncherNotification.launch(
//                Manifest.permission.POST_NOTIFICATIONS
//            )
//        }
//    }

    private fun askPermissions()
    {
        val permissions = mutableListOf<String>()
        if (ContextCompat.checkSelfPermission(
                this@MainActivity,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
        }
        if (ContextCompat.checkSelfPermission(
                this@MainActivity,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            permissions.add(Manifest.permission.ACCESS_FINE_LOCATION)
        }
        ActivityCompat.requestPermissions(this, permissions.toTypedArray(), 1);

    }


}
