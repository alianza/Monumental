package com.example.monumental.model.data.room.repository

import android.content.Context
import androidx.lifecycle.LiveData
import com.example.monumental.model.data.room.MonumentalRoomDatabase
import com.example.monumental.model.data.room.dao.LandmarkDao
import com.example.monumental.model.entity.Landmark

class LandmarkRepository(context: Context) {

    private val landmarkDao: LandmarkDao

    init {
        val database = MonumentalRoomDatabase.getDatabase(context)
        landmarkDao = database!!.landmarkDao()
    }

    fun getLandmarks(): LiveData<List<Landmark>?> {
        return landmarkDao.getLandmarks()
    }

    fun getLandmarksByJourney(journeyId: Int): LiveData<List<Landmark>?> {
        return landmarkDao.getLandmarksByJourney(journeyId)
    }

    fun getLandmark(name: String): LiveData<Landmark?> {
        return landmarkDao.getLandmark(name)
    }

    suspend fun updateLandmark(landmark: Landmark) {
        landmarkDao.updateLandmark(landmark)
    }

    suspend fun insertLandmark(landmark: Landmark) {
        landmarkDao.insertLandmark(landmark)
    }

    suspend fun deleteLandmark(landmark: Landmark) {
        landmarkDao.deleteLandmark(landmark)
    }
}
