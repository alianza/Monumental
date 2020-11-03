package com.example.monumental.ui.landmark

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.example.monumental.model.Journey
import com.example.monumental.model.Landmark
import com.example.monumental.room.repository.JourneyRepository
import com.example.monumental.room.repository.LandmarkRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class LandmarkViewModel(application: Application) : AndroidViewModel(application) {
    private val landmarkRepository = LandmarkRepository(application.applicationContext)
    private val journeyRepository = JourneyRepository(application.applicationContext)

    private val mainScope = CoroutineScope(Dispatchers.Main)

    fun deleteLandmark(landmark: Landmark) {
        mainScope.launch {
            landmarkRepository.deleteLandmark(landmark)
        }
    }

    fun getLandmarksByJourney(journeyId: Int): LiveData<List<Landmark>?> {
        return landmarkRepository.getLandmarksByJourney(journeyId)
    }

    fun setActiveJourney(journey: Journey) {
        mainScope.launch {
            journeyRepository.setActiveJourney(journey)
        }
    }
}