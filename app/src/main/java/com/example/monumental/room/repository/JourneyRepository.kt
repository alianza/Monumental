package com.example.monumental.room.repository

import android.content.Context
import androidx.lifecycle.LiveData
import com.example.monumental.model.Journey
import com.example.monumental.room.MonumentalRoomDatabase
import com.example.monumental.room.dao.JourneyDao

class JourneyRepository(context: Context) {

    private val journeyDao: JourneyDao

    init {
        val database = MonumentalRoomDatabase.getDatabase(context)
        journeyDao = database!!.journeyDao()
    }

    fun getJourneys(): LiveData<List<Journey>?> {
        return journeyDao.getJourneys()
    }

    fun getActiveJourney(): LiveData<Journey?> {
        return journeyDao.getActiveJourney()
    }

    fun getJourney(name: String): LiveData<Journey?> {
        return journeyDao.getJourney(name)
    }

    suspend fun updateJourney(journey: Journey) {
        journeyDao.updateJourney(journey)
    }

    suspend fun setActiveJourney(journey: Journey) {
        journeyDao.setActiveJourney(journey.id!!)
    }

    suspend fun insertJourney(journey: Journey) {
        journeyDao.insertJourney(journey)
    }

    suspend fun deleteJourney(journey: Journey) {
        journeyDao.deleteJourney(journey)
    }
}
