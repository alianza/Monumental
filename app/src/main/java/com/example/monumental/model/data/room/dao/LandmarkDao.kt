package com.example.monumental.model.data.room.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.monumental.model.entity.Landmark

@Dao
interface LandmarkDao {

    /**
     * Inserts a new Landmark
     *
     * @param landmark Landmark to insert
     * @return ID of inserted row
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLandmark(landmark: Landmark)

    /**
     * Gets all Landmarks
     *
     * @return List of Landmarks
     */
    @Query("SELECT * FROM Landmark")
    fun getLandmarks(): LiveData<List<Landmark>?>

    /**
     * Gets single Landmark
     *
     * @param name Name of Landmark to retrieve
     * @return Landmark
     */
    @Query("SELECT * FROM Landmark WHERE name LIKE :name LIMIT 1")
    fun getLandmark(name: String): LiveData<Landmark?>

    /**
     * Gets all Landmarks of Journey
     *
     * @param journeyId ID of Landmark to get Landmarks of
     * @return List of Landmarks
     */
    @Query("SELECT * FROM Landmark WHERE journey_id = :journeyId")
    fun getLandmarksByJourney(journeyId: Int): LiveData<List<Landmark>?>

    /**
     * Updates a Landmark
     *
     * @param landmark Landmark to update
     * @return Number of affected rows
     */
    @Update
    suspend fun updateLandmark(landmark: Landmark):Int

    /**
     * Removes a Landmark
     *
     * @param landmark Landmark to remove
     */
    @Delete
    suspend fun deleteLandmark(landmark: Landmark)

}
