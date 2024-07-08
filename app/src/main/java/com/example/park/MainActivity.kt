package com.example.park

import android.Manifest
import android.app.ActivityManager
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.room.Room
import com.example.park.adapter.ParkingItemAdapter
import com.example.park.databinding.ActivityMainBinding
import com.example.park.db.ParkingDao
import com.example.park.db.ParkingDatabase
import com.example.park.foregroundservices.ParkingDetectorService
import com.example.park.model.Parking
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.util.function.Consumer


class MainActivity : AppCompatActivity() {
    companion object {
        lateinit var database: ParkingDatabase
    }

    private lateinit var binding: ActivityMainBinding

    var locationPermission = false
    var notificationPermission = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        setSavedToggle()

        database =  Room.databaseBuilder(
            applicationContext,
            ParkingDatabase::class.java,
            ParkingDatabase.NAME
        ).allowMainThreadQueries().build()

        val parkingDao: ParkingDao = database.parkingDao()

        val adapter = ParkingItemAdapter(listOf())
        binding.parkingRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.parkingRecyclerView.adapter = adapter

        parkingDao.getAll().observe(this) { dataset ->
            adapter.updateDataset(dataset)
        }

        binding.parkingRecyclerView.layoutManager = LinearLayoutManager(this)

        binding.saveButton.setOnClickListener {

            val alertDialog = MaterialAlertDialogBuilder(this)
                .setTitle("Save Parking Space?")
                .setPositiveButton("Add") { dialog, which ->
                    saveParking()
                }
                .setNegativeButton("Cancel") { dialog, which ->
                    dialog.dismiss()
                }
                .create()
            alertDialog.show()
        }

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
                        dialog.dismiss()
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
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

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

    private fun saveParking() {
        val lm = getSystemService(LOCATION_SERVICE) as LocationManager
        if (ActivityCompat.checkSelfPermission(
                this,
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
                    val altitude:Double = location?.altitude ?: Double.MIN_VALUE

                    if(minOf(longitude,latitude,altitude)!=Double.MIN_VALUE)
                    {
                        val parking: Parking = Parking("Last Saved",latitude, longitude, altitude,0)
                        database.parkingDao().insert(parking)
                    }
                }
            )
        }else{
            Toast.makeText(this,"Grant Location permissions for this feature", Toast.LENGTH_LONG).show()
            askPermissions()
        }
    }


}
