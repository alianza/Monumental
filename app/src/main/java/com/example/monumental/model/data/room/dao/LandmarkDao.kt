package com.example.monumental.model.data.room.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.monumental.model.entity.Landmark

@Dao
interface LandmarkDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLandmark(landmark: Landmark)

    @Query("SELECT * FROM Landmark")
    fun getLandmarks(): LiveData<List<Landmark>?>

    @Query("SELECT * FROM Landmark WHERE name LIKE :name LIMIT 1")
    fun getLandmark(name: String): LiveData<Landmark?>

    @Query("SELECT * FROM Landmark WHERE journey_id = :journeyId")
    fun getLandmarksByJourney(journeyId: Int): LiveData<List<Landmark>?>

    @Update
    suspend fun updateLandmark(landmark: Landmark):Int

    @Delete
    suspend fun deleteLandmark(landmark: Landmark)
}
