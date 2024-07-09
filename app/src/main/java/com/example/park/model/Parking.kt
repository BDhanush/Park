package com.example.park.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
class Parking(var title:String,val latitude: Double,val longitude: Double,val altitude:Double,@PrimaryKey var id:Long,var note:String="") {
}