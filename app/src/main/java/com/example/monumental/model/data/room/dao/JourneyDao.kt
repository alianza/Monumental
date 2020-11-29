package com.example.monumental.model.data.room.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.monumental.model.entity.Journey

@Dao
interface JourneyDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertJourney(journey: Journey): Long

    @Query("SELECT * FROM Journey")
    fun getJourneys(): LiveData<List<Journey>?>

    @Query("SELECT * FROM Journey WHERE name LIKE :name LIMIT 1")
    fun getJourney(name: String): LiveData<Journey?>

    @Query("SELECT * FROM Journey WHERE current = 1 LIMIT 1")
    fun getActiveJourney(): LiveData<Journey?>

    @Update
    suspend fun updateJourney(journey: Journey):Int

    @Delete
    suspend fun deleteJourney(journey: Journey)

    @Query("UPDATE Journey SET current = CASE id WHEN :journeyId THEN 1 ELSE 0 END")
    suspend fun setActiveJourney(journeyId: Int)

}
