package com.example.monumental.model.data.room.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.monumental.model.entity.Journey

@Dao
interface JourneyDao {

    /**
     * Inserts a new Journey
     *
     * @param journey Journey to insert
     * @return ID of inserted row
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertJourney(journey: Journey): Long

    /**
     * Gets all Journeys
     *
     * @return List of Journeys
     */
    @Query("SELECT * FROM Journey")
    fun getJourneys(): LiveData<List<Journey>?>

    /**
     * Gets single Journey
     *
     * @param name Name of Journey to retrieve
     * @return Journey
     */
    @Query("SELECT * FROM Journey WHERE name LIKE :name LIMIT 1")
    fun getJourney(name: String): LiveData<Journey?>

    /**
     * Gets the active Journey
     *
     * @return Journey
     */
    @Query("SELECT * FROM Journey WHERE current = 1 LIMIT 1")
    fun getActiveJourney(): LiveData<Journey?>

    /**
     * Updates a Journey
     *
     * @param journey Journey to update
     * @return Number of affected rows
     */
    @Update
    suspend fun updateJourney(journey: Journey):Int

    /**
     * Sets the active Journey, unset all other journeys
     *
     * @param journeyId ID of Journey to set to active
     */
    @Query("UPDATE Journey SET current = CASE id WHEN :journeyId THEN 1 ELSE 0 END")
    suspend fun setActiveJourney(journeyId: Int)

    /**
     * Removes a Journey
     *
     * @param journey Journey to remove
     */
    @Delete
    suspend fun deleteJourney(journey: Journey):Int
}
