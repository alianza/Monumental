package com.example.monumental.viewModel.journey

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.example.monumental.model.data.room.repository.JourneyRepository
import com.example.monumental.model.data.room.repository.LandmarkRepository
import com.example.monumental.model.entity.Journey
import com.example.monumental.model.entity.Landmark
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class JourneysViewModel(application: Application) : AndroidViewModel(application) {

    private val journeyRepository = JourneyRepository(application.applicationContext)
    private val landmarkRepository = LandmarkRepository(application.applicationContext)

    var journeys = journeyRepository.getJourneys()

    private val mainScope = CoroutineScope(Dispatchers.Main)

    /**
     * Inserts new Journey
     *
     * @return Long ID of inserted journey
     */
    fun createJourney(): Long = runBlocking {
        return@runBlocking journeyRepository.insertJourney(Journey(null,"New Journey!"))
    }

    /**
     * Updates journey
     *
     * @param journey Journey to update
     */
    fun updateJourney(journey: Journey) {
        mainScope.launch {
            journeyRepository.updateJourney(journey)
        }
    }

    /**
     * Sets the active journey
     *
     * @param journey Journey to set to active
     */
    fun setActiveJourney(journey: Journey) {
        mainScope.launch {
            journeyRepository.setActiveJourney(journey)
        }
    }

    /**
     * Deletes Journey
     *
     * @param journey Journey to delete
     */
    fun deleteJourney(journey: Journey) {
        mainScope.launch {
            journeyRepository.deleteJourney(journey)
        }
    }

    /**
     * Gets all Landmarks of Journey
     *
     * @param journeyId ID of Landmark to get Landmarks of
     * @return List of Landmarks
     */
    fun getLandmarksByJourney(journeyId: Int): LiveData<List<Landmark>?> {
        return landmarkRepository.getLandmarksByJourney(journeyId)
    }
}