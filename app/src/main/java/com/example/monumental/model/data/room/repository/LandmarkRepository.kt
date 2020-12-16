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

    /**
     * Inserts a new Landmark
     *
     * @param landmark Landmark to insert
     * @return ID of inserted row
     */
    suspend fun insertLandmark(landmark: Landmark) {
        landmarkDao.insertLandmark(landmark)
    }

    /**
     * Gets all Landmarks
     *
     * @return List of Landmarks
     */
    fun getLandmarks(): LiveData<List<Landmark>?> {
        return landmarkDao.getLandmarks()
    }

    /**
     * Gets single Landmark
     *
     * @param name Name of Landmark to retrieve
     * @return Landmark
     */
    fun getLandmark(name: String): LiveData<Landmark?> {
        return landmarkDao.getLandmark(name)
    }

    /**
     * Gets all Landmarks of Journey
     *
     * @param journeyId ID of Landmark to get Landmarks of
     * @return List of Landmarks
     */
    fun getLandmarksByJourney(journeyId: Int): LiveData<List<Landmark>?> {
        return landmarkDao.getLandmarksByJourney(journeyId)
    }

    /**
     * Updates a Landmark
     *
     * @param landmark Landmark to update
     * @return Number of affected rows
     */
    suspend fun updateLandmark(landmark: Landmark) {
        landmarkDao.updateLandmark(landmark)
    }

    /**
     * Removes a Landmark
     *
     * @param landmark Landmark to remove
     */
    suspend fun deleteLandmark(landmark: Landmark) {
        landmarkDao.deleteLandmark(landmark)
    }
}
