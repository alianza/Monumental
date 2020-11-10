package com.example.monumental.view.journey

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.monumental.data.room.repository.JourneyRepository
import com.example.monumental.model.Journey
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class JourneyViewModel(application: Application) : AndroidViewModel(application) {

    private val journeyRepository = JourneyRepository(application.applicationContext)

    var journeys = journeyRepository.getJourneys()

    private val mainScope = CoroutineScope(Dispatchers.Main)

    fun deleteJourney(journey: Journey) {
        mainScope.launch {
            journeyRepository.deleteJourney(journey)
        }
    }

    suspend fun createJourney(): Long {
        return journeyRepository.insertJourney(Journey(null,"New Journey!"))
    }

    fun updateJourney(journey: Journey) {
        mainScope.launch {
            journeyRepository.updateJourney(journey)
        }
    }

    fun setActiveJourney(journey: Journey) {
        mainScope.launch {
            journeyRepository.setActiveJourney(journey)
        }
    }
}