package com.example.monumental.ui.journey

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.monumental.model.Journey
import com.example.monumental.room.repository.JourneyRepository
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

    fun createJourney() {
        mainScope.launch {
            journeyRepository.insertJourney(Journey("New Journey!"))
        }
    }
}