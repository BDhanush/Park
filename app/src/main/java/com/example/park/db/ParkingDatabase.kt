package com.example.park.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.park.model.Parking

@Database(entities = [Parking::class], version = 1)
abstract class ParkingDatabase : RoomDatabase() {
    companion object{
        val NAME = "ParkingDatabase"
    }
    abstract fun parkingDao(): ParkingDao

}