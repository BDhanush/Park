package com.example.park

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.view.View.GONE
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.park.databinding.ActivityParkingBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.abs

class ParkingActivity : AppCompatActivity() {
    private lateinit var binding: ActivityParkingBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityParkingBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        val longitude=intent.getDoubleExtra("longitude", Double.MIN_VALUE)
        val latitude=intent.getDoubleExtra("latitude", Double.MIN_VALUE)
        val altitude=intent.getDoubleExtra("altitude", Double.MIN_VALUE)

        binding.longitude.text=resources.getString(R.string.longitude,longitude)
        binding.latitude.text=resources.getString(R.string.latitude,latitude)
        binding.altitude.text=resources.getString(R.string.altitude,altitude)

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

        if(location==null)
        {
            binding.guideAltitude.visibility=GONE
        }else{
            updateAltitude(lm,altitude)
        }

        binding.navigateButton.setOnClickListener {
            val gmmIntentUri = Uri.parse("google.navigation:q=$latitude,$longitude&mode=w")
            val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
            startActivity(mapIntent)
        }


    }
    private fun updateAltitude(lm: LocationManager,altitude:Double) {
        val updateJob = CoroutineScope(Dispatchers.Main).launch {
            while (isActive) {
                if (ActivityCompat.checkSelfPermission(
                        applicationContext,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    val location=lm.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                    val dif: Double = location!!.altitude - altitude
                    val guideWord: String = if (dif > 0) "down" else "up"
                    binding.guideAltitude.text = resources.getString(R.string.guideAltitude, guideWord, abs(dif))
//                    delay(1000)
                }
            }
        }
    }
}