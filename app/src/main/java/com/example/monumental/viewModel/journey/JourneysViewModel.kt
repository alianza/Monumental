package com.example.monumental.viewModel.journey

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.monumental.model.data.room.repository.JourneyRepository
import com.example.monumental.model.entity.Journey
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

open class JourneysViewModel(application: Application) : AndroidViewModel(application) {

    private val journeyRepository = JourneyRepository(application.applicationContext)

    var journeys = journeyRepository.getJourneys()

    private val mainScope = CoroutineScope(Dispatchers.Main)

    fun deleteJourney(journey: Journey) {
        mainScope.launch {
            journeyRepository.deleteJourney(journey)
        }
    }

    fun createJourney(): Long = runBlocking {
        return@runBlocking journeyRepository.insertJourney(Journey(null,"New Journey!"))
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
