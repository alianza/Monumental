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

    suspend fun insertJourney(journey: Journey): Long {
        return journeyDao.insertJourney(journey)
    }

    suspend fun deleteJourney(journey: Journey) {
        journeyDao.deleteJourney(journey)
    }
}
