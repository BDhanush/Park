package com.example.park.db

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.park.model.Parking

@Dao
interface ParkingDao {
    @Query("SELECT * FROM parking ORDER BY id DESC")
    fun getAll(): LiveData<List<Parking>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(parking: Parking):Long

    @Delete
    fun delete(parking: Parking)
}