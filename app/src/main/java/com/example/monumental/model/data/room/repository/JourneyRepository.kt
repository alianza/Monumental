package com.example.monumental.model.data.room.repository

import android.content.Context
import androidx.lifecycle.LiveData
import com.example.monumental.model.data.room.MonumentalRoomDatabase
import com.example.monumental.model.data.room.dao.JourneyDao
import com.example.monumental.model.entity.Journey

class JourneyRepository(context: Context) {

    private val journeyDao: JourneyDao

    init {
        val database = MonumentalRoomDatabase.getDatabase(context)
        journeyDao = database!!.journeyDao()
    }

    /**
     * Inserts a new Journey
     *
     * @param journey Journey to insert
     * @return ID of inserted row
     */
    suspend fun insertJourney(journey: Journey): Long {
        return journeyDao.insertJourney(journey)
    }

    /**
     * Gets all Journeys
     *
     * @return List of Journeys
     */
    fun getJourneys(): LiveData<List<Journey>?> {
        return journeyDao.getJourneys()
    }

    /**
     * Gets single Journey
     *
     * @param name Name of Journey to retrieve
     * @return Journey
     */
    fun getJourney(name: String): LiveData<Journey?> {
        return journeyDao.getJourney(name)
    }

    /**
     * Gets the active Journey
     *
     * @return Journey
     */
    fun getActiveJourney(): LiveData<Journey?> {
        return journeyDao.getActiveJourney()
    }

    /**
     * Updates a Journey
     *
     * @param journey Journey to update
     * @return Number of affected rows
     */
    suspend fun updateJourney(journey: Journey) {
        journeyDao.updateJourney(journey)
    }

    /**
     * Sets the active Journey, unset all other journeys
     *
     * @param journey Journey to set to active
     */
    suspend fun setActiveJourney(journey: Journey) {
        journeyDao.setActiveJourney(journey.id!!)
    }

    /**
     * Removes a Journey
     *
     * @param journey Journey to remove
     */
    suspend fun deleteJourney(journey: Journey):Int {
        return journeyDao.deleteJourney(journey)
    }
}
