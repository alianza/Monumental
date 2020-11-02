package com.example.monumental.room.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.monumental.model.Journey

@Dao
interface JourneyDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertJourney(journey: Journey)

    @Query("SELECT * FROM Journey")
    fun getJourneys(): LiveData<List<Journey>?>

    @Query("SELECT * FROM Journey WHERE name LIKE :name LIMIT 1")
    fun getJourney(name: String): LiveData<Journey?>

    @Update
    suspend fun updateJourney(journey: Journey):Int

    @Delete
    suspend fun deleteJourney(journey: Journey)
}